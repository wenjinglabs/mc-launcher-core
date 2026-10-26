# LaunchCore — Agent 工作约定

MC Java 版安卓启动器核心库，从 ZalithLauncher2 tag `2.7.2` 硬 fork 而来（GPL-3.0）。
工程路线图、架构决策（ADR-1~8）、测试策略见 **PROJECT.md**，改代码前先读它。

## 硬规则

1. **`:launchcore` 禁止 Android UI 依赖**：无 Compose（连编译器插件都不许）、无 Toast、无 Activity。
   每次改动后、push 前必跑 `bash scripts/gate_core_purity.sh`。本项目**不使用 GitHub Actions**（ADR-9），验证全部本地执行。
2. **AAR 零捆绑二进制**（ADR-2）：不要往 `:launchcore` 的 assets/jniLibs 里塞运行时；大文件属于 runtime packs，走下载。
3. **硬 fork 纪律**（ADR-1）：不做 `git pull` 上游；同步安全补丁用 cherry-pick 并注明来源 tag。
4. **0.x API**：公开 API 标 `@UnstableApi`；包名 `io.launchcore.core` 暂为占位，发布命名空间定案后统一替换。
5. 基线测试必须常绿：`./gradlew :launchcore:assembleRelease :launchcore:test`。

## 常用命令

```bash
bash scripts/gate_core_purity.sh                              # core 纯净度门禁
./gradlew :launchcore:assembleRelease :launchcore:test        # 库构建 + 单测
./gradlew :ZalithLauncher:assembleDebug                       # 原 App 基线回归
```

## 环境注意

- Windows + Git Bash；仓库已开 `core.longpaths`
- `local.properties` 指向本机 SDK，不入库（已被 .gitignore）
- JDK 25 在 PATH；CI 用 temurin 21
