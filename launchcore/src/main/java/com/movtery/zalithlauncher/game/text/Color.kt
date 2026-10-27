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

package com.movtery.zalithlauncher.game.text

import kotlinx.serialization.Serializable

const val IDENTIFIER_BLACK = "black"
const val IDENTIFIER_DARK_BLUE = "dark_blue"
const val IDENTIFIER_DARK_GREEN = "dark_green"
const val IDENTIFIER_DARK_AQUA = "dark_aqua"
const val IDENTIFIER_DARK_RED = "dark_red"
const val IDENTIFIER_DARK_PURPLE = "dark_purple"
const val IDENTIFIER_GOLD = "gold"
const val IDENTIFIER_GRAY = "gray"
const val IDENTIFIER_DARK_GRAY = "dark_gray"
const val IDENTIFIER_BLUE = "blue"
const val IDENTIFIER_GREEN = "green"
const val IDENTIFIER_AQUA = "aqua"
const val IDENTIFIER_RED = "red"
const val IDENTIFIER_LIGHT_PURPLE = "light_purple"
const val IDENTIFIER_YELLOW = "yellow"
const val IDENTIFIER_WHITE = "white"

/**
 * 表示 Minecraft 中的文本颜色（ADR-14：核心域用 Long ARGB，宿主侧按需转 compose Color）
 * @param foreground 文字的前景色（ARGB）
 * @param background 文字的背景色（ARGB）
 */
@Serializable
data class TextColor(
    val foreground: Long,
    val background: Long
)

val COMMON_BLACK = TextColor(0xFF000000, 0xFF000000)

val DARK_BLUE = TextColor(foreground = 0xFF0000AA, background = 0xFF00002A)
val DARK_GREEN = TextColor(foreground = 0xFF00AA00, background = 0xFF002A00)
val DARK_AQUA = TextColor(foreground = 0xFF00AAAA, background = 0xFF002A2A)
val DARK_RED = TextColor(foreground = 0xFFAA0000, background = 0xFF2A0000)
val DARK_PURPLE = TextColor(foreground = 0xFFAA00AA, background = 0xFF2A002A)
val GOLD = TextColor(foreground = 0xFFFFAA00, background = 0xFF2A2A00)
val GRAY = TextColor(foreground = 0xFFAAAAAA, background = 0xFF2A2A2A)
val DARK_GRAY = TextColor(foreground = 0xFF555555, background = 0xFF151515)
val BLUE = TextColor(foreground = 0xFF5555FF, background = 0xFF15153F)
val GREEN = TextColor(foreground = 0xFF55FF55, background = 0xFF153F15)
val AQUA = TextColor(foreground = 0xFF55FFFF, background = 0xFF153F3F)
val RED = TextColor(foreground = 0xFFFF5555, background = 0xFF3F1515)
val LIGHT_PURPLE = TextColor(foreground = 0xFFFF55FF, background = 0xFF3F153F)
val YELLOW = TextColor(foreground = 0xFFFFFF55, background = 0xFF3F3F15)
val WHITE = TextColor(foreground = 0xFFFFFFFF, background = 0xFF3F3F3F)

/**
 * 通过颜色名称获取文本颜色
 */
fun parseColorFromIdentifier(identifier: String): TextColor? {
    return when (identifier.lowercase()) {
        IDENTIFIER_BLACK -> COMMON_BLACK
        IDENTIFIER_DARK_BLUE -> DARK_BLUE
        IDENTIFIER_DARK_GREEN -> DARK_GREEN
        IDENTIFIER_DARK_AQUA -> DARK_AQUA
        IDENTIFIER_DARK_RED -> DARK_RED
        IDENTIFIER_DARK_PURPLE -> DARK_PURPLE
        IDENTIFIER_GOLD -> GOLD
        IDENTIFIER_GRAY -> GRAY
        IDENTIFIER_DARK_GRAY -> DARK_GRAY
        IDENTIFIER_BLUE -> BLUE
        IDENTIFIER_GREEN -> GREEN
        IDENTIFIER_AQUA -> AQUA
        IDENTIFIER_RED -> RED
        IDENTIFIER_LIGHT_PURPLE -> LIGHT_PURPLE
        IDENTIFIER_YELLOW -> YELLOW
        IDENTIFIER_WHITE -> WHITE
        else -> null
    }
}

private fun channel(argb: Long, shift: Int): Float {
    val value = ((argb ushr shift) and 0xFFL).toInt() / 255f
    return value
}

fun scaleColor(
    color: Long,
    scaleR: Float,
    scaleG: Float,
    scaleB: Float
): Long {
    val a = ((color ushr 24) and 0xFFL).toInt()
    val r = ((channel(color, 16) * scaleR).coerceIn(0f, 1f) * 255f).toInt()
    val g = ((channel(color, 8) * scaleG).coerceIn(0f, 1f) * 255f).toInt()
    val b = ((channel(color, 0) * scaleB).coerceIn(0f, 1f) * 255f).toInt()
    return (a.toLong() shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}

/**
 * 仿 Minecraft，生成与之对应的阴影色
 */
fun shadowColor(color: Long): Long =
    scaleColor(color, 0.25f, 0.25f, 0.25f)

/**
 * Minecraft 的颜色代码，参考 [Minecraft Wiki](https://zh.minecraft.wiki/w/%E6%A0%BC%E5%BC%8F%E5%8C%96%E4%BB%A3%E7%A0%81#%E9%A2%9C%E8%89%B2%E4%BB%A3%E7%A0%81)
 */
val MINECRAFT_COLOR_FORMAT = mapOf(
    '0' to COMMON_BLACK,
    '1' to DARK_BLUE,
    '2' to DARK_GREEN,
    '3' to DARK_AQUA,
    '4' to DARK_RED,
    '5' to DARK_PURPLE,
    '6' to GOLD, //仅 JE，BE 为 #402A00
    '7' to GRAY,
    '8' to DARK_GRAY,
    '9' to BLUE,
    'a' to GREEN,
    'b' to AQUA,
    'c' to RED,
    'd' to LIGHT_PURPLE,
    'e' to YELLOW,
    'f' to WHITE,
)
