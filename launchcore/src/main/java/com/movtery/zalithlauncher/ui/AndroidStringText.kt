package com.movtery.zalithlauncher.ui

import androidx.annotation.StringRes

/**
 * Android 可展示字符串文本代理接口，提供不同的文本展现方式。
 *
 * M1 迁移注记：本文件自 App 层拆分而来——模型（Text/StringRes/Appended 与工厂、DSL）
 * 属于核心库；Compose 渲染器与富文本变体留在 App 层
 * （ui/AndroidStringTextCompose.kt）。接口为非 sealed：宿主可自行扩展变体。
 */
interface AndroidStringText {
    /**
     * 直接展示普通字符串
     *
     * @property value 字符串内容
     */
    data class Text(val value: String) : AndroidStringText

    /**
     * 通过 Android 资源 ID 加载字符串，支持格式化参数
     *
     * @property key 字符串资源 ID
     * @property args 格式化参数，支持 [AndroidStringText]
     */
    data class StringRes(
        @field:androidx.annotation.StringRes
        val key: Int,
        val args: Array<out Any>?
    ) : AndroidStringText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringRes

            if (key != other.key) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }

    /**
     * 拼接多个 [AndroidStringText] 实例
     *
     * @property texts 要拼接的字符串列表
     */
    data class Appended(
        val texts: List<AndroidStringText>
    ) : AndroidStringText
}

/**
 * 创建 [AndroidStringText.Text] 实例
 *
 * @param string 字符串内容
 */
fun androidText(string: String) = AndroidStringText.Text(string)

/**
 * 创建 [AndroidStringText.StringRes] 实例
 *
 * @param key 字符串资源 ID
 */
fun androidText(@StringRes key: Int) = AndroidStringText.StringRes(key, null)

/**
 * 创建带有格式化参数的 [AndroidStringText.StringRes] 实例
 *
 * @param key 字符串资源 ID
 * @param args 格式化参数
 */
fun androidText(
    @StringRes
    key: Int,
    vararg args: Any
) = AndroidStringText.StringRes(key, args)

/**
 * 创建 [AndroidStringText.Appended] 实例
 */
fun androidText(vararg texts: AndroidStringText) = AndroidStringText.Appended(texts.toList())


/**
 * 使用 DSL 构建 [AndroidStringText.Appended] 实例
 */
inline fun buildAppendedText(
    block: AndroidStringTextBuilder.() -> Unit
): AndroidStringText = AndroidStringTextBuilder().apply(block).build()

@DslMarker
private annotation class AndroidStringTextDsl
@AndroidStringTextDsl
class AndroidStringTextBuilder {
    private val texts = mutableListOf<AndroidStringText>()
    /**
     * 追加普通字符串
     */
    fun append(text: String) {
        texts.add(AndroidStringText.Text(text))
    }
    /**
     * 通过 Android 资源 ID 加载字符串
     */
    fun append(@StringRes resId: Int) {
        texts.add(AndroidStringText.StringRes(resId, null))
    }
    /**
     * 通过 Android 资源 ID 加载字符串，支持格式化参数
     */
    fun append(@StringRes resId: Int, vararg args: Any) {
        texts.add(AndroidStringText.StringRes(resId, args))
    }
    /**
     * 追加另一个字符串代理对象
     */
    fun append(other: AndroidStringText) {
        texts.add(other)
    }
    /**
     * 构建 [AndroidStringText.Appended] 实例
     */
    fun build(): AndroidStringText = AndroidStringText.Appended(texts.toList())
}
