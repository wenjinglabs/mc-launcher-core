import io, subprocess, re

# 1. 从 git 索引恢复 sha1 函数体（staged blob）
out = subprocess.run(['git', 'grep', '--cached', '-l', 'fun getModrinthVersBySha1'],
                     capture_output=True, text=True).stdout.strip()
blob_path = out.splitlines()[0].replace('\\', '/')
staged = subprocess.run(['git', 'show', ':' + blob_path],
                        capture_output=True, text=True).stdout
m = re.search(r'suspend fun getModrinthVersBySha1\(.*?\n\}\n\n/\*\*[^*]*\*/\nsuspend fun getCFFilesByFingerprints\(.*?\n\}', staged, re.S)
assert m, 'sha1 block not in staged blob'
block = m.group(0)

mf = 'launchcore/src/main/java/com/movtery/zalithlauncher/game/download/assets/platform/MirroredSearch.kt'
s = io.open(mf, encoding='utf-8').read()
if 'fun getModrinthVersBySha1' not in s:
    s += '\n/**\n * 按 SHA-1 批量查 Modrinth 版本（M1 抽取）\n */\n' + block + '\n'
    io.open(mf, 'w', encoding='utf-8', newline='\n').write(s)
print('sha1 functions recovered')

# 2. isChinese 从 LocalUtils 抽取
lu = 'ZalithLauncher/src/main/java/com/movtery/zalithlauncher/utils/LocalUtils.kt'
s = io.open(lu, encoding='utf-8').read()
m1 = re.search(r'/\*\*\n \* 检查当前环境是否为中文环境\n \*/\nfun isChinese\(.*?\n\}\n\n/\*\*\n \* 判断单个Locale是否为中文环境\n \*/\nfun isChineseLocale\(.*?\n\}\n', s, re.S)
assert m1, 'isChinese not found'
block1 = m1.group(0)
s = s.replace(block1, '')
io.open(lu, 'w', encoding='utf-8', newline='\n').write(s)
os.makedirs('launchcore/src/main/java/com/movtery/zalithlauncher/utils', exist_ok=True)
io.open('launchcore/src/main/java/com/movtery/zalithlauncher/utils/LocaleCheck.kt', 'w', encoding='utf-8', newline='\n').write(
    'package com.movtery.zalithlauncher.utils\n\nimport android.content.Context\nimport java.util.Locale\n\n'
    + '/**\n * 中文环境判定（M1 自 App 层 LocalUtils 抽取）\n */\n' + block1)
print('isChinese extracted')
