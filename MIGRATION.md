# M1 迁移作战表

策略（详见 PROJECT.md ADR-1/4）：**FQCN 不变**（`com.movtery.zalithlauncher.*` 原包名搬入 `:launchcore`，App 侧零改动）、逐文件卡点判定（compose / toast / activity / hilt / R 资源 / BuildKeys / BuildConfig 任一命中即留后续波次）、依赖方向单向（launchcore 不得 import 留在 App 的包）。

每波验收：`bash scripts/gate_core_purity.sh` + `./gradlew :launchcore:assembleRelease :launchcore:test :ZalithLauncher:assembleDebug` 全绿。

**ADR-13 铁则**：core 单一 HTTP 栈（OkHttp + kotlinx-serialization，零 ktor）。携带 `import io.ktor` 的文件搬入 core 前必须重写为 `utils/network/HttpJson.kt` 助手（fetchJson/postJson/postForm/fetchText）；ktor 版客户端与助手留在 App 层（path/AppGlobalClient.kt、utils/network/KtorHttp.kt）。

**W1b 教训**：逐文件审计只查 `import` 不够——**同包符号不经 import**，Kotlin 同包互赖全部隐形。后续波次先按"同包是否有文件留下"补查，再执行搬运。

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
- ✅ **W1b-批次二**：coroutine 纯净四件（DataBridge/FlowExtensions/MutableTransitionStateFlow/TaskStage）+ **crashlogs 全套**（AbstractAPI 已按 ADR-13 重写为 `postForm`；MCLogsAPI/MirroredAPI/响应模型）+ context/Contexts.kt（含 copyAssetFile）+ components 子集（AbstractUnpackTask/InstallableItem/UnpackSingleTask/jre/UnpackJnaTask）。HttpJson 新增 `postForm`。
- ✅ **W2a**：**AndroidStringText 拆分**——模型（非 sealed 接口 + Text/StringRes/Appended + 工厂/DSL）进 core（FQCN 不变，宿主可扩展变体）；App 层保留渲染器/富文本变体 `AnnotatedText`/`toAndroidString`（ui/AndroidStringTextCompose.kt）。**coroutine 任务引擎全套五件**（Task/TitledTask/TaskLogOutput/TaskSystem/TaskFlowExecutor）进 core。**TaskKeepAlive** 进 core（前台 Service 经 `serviceIntentProvider` 注入，Service 留 App）。
- ⛔ **W2b 改道（审计否决原方案）**：setting 包**留 App**——审计发现 `AbstractSettingUnit.state` 用 compose runtime（`mutableStateOf`），全库 284 处 UI 直接读 `.state` 重组；8 个枚举带 R 字符串；AllSettings 满身 game/ui 依赖。它是"App 的 UI 设置目录"而非干净持久化设施。**MMKV 不进 core**（core 当前零设置消费）；W3 game 包搬入时用注入式配置接口承接其 AllSettings 读取，届时再按事实评估持久化后端。
- ✅ **W2c（提前完成）**：**bridge JNI 面收口**——ZLBridge.java / NativeLibraryLoader.java / LoggerBridge.java / FliteTts.kt 进 core（FQCN 不变，JNI 符号绑定不受影响）。留 App：ZLBridgeStates（compose PointerIcon）、ZLNativeInvoker（Activity+启动编排 UI 胶水，M2 做 LaunchCore 实现时抽纯启动路径）。**注意：.so 仍由 App 模块构建**（externalNativeBuild 在 App），core 侧 native 打包（launchcore-native 构件）是 M3 工作——下游单独依赖 core 时缺 .so，M3 解决。
- ✅ **W3-1**：**database 进 core**（Room 决策落地：3 表/浅 CRUD/两消费方全在 core，room-runtime 无 native）+ ksp/kotlin-parcelize 接线。**game/download/engine 全套 7 件**（分块/换源/重试下载引擎——M2 核心）。**renderer 全套 9 件**。**account 持久化集群**（Account 实体/Dao/Type/异常、auth_server.data、wardrobe 下载器×5）。**path 实体**（GamePath/GamePathDao）。抽取（FQCN 不变）：GSON、BitmapExt、DownloadHelpers（downloadFile/fetchString/withSpeedReport 族）。`WardrobeHooks` 钩子解开 Account 实体→AccountsManager 反向依赖（App init 注入）。
- ⬜ **W3-2（待做）**：**AllSettings 注入接口**（23 项/16 文件）→ 解锁 account 其余/addons/path manager/sdl/multirt/plugin/support。
- ✅ **W3-2（缩水版，侦察改写）**：原计划的 addons/ControlManager 侦察后推迟——addons 拖着 W3-3 的 platform 区 + ADR-13 移走的 ktor 助手（safeBodyAsJson 等）+ 一个 App UI 函数；ControlManager 依赖 :LayerController（compose 模块），**留 App**。实际完成：**MirrorSourceType 去 R 进 core**（textRes 展示名改为 App 侧扩展属性，UI 单点补 import）；**GameConfig**（SettingAccess 读写桥 + 拉取式 lambda）与 **StoragePermissionState** 两个注入面；**GamePathManager 进 core**（currentGamePathId 走 SettingAccess，权限状态走镜像，VersionsManager.refresh 走 versionsRefresh 钩子）；ZLApplication 完成桥接。
- ⬜ **W3-3（待做，终波）**：R→core 资源迁移（已获批准）+ ktor 助手调用点重写为 HttpJson + GameConfig 扩展（renderer/ramAllocation/jvmArgs/javaRuntime/graphicsApi 等）→ download（26 脏）/version（17 脏）/launch（9 脏）/addons（39，含 3 脏）/plugin（5 脏）/account 余部/sdl/multirt/keycodes/input。**filemanager/ControlManager 留 App**。
- 留 App 不搬：library（R+BuildKeys）、CopyLabels（BuildKeys）、utils/image+video（死代码）、VulkanCapabilities（UI 调用方）、setting 整包（见上）、contract（ActivityResultContract 纯 UI）。
- ⬜ **W3**：filemanager、game（大头）、database（依赖 game.account，随 W3）。

## 环境备忘（本机）

- GraalVM 25 的 jlink 与 AGP JdkImageTransform 不兼容（Android system-modules 转换失败）→ `~/.gradle/gradle.properties` 已设 `org.gradle.java.home=<Android Studio JBR 21>`。App 模块本就有 39 个 Java 文件，此问题影响全局，非 launchcore 独有。
- NDK r25b 曾缺 `source.properties` 的 `Pkg.BaseRevision` 字段，已补全。
