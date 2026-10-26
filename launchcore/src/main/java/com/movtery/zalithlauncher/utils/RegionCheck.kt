package com.movtery.zalithlauncher.utils

import java.util.TimeZone

/**
 * 是否为中国大陆时区（按系统时区判断，与语言无关）。
 * M1 自 LocalUtils 拆出，供 RegionRestriction 与宿主共用。
 */
fun isChinaMainland(): Boolean {
    val timeZone = TimeZone.getDefault()

    if (
        timeZone.id in listOf(
            "Asia/Shanghai",
            "Asia/Chongqing",//历史遗留
            "Asia/Urumqi"
        )
    ) return true

    val offsetMillis = timeZone.getOffset(System.currentTimeMillis())
    val isUtcPlus8 = offsetMillis == 8 * 60 * 60 * 1000

    if (!isUtcPlus8) return false

    //应用内支持修改语言，不能再以语言来进行判断
    return /*Locale.getDefault().country.equals("CN", ignoreCase = true)*/false
}
