package com.movtery.zalithlauncher.ui

import android.content.Context
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * 富文本变体（M1 自 AndroidStringText 拆分：Compose 类型留在 App 层）
 *
 * 展示带有样式的富文本
 *
 * @property value [AnnotatedString] 内容
 */
data class AnnotatedText(val value: AnnotatedString) : AndroidStringText

/**
 * 创建富文本变体实例
 */
fun androidText(annotated: AnnotatedString) = AnnotatedText(annotated)

/**
 * DSL 追加富文本（Compose 类型留在 App 层的扩展重载）
 */
fun AndroidStringTextBuilder.append(text: AnnotatedString) {
    append(androidText(text))
}

/**
 * 将 [AndroidStringText] 解析为 [AnnotatedString]
 */
@Composable
fun resolveAndroidString(text: AndroidStringText): AnnotatedString {
    return when (text) {
        is AndroidStringText.Text -> AnnotatedString(text.value)
        is AnnotatedText -> text.value
        is AndroidStringText.StringRes -> {
            val args = text.args
            AnnotatedString(
                if (args == null) {
                    stringResource(text.key)
                } else {
                    val resolvedArgs = args.map { arg ->
                        if (arg is AndroidStringText) {
                            resolveAndroidString(arg).text
                        } else {
                            arg
                        }
                    }.toTypedArray()
                    stringResource(text.key, *resolvedArgs)
                }
            )
        }
        is AndroidStringText.Appended -> {
            buildAnnotatedString {
                text.texts.forEach {
                    append(resolveAndroidString(it))
                }
            }
        }
        else -> AnnotatedString("")
    }
}

/**
 * 在非 Composable 环境中将 [AndroidStringText] 解析为 [String]
 *
 * @param context Android [Context]，用于加载 [AndroidStringText.StringRes] 类型的字符串资源
 */
fun AndroidStringText.toAndroidString(context: Context): String = when (this) {
    is AndroidStringText.Text -> value
    is AnnotatedText -> value.toString()
    is AndroidStringText.StringRes -> {
        val args = args
        if (args == null) {
            context.getString(key)
        } else {
            val resolvedArgs = args.map { arg ->
                if (arg is AndroidStringText) {
                    arg.toAndroidString(context)
                } else {
                    arg
                }
            }.toTypedArray()
            context.getString(key, *resolvedArgs)
        }
    }
    is AndroidStringText.Appended -> texts.joinToString("") { it.toAndroidString(context) }
    else -> ""
}

/**
 * 用于展示 [AndroidStringText] 的 Composable 组件
 *
 * 该组件是对 [Text] 的封装，能够根据 [AndroidStringText] 的具体类型
 * 自动选择合适的方式进行渲染
 */
@Composable
fun AndroidStringText(
    text: AndroidStringText,
    modifier: Modifier = Modifier,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = resolveAndroidString(text),
        modifier = modifier,
        autoSize = autoSize,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        style = style,
    )
}
