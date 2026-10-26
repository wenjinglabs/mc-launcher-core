package com.movtery.zalithlauncher.game

import com.movtery.zalithlauncher.setting.enums.MirrorSourceType

/**
 * 可读写 bridged 设置项：核心库通过 [get]/[set] 访问宿主设置。
 */
class SettingAccess<V>(
    val get: () -> V,
    val set: (V) -> Unit
)

/**
 * 游戏运行时所需的宿主配置读取口。
 *
 * 核心库不持有设置系统（setting 包属于 App 的 UI 目录）——宿主把自己的设置桥接进来：
 * `GameConfig.currentGamePathId = SettingAccess({ AllSettings.currentGamePathId.state }, { AllSettings.currentGamePathId.save(it) })`
 *
 * 按迁移波次增量扩展；未桥接时返回各字段的默认值（与 Zalith2 默认一致）。
 */
object GameConfig {
    /** 当前选中的游戏目录 id（GamePathManager 会写入切换结果） */
    @Volatile
    var currentGamePathId: SettingAccess<String> = SettingAccess({ "default" }, {})

    /** 当前使用的虚拟按键布局文件名 */
    @Volatile
    var controlLayout: () -> String = { "" }

    /** 游戏本体下载源偏好 */
    @Volatile
    var gameDownloadSource: () -> MirrorSourceType = { MirrorSourceType.AUTO }

    /** 资源平台（Mod/整合包等）下载源偏好 */
    @Volatile
    var assetPlatformSource: () -> MirrorSourceType = { MirrorSourceType.AUTO }
}
