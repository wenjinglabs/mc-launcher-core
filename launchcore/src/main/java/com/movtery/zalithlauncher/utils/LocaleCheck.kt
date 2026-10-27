package com.movtery.zalithlauncher.utils

import android.content.Context
import java.util.Locale

/**
 * 中文环境判定（M1 自 App 层 LocalUtils 抽取）
 */
/**
 * 检查当前环境是否为中文环境
 */
fun isChinese(context: Context? = null): Boolean {
    //检查默认区域设置
    val defaultLocale = Locale.getDefault()
    if (isChineseLocale(defaultLocale)) {
        return true
    }

    //检查实际区域设置
    val resources = context?.resources
    return resources != null && isChineseLocale(resources.configuration.locales[0])
}

/**
 * 判断单个Locale是否为中文环境
 */
fun isChineseLocale(locale: Locale): Boolean {
    val language = locale.language.lowercase(Locale.ROOT)
    if (language != "zh") return false

    val country = locale.country.uppercase(Locale.ROOT)
    return country in setOf(
        "CN", //中国大陆
        "TW", //台湾
        "HK", //香港
        "MO", //澳门
        "SG", //新加坡
        "MY"  //马来西亚
    )
}
