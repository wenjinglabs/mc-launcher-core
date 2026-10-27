# LaunchCore

MC Java 版安卓**启动器核心库**：下游开发者加一个 Maven 依赖、自带 UI，即可得到完整可用的启动器。
由 ZalithLauncher2 抽取而来，**UI 层不属于本库**——库做底层，宿主做界面。

> 基线：ZalithLauncher2 tag `2.7.2`（硬 fork，GPL-3.0）。
> 完整背景与选型论证见会话记录；本文件是唯一权威的工程决策记录。

## 目标形态

下游 `build.gradle.kts`：

```kotlin
implementation("io.launchcore:launchcore:<ver>")            // 核心
implementation("io.launchcore:launchcore-compose:<ver>")    // 可选：MinecraftSurface + 参考 UI
```

get-started 约 50 行：

```kotlin
val core = LaunchCore.create(context)
core.installer.install(InstallRequest(mcVersion = "1.21.1")).collect { /* 进度 */ }
val account = core.auth.offline("Steve")
val handle = core.launch(LaunchConfig(account = account, versionId = "1.21.1"))
MinecraftSurface(handle)   // 来自可选 compose 构件
```

**API 完整性的最终验收**：Zalith2 自己的 UI 能重建在本库 API 之上（reference-app 狗粮）。

## 交付物

| 构件 | 内容 | AAR 体积目标 |
|---|---|---|
| `:launchcore` | 版本下载/安装、启动、账号、日志、runtime pack 管理、JNI 胶水 | **< 10MB** |
| `:launchcore-compose` | **组件库（一等公民交付物）**：`MinecraftSurface`（含默认虚拟按键层）、安装进度、版本选择、账号登录、启动按钮、崩溃面板、日志查看器等开箱组件 + 整页模板；底座优先复用仓库内 Terracotta/InputMap/LayerController | 中 |
| runtime packs | JRE 8/17/21/25、ANGLE/gl4es/Zink、LWJGL 3.3/3.4、AWT 适配 —— **不进 AAR**，按需下载 | 外部托管 |
| `:sample` | get-started 示例 App（目标 ≈30 行，靠组件层达成） | — |
| reference-app | 原 Zalith2 UI 重建在 core 上 = API 验收 + 回归基线 | — |

## 里程碑

- [x] **M0**（本阶段）：硬 fork 2.7.2、`:launchcore` 模块 + API 草案、纯净度门禁、PROJECT.md
- [ ] **M1**：边界重构——按包映射搬家、解开 Compose/Toast/Activity 依赖、门禁脚本持续绿灯（本地执行）。交付：App 功能不变，core 无 Android UI 依赖
- [ ] **M2**：对外 0.1（JitPack）——`MinecraftSurface` + 组件库第一组（安装进度/账号登录/启动按钮/崩溃面板）、sample 离线启动 arm64（≈30 行）、get-started 文档
- [ ] **M3**：0.2——runtime packs 按需下载上线（AAR 瘦身达标）、渲染器切换。**微软登录延后**（2026-10-27 维护者决策：当前只做离线模式；API 契约已就绪——`LaunchCoreConfig.microsoftClientId` + `Auth.microsoft` 抛 IllegalStateException，实现在延后期间保持抛错即可）
- [ ] **M4**：1.0——mod loader 安装、reference-app 验收、迁 Maven Central、锁 API

## 架构决策（ADR）

**ADR-1 硬 fork，不追上游**。模块重排后 merge 必然冲突；安全补丁人工 cherry-pick。从 fork 起我们即维护者。

**ADR-2 AAR 零捆绑二进制**。基线自带 218MB .so + 183MB assets，不可发布也不该捆绑；全部转为 runtime packs（manifest 索引 + sha256 + 断点续传）。此决策从 M0 起约束 assets 搬迁方式。

**ADR-3 多源下载**。GitHub Releases 为唯一权威源；国内镜像首选 Cloudflare R2（免流量费起步）；游戏本体文件走 BMCLAPI。宿主可注入自定义源列表（服务器作者指自己的 CDN，顺带成为卖点）。

**ADR-4 core 零 Android UI 依赖**。`:launchcore` 禁 Compose/Toast/Activity（`scripts/gate_core_purity.sh` CI 强制）。用户感知事件走 `CoreEvent`（SharedFlow），日志走 `LaunchHandle.logs`。理由：库的客户是开发者，UI 呈现是宿主的权利。

**ADR-5 API 稳定性**。0.x 全库 `@UnstableApi`（RequiresOptIn），1.0 前可破坏性变更；1.0 由 reference-app 验收后锁定。

**ADR-6 进程模型**。v1 游戏跑在宿主进程（与 Zalith2 一致），崩溃连带宿主——文档明示 + `LaunchState.Crashed(logTail)` 兜底回调；独立 `:game` 进程列为 1.x 候选。

**ADR-7 许可**。全库 GPL-3.0（继承自 Pojav→Zalith2 血统），下游必须开源。发布 Maven Central 合规（GPL 为 OSI 协议）；游戏资源零捆绑，版本/资产走 Mojang 官方 manifest。

**ADR-8 ABI 策略**。v1 锁 arm64-v8a（基线现状），32 位/x86 是 1.0 后的扩展项——每加一个 ABI，packs/测试矩阵/构建全翻倍。

**ADR-9 不使用 GitHub Actions**（2026-10-27 维护者决策）。所有验证本地执行：门禁脚本 + 单测每次改动必跑；插桩/E2E 用本地无头模拟器或真机。代价是仓库无 CI badge、纯靠本地纪律；如未来需要再重新评估。

**ADR-10 包名与发布坐标解耦**（2026-10-27 维护者决策）。API 包名固定 `io.launchcore.core`，不随 GitHub 账号（曾用名 LanHuangJG → wenjinglabs）变化；M4 发布 Maven Central 时 group id 使用 Central Portal 已验证的 namespace，包名与 group id 无需一致。内部搬运代码保持 `com.movtery.zalithlauncher.*` 原 FQCN（见 MIGRATION.md），API 收口在 1.0 前单独处理。

**ADR-11 微软登录 Client ID 由宿主注入**（2026-10-27 维护者决策）。库不内置任何 OAuth Client ID，也不复用上游 MovTery 的注册；宿主注册自己的 Azure 应用，经 `LaunchCoreConfig.microsoftClientId` 传入，未配置时 `Auth.microsoft` 抛 IllegalStateException。离线登录零依赖，get-started 不受影响。

**ADR-12 runtime packs 托管**（2026-10-27 维护者决策）。权威源挂 wenjinglabs 个人 GitHub Releases；国内镜像（Cloudflare R2）M3 落地时开通；游戏本体文件继续走 BMCLAPI。

**ADR-13 core 单一 HTTP 栈，零 ktor**（2026-10-27 维护者决策）。core 只用 OkHttp（下载/API 共用引擎）+ kotlinx-serialization（JSON），不再引入 ktor——原先 ktor-on-okhttp 与裸 OkHttp 双栈冗余。core 的 JSON API 层为 `utils/network/HttpJson.kt`（fetchJson/postJson/fetchText + HttpException）；ktor 版 GLOBAL_CLIENT 与助手原样移至 App 层（path/AppGlobalClient.kt、utils/network/KtorHttp.kt）。**铁则：任何携带 ktor import 的文件搬入 core 时，必须先重写为 HttpJson 助手**（ktor 只属于 App 的 viewmodel/ui 层）。

**ADR-14 门禁修正案：compose.runtime 入 core**（2026-10-27 维护者批准）。门禁改为只允许 `androidx.compose.runtime`（纯状态管理库，无渲染、无 android UI 类）；compose.ui/foundation/material/animation 等渲染层与 Toast/Activity 依旧全禁。立法本意不变——core 不渲染、不做 UI 决定；mutableStateOf 只是可观察状态载体，是本代码库唯一的状态栈（与 Room 同构论证：非冗余）。动因：Version/LocalMod/RemoteMod/ServerData 四个核心域模型内嵌 compose 状态，App 侧约 30 处读取；重写 StateFlow 属于无收益手术。

## 测试策略（headless 优先）

| 层 | 覆盖 | 工具 | 模拟器 |
|---|---|---|---|
| 1 纯 JVM | manifest 解析、classpath/JVM 参数、下载队列、多源 fallback、校验 | JUnit + MockWebServer3 + 录制 fixture | 否 |
| 2 Robolectric | 需要 Context 的文件/目录/数据库逻辑 | Robolectric | 否 |
| 3 插桩 smoke | JNI 加载、pack .so 完整性、日志结构 | connectedAndroidTest | CI 无头模拟器或真机 |
| 4 E2E 启动 | MC 真的跑到主循环 | 日志断言：`LWJGL version` → `Setting user:` → 90s 无 FATAL | 同上 |

- **golden-file**：启动参数/classpath 输出与录制基线逐字段比对，不启动游戏即可抓回归
- **执行位置**（ADR-9）：1、2 层本地随时跑；3、4 层本地无头模拟器或真机 adb
- 判定"启动成功"= 结构化日志事实，不依赖画面/截图

## 包 → 模块迁移映射（M1 执行）

| Zalith2 现有包 | 去向 |
|---|---|
| `bridge`、`library` | `:launchcore`（native 胶水） |
| `game`、`path`、`provider`、`context`、`contract` | `:launchcore` |
| `filemanager`、`utils`、`coroutine`、`crashlogs`、`database`、`setting` | `:launchcore`（内部） |
| `ui`、`viewmodel`、`terracotta`、`notification`、`keepalive`、`upgrade` | 留在 reference-app，重建时按需上移 |

## 当前构建信息

- Gradle 9.5.0 / AGP 9.3.0 / Kotlin 2.4.20 / compileSdk 37 / minSdk 26 / JVM target 11
- 常用命令：`./gradlew :launchcore:assembleRelease :launchcore:test`；门禁 `bash scripts/gate_core_purity.sh`
- API 草案位于 `launchcore/src/main/java/io/launchcore/core/`（包名固定 `io.launchcore.core`，见 ADR-10）
