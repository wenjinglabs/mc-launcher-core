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

package com.movtery.zalithlauncher.bridge

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ZLBridgeStates {

    private val _cursorMode = MutableStateFlow(CURSOR_ENABLED)
    /** 状态：指针模式（启用、禁用） */
    val cursorMode = _cursorMode.asStateFlow()

    /**
     * 变更指针模式
     */
    @JvmStatic
    fun changeCursorMode(mode: Int) {
        require(mode in 0..1)
        this._cursorMode.update { mode }
    }

    private val _cursorShape = MutableStateFlow(CursorShape.Arrow)
    /** 状态：指针形状 */
    val cursorShape = _cursorShape.asStateFlow()

    /**
     * 变更指针形状
     */
    @JvmStatic
    fun changeCursorShape(shape: CursorShape) {
        _cursorShape.update { shape }
    }

    @JvmStatic
    private val _windowChangeKey = MutableStateFlow(false)
    /** 状态：窗口变更刷新key */
    val windowChangeKey = _windowChangeKey.asStateFlow()

    fun onWindowChange() {
        this._windowChangeKey.update { old -> old.not() }
    }
}

/**
 * 指针形状（M1 自 App 层拆分：枚举本体进核心库，composeIcon 映射留在 App 层）
 */
enum class CursorShape {
    /** 箭头 */
    Arrow,

    /** 输入 */
    IBeam,

    /** 手形 */
    Hand,

    /** 十字 */
    CrossHair,

    /** 调整大小（上下） */
    ResizeNS,

    /** 调整大小（左右） */
    ResizeEW,

    /** 调整大小（全部方向） */
    ResizeAll,

    /** 禁止/无效操作 */
    NotAllowed
}
