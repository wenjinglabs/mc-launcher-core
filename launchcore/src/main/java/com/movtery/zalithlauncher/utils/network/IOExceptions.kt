package com.movtery.zalithlauncher.utils.network

import java.io.InterruptedIOException
import java.net.SocketTimeoutException

/**
 * 判定异常是否为协程取消产生的中断 IO（超时不算）。
 * M1 自 NetWorkUtils 拆出，供核心库与宿主共用。
 */
fun Throwable.isInterruptedIOException(): Boolean {
    return this is InterruptedIOException && this !is SocketTimeoutException
}
