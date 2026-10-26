package com.movtery.zalithlauncher.utils.image

import android.graphics.Bitmap
import androidx.core.graphics.get

/**
 * M1 自 App 层 ImageUtils 抽取（皮肤/披风处理需要），FQCN 不变。
 */

/**
 * 遍历指定范围的像素，判断是否满足颜色谓词
 *
 * @param xRange X 轴坐标范围
 * @param yRange Y 轴坐标范围
 * @param predicate 谓词函数，参数为 (颜色值, x, y)
 * @param requireAll 是否要求所有像素都满足谓词，默认为 false (任一满足即返回 true)
 * @return 是否满足条件
 */
inline fun Bitmap.isColorMatch(
    xRange: IntRange,
    yRange: IntRange,
    predicate: (color: Int, x: Int, y: Int) -> Boolean,
    requireAll: Boolean = false
): Boolean {
    val width = this.width
    val height = this.height

    for (x in xRange) {
        if (x !in 0 until width) continue
        for (y in yRange) {
            if (y !in 0 until height) continue
            val match = predicate(this[x, y], x, y)
            if (requireAll) {
                if (!match) return false
            } else {
                if (match) return true
            }
        }
    }
    return requireAll
}

/**
 * 如果 Bitmap 大于指定阈值，则回收它
 */
fun Bitmap?.recycleIfLarge(thresholdBytes: Int = 8 * 1024 * 1024) {
    this ?: return

    if (!isRecycled && byteCount >= thresholdBytes) {
        recycle()
    }
}
