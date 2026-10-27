package com.movtery.zalithlauncher.bridge

import android.view.PointerIcon as NativePointerIcon
import androidx.compose.ui.input.pointer.PointerIcon

/**
 * CursorShape 的 compose 映射（M1 拆分：枚举本体已入核心库，渲染映射留在 App 层）
 */
val CursorShape.composeIcon: PointerIcon
    get() = when (this) {
        CursorShape.Arrow -> PointerIcon.Default
        CursorShape.IBeam -> PointerIcon.Text
        CursorShape.Hand -> PointerIcon.Hand
        CursorShape.CrossHair -> PointerIcon.Crosshair
        CursorShape.ResizeNS -> PointerIcon(NativePointerIcon.TYPE_VERTICAL_DOUBLE_ARROW)
        CursorShape.ResizeEW -> PointerIcon(NativePointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW)
        CursorShape.ResizeAll -> PointerIcon(NativePointerIcon.TYPE_ALL_SCROLL)
        CursorShape.NotAllowed -> PointerIcon(NativePointerIcon.TYPE_NO_DROP)
    }
