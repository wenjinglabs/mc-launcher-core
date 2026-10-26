# M1 迁移作战表

策略（详见 PROJECT.md ADR-1/4）：**FQCN 不变**（`com.movtery.zalithlauncher.*` 原包名搬入 `:launchcore`，App 侧零改动）、逐文件卡点判定（compose / toast / activity / hilt / R 资源 / BuildKeys / BuildConfig 任一命中即留后续波次）、依赖方向单向（launchcore 不得 import 留在 App 的包）。

每波验收：`bash scripts/gate_core_purity.sh` + `./gradlew :launchcore:assembleRelease :launchcore:test :ZalithLauncher:assembleDebug` 全绿。

## 逐包审计（2026-10-27）

| 包 | 文件数 | compose | toast | activity | hilt | 波次 |
|---|---|---|---|---|---|---|
| bridge | 6 | 1 | 0 | 1 | 0 | W2（解 ZLNativeInvoker 的 activity+BuildKeys） |
| components | 8 | 0 | 0 | 0 | 0 | W1b（Components.kt/Jre.kt 需解 R 引用） |
| context | 2 | 0 | 0 | 0 | 0 | W1b（依赖 utils.file/logging 先行） |
| contract | 2 | 0 | 0 | 1 | 0 | W1b（MediaPickerContract 解 activity） |
| coroutine | 9 | 0 | 0 | 0 | 0 | W1b（Task/TaskLogOutput/TitledTask 需解 ui 依赖！） |
| crashlogs | 5 | 0 | 0 | 0 | 0 | W1b |
| database | 2 | 0 | 0 | 0 | 0 | W1b（Room，需解 game.account 依赖 + ksp 接入） |
| filemanager | 71 | 22 | 0 | 3 | 2 | W3 |
| game | 344 | 19 | 2 | 5 | 0 | W3（逐子包，污染率仅 5.5%） |
| keepalive | 2 | 0 | 0 | 0 | 0 | W1b（TaskKeepAliveService 解 R） |
| library | 3 | 0 | 0 | 0 | 0 | W1b（解 R） |
| path | 3 | 0 | 0 | 0 | 0 | W1b（PathManager 解 game.launch） |
| provider | 1 | 0 | 0 | 0 | 0 | ✅ W1a |
| setting | 27 | 5 | 0 | 0 | 0 | W2 |
| utils | 47 | 4 | 1 | 4 | 0 | W2（纯净子集 W1b 起） |
| viewmodel / ui / terracotta / notification / upgrade | — | — | — | — | — | **不搬**（留 reference-app） |

## 波次进度

- ✅ **W1a**：`utils/logging` 内核（Level / LogMessage / Logger + 新增 **LoggerConfig**：日志目录、保留天数、横幅、环境信息全部改为宿主注入，解耦 BuildKeys/PathManager/AllSettings/printLauncherInfo）；`zipDirectory` 从 FileUtils 抽出；`provider/FolderProvider.java`。
- ✅ **W1b-批次一**：**path 包整体**（PathManager/LibPath/LogName + UrlManager 重构）+ **网络三件**（NetWorks/ResilientDns/RegionRestriction）+ **file/string 工具**（FileUtils/zip 适配器家族/InvalidFilenameException/UnpackZipException/_CompareString/StringUtils/ShiftDirection）+ 新增 **NetIdentity**（UA/CurseForge key 宿主注入）与 `isInterruptedIOException`、`isChinaMainland` 抽取。**Zalith2 品牌 URL（14 个常量）拆回 App 层 path/AppUrls.kt，core 保持品牌中立**。launchcore 新增依赖：ktor(client/content-negotiation/serialization)、okhttp、okio、commons-codec/compress、maven-artifact、androidx-core；启用 kotlinx-serialization 插件。
- ⬜ **W1b-批次二（待做）**：coroutine 子集（Task/TaskLogOutput/TitledTask 需解 ui 依赖）、crashlogs、context、components（解 R）、keepalive（解 R）、utils/image+video+device 子集（需 coil）。
- ⬜ **W2**：setting、utils 混合部分（LocalUtils/NetWorkUtils）、bridge（JNI 胶水收口 facade）、contract。
- ⬜ **W3**：filemanager、game（大头）、database（依赖 game.account，随 W3）。

## 环境备忘（本机）

- GraalVM 25 的 jlink 与 AGP JdkImageTransform 不兼容（Android system-modules 转换失败）→ `~/.gradle/gradle.properties` 已设 `org.gradle.java.home=<Android Studio JBR 21>`。App 模块本就有 39 个 Java 文件，此问题影响全局，非 launchcore 独有。
- NDK r25b 曾缺 `source.properties` 的 `Pkg.BaseRevision` 字段，已补全。
