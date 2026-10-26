# :launchcore

MC Java 版安卓启动器核心库。**本模块是整个项目对外的 API 面**，下游 App 只依赖它（和可选的 `:launchcore-compose`）。

## 模块纪律（CI 强制，见 `scripts/gate_core_purity.sh`）

- 禁止 `androidx.compose` / Compose 编译器插件
- 禁止 `android.widget.Toast`、`android.app.Activity`、`androidx.activity`
- 需要用户感知的事件一律走 `CoreEvent` / `LaunchHandle.logs`，由宿主 UI 决定呈现方式
- 需要画面的部分由宿主提供 Surface（M2 的 `MinecraftSurface` 属于可选的 `:launchcore-compose`，不在这里）

理由与完整架构见仓库根目录 `PROJECT.md`。
