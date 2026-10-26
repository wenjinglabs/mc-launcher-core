/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.setting.enums

/**
 * 下载源偏好（M1 自 App 层迁入：textRes 展示名改为 App 侧扩展属性，枚举本体进核心库）
 */
enum class MirrorSourceType {
    /**
     * 自动选择下载源
     * 依据当前网络环境判定初始偏好，后续由下载引擎按失败情况自适应换源
     */
    AUTO,

    /**
     * 尽量使用官方源
     */
    OFFICIAL,

    /**
     * 尽量使用镜像源
     */
    MIRROR;

    companion object {
        /** 旧版枚举名到现名的映射，读取历史存档设置时兜底 */
        val LEGACY_NAMES: Map<String, MirrorSourceType> = mapOf(
            "OFFICIAL_FIRST" to OFFICIAL,
            "MIRROR_FIRST" to MIRROR
        )
    }
}
