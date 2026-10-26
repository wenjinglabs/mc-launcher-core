package com.movtery.zalithlauncher.setting.enums

import com.movtery.zalithlauncher.R

/**
 * MirrorSourceType 的展示名资源（App 侧；枚举本体已迁入核心库）
 */
val MirrorSourceType.textRes: Int
    get() = when (this) {
        MirrorSourceType.AUTO -> R.string.settings_launcher_mirror_auto
        MirrorSourceType.OFFICIAL -> R.string.settings_launcher_mirror_official
        MirrorSourceType.MIRROR -> R.string.settings_launcher_mirror_mirror
    }
