package com.movtery.zalithlauncher.game.download.assets.platform

import android.util.Log
import com.movtery.zalithlauncher.game.GameConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.io.File
import com.movtery.zalithlauncher.game.addons.mirror.MirrorPriority
import com.movtery.zalithlauncher.game.addons.mirror.resolveMirrorPriority
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.MCIM_CURSEFORGE_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.MCIM_MODRINTH_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isInterruptedIOException
import java.io.FileNotFoundException
import java.io.IOException

private const val TAG = "MirroredSearch"

val modrinthSearcher = ModrinthSearcher()
val mirrorModrinthSearcher = ModrinthSearcher(
    api = MCIM_MODRINTH_API,
    source = "MCIM Modrinth"
)

val curseForgeSearcher = CurseForgeSearcher()
val mirrorCurseForgeSearcher = CurseForgeSearcher(
    api = MCIM_CURSEFORGE_API,
    source = "MCIM CurseForge"
)

/**
 * 镜像搜索助手（M1 自 App 层 _PlatformSearch 抽取：纯源排序逻辑，UI 状态机留在 App）
 */
suspend fun <E: AbstractPlatformSearcher, T> mirroredPlatformSearcher(
    searchers: List<E>,
    printLog: Boolean = true,
    block: suspend (E) -> T
): T {
    require(searchers.isNotEmpty()) { "Searcher list must not be empty." }

    val errors = mutableListOf<Exception>()
    var lastException: Exception? = null

    for (searcher in searchers) {
        try {
            if (printLog) {
                Logger.debug(TAG, "Starting to attempt to perform the operation on source: {${searcher.source}}")
            }
            return block(searcher)
        } catch (e: Exception) {
            Log.w("PlatformSearcher", "Failed to perform the operation on source: {${searcher.source}}", e)
            lastException = e

            if (e.isInterruptedIOException()) {
                throw e
            } else if (e is FileNotFoundException) {
                errors.add(e)
                break
            } else {
                errors.add(e)
            }
        }
    }

    if (printLog) {
        Logger.warning(TAG, 
            msg = "An error occurred during this search.",
            t = IOException("All sources have failed to attempt", lastException).apply {
                errors.forEachIndexed { i, e ->
                    addSuppressed(Exception("Mirror error #${i + 1}: ${e.message}"))
                }
            }
        )
    }
    throw lastException ?: IllegalStateException("Should not have executed to this stage.")
}

/**
 * 镜像源只能在中国地区使用
 */
fun mirroredCurseForgeSource(
    enabledMirror: Boolean = isChinaMainland()
): List<CurseForgeSearcher> {
    val source = resolveMirrorPriority(GameConfig.assetPlatformSource(), mainland = enabledMirror)
    val mirrorSource = mirrorCurseForgeSearcher.takeIf { enabledMirror }
    return when (source) {
        MirrorPriority.OFFICIAL -> listOf(curseForgeSearcher)
        MirrorPriority.MIRROR_FIRST ->
            listOfNotNull(mirrorSource, curseForgeSearcher)
    }
}

/**
 * 镜像源只能在中国地区使用
 */
fun mirroredModrinthSource(
    enabledMirror: Boolean = isChinaMainland()
): List<ModrinthSearcher> {
    val source = resolveMirrorPriority(GameConfig.assetPlatformSource(), mainland = enabledMirror)
    val mirrorSource = mirrorModrinthSearcher.takeIf { enabledMirror }
    return when (source) {
        MirrorPriority.OFFICIAL -> listOf(modrinthSearcher)
        MirrorPriority.MIRROR_FIRST ->
            listOfNotNull(mirrorSource, modrinthSearcher)
    }
}

/**
 * 按 SHA-1 批量查 Modrinth 版本 / CurseForge 指纹（M1 抽取）
 */
suspend fun getModrinthVersBySha1(
    sha1List: List<String>
): Map<String, ModrinthVersion> = mirroredPlatformSearcher(
    searchers = mirroredModrinthSource(),
    printLog = false
) { searcher ->
    searcher.getVersionFiles(sha1List = sha1List)
}

/**
 * 通过本地文件的 CurseForge 指纹批量获取 CurseForge 平台对应的文件
 * @param fingerprints 指纹列表，长度不应超过 [FINGERPRINT_BATCH_SIZE]
 * @return 键为文件指纹，值为匹配到的文件，未命中的指纹不在结果中
 */
suspend fun getCFFilesByFingerprints(
    fingerprints: List<Long>
): Map<Long, CurseForgeFile> = mirroredPlatformSearcher(
    searchers = mirroredCurseForgeSource(),
    printLog = false
) { searcher ->
    searcher.getFilesByFingerprints(fingerprints = fingerprints)
}

/**
 * 单项目 / 本地文件反查（M1 抽取）
 */
suspend fun getProjectByVersion(
    projectId: String,
    platform: Platform,
    printLog: Boolean = true
): PlatformProject = withContext(Dispatchers.IO) {
    when (platform) {
        Platform.MODRINTH -> mirroredPlatformSearcher(
            searchers = mirroredModrinthSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
        Platform.CURSEFORGE -> mirroredPlatformSearcher(
            searchers = mirroredCurseForgeSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
    }
}

suspend fun getVersionByLocalFile(file: File, sha1: String): PlatformVersion? = withContext(Dispatchers.IO) {
    coroutineScope {
        val modrinthDeferred = async(Dispatchers.IO) {
            runCatching {
                mirroredPlatformSearcher(
                    searchers = mirroredModrinthSource(),
                    printLog = false
                ) { searcher ->
                    searcher.getVersionByLocalFile(file, sha1)
                }
            }.getOrNull()
        }

        val curseForgeDeferred = async(Dispatchers.IO) {
            runCatching {
                mirroredPlatformSearcher(
                    searchers = mirroredCurseForgeSource(),
                    printLog = false
                ) { searcher ->
                    searcher.getVersionByLocalFile(file, sha1)
                }
            }.getOrNull()
        }

        val result = select {
            modrinthDeferred.onAwait { result ->
                if (result != null) {
                    curseForgeDeferred.cancel()
                    result
                } else {
                    null
                }
            }
            curseForgeDeferred.onAwait { result ->
                if (result != null) {
                    modrinthDeferred.cancel()
                    result
                } else {
                    null
                }
            }
        }

        result ?: run {
            if (!modrinthDeferred.isCompleted) modrinthDeferred.await()
            else if (!curseForgeDeferred.isCompleted) curseForgeDeferred.await()
            else null
        }
    }
}
