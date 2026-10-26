# mc-launcher-core · MC 启动器核心库

**A reusable Minecraft: Java Edition launcher core library for Android — build your own MC launcher in ~50 lines.**

**可复用的 MC Java 版安卓启动器核心库**：加一个 Maven 依赖、自带 UI，即可得到完整可用的启动器。库只做底层（下载 / 安装 / 启动 / 账号 / 日志），界面由你决定。

```kotlin
val core = LaunchCore.create(context)
core.installer.install(InstallRequest(mcVersion = "1.21.1")).collect { /* 进度 */ }
val handle = core.launch(LaunchConfig(account = core.auth.offline("Steve"), versionId = "1.21.1"))
MinecraftSurface(handle)   // 可选 compose 构件
```

## 特性

- 📦 **AAR 零捆绑二进制**——JRE 8/17/21/25、ANGLE / gl4es / Zink 渲染栈、LWJGL 全部作为 runtime packs 按需下载（sha256 校验 + 断点续传），核心库 < 10MB
- 🌏 **多源下载**——GitHub Releases 为权威源 + 国内镜像，宿主可注入自己的 CDN（服务器作者可自建资源源）
- 🧩 **core 零 Android UI 依赖**（门禁脚本强制）——所有用户可感知事件走 `CoreEvent` / `LaunchHandle.logs` Flow
- 🔌 **UI 自由**——`MinecraftSurface`（画面 + 输入管线）在可选的 `:launchcore-compose`，参考实现即文档
- ⚖️ **GPL-3.0**——下游需同样开源；不捆绑任何游戏资源，版本/资产走 Mojang 官方 manifest

## 状态

M0 完成（API 草案 + CI + 门禁）。里程碑与架构决策（ADR-1~8）见 **[PROJECT.md](PROJECT.md)**。

| 里程碑 | 内容 | 状态 |
|---|---|---|
| M0 | 基线 fork + `:launchcore` API 草案 + 门禁 | ✅ |
| M1 | 边界重构：core 内零 UI 依赖 | 进行中 |
| M2 | 0.1（JitPack）：MinecraftSurface + sample 离线启动 | |
| M3 | 0.2：runtime packs 按需下载 + 微软登录 | |
| M4 | 1.0：mod loader + reference-app 验收 + Maven Central | |

## 致谢 / Credits

基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2)（tag 2.7.2）抽取，血统上溯 [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)。上游原始 README 见 [docs/upstream/](docs/upstream/)。

## License

[GPL-3.0](LICENSE)
