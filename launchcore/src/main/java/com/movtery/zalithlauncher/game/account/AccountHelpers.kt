package com.movtery.zalithlauncher.game.account

import java.util.Objects

/**
 * Account 类型判定扩展（M1 自 AccountUtils 抽取，FQCN 不变——AccountUtils 留在 App 层）。
 */

fun Account.isAuthServerAccount(): Boolean {
    return !isLocalAccount() && !Objects.isNull(otherBaseUrl) && otherBaseUrl != "0"
}

fun Account.isMicrosoftAccount(): Boolean {
    return accountType == AccountType.MICROSOFT.tag
}

fun Account.isLocalAccount(): Boolean {
    return accountType == AccountType.LOCAL.tag
}

/**
 * 衣柜刷新钩子：Account 实体下载皮肤/披风后通知宿主刷新缓存。
 * 由 App 层的 AccountsManager 在初始化时注入（避免实体反向依赖管理器）。
 */
object WardrobeHooks {
    @Volatile
    var refreshWardrobe: (() -> Unit)? = null
}
