package com.movtery.zalithlauncher.game.download.assets.platform

import androidx.compose.ui.graphics.Color

/**
 * PlatformReleaseType 的 App 侧映射（M1：枚举本体 Long 化入核心库）
 */
val PlatformReleaseType.composeColor: Color
    get() = Color(color)

val PlatformReleaseType.textRes: Int
    get() = when (this) {
        PlatformReleaseType.RELEASE -> com.movtery.zalithlauncher.R.string.download_assets_release_type_release
        PlatformReleaseType.BETA -> com.movtery.zalithlauncher.R.string.download_assets_release_type_beta
        PlatformReleaseType.ALPHA -> com.movtery.zalithlauncher.R.string.download_assets_release_type_alpha
    }
