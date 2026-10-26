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

package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "NetWorks"

/**
 * 带指数退避的重试。协程取消不重试；5xx / DNS 失败 / IO 错误可重试。
 *
 * M1 迁移注记：原文件中的 ktor 助手（safeBodyAsJson/submitForm/httpGetJson 等）
 * 已随 ADR-13 移回 App 层 KtorHttp.kt；core 统一走 [fetchJson]（HttpJson.kt）。
 */
suspend fun <T> withRetry(
    logTag: String,
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10_000,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    var retryCount = 0
    var lastError: Throwable? = null

    while (retryCount < maxRetries) {
        try {
            return block()
        } catch (e: CancellationException) {
            //协程被取消时不重试，直接抛出
            Logger.debug(TAG, "$logTag: Cancelled: ${e.message}")
            throw e
        } catch (e: Exception) {
            // 部分异常（如 UnresolvedAddressException）message 为 null，需要输出异常类型以便诊断
            Logger.debug(TAG, "$logTag: Attempt ${retryCount + 1} failed: ${e::class.simpleName}: ${e.message}")
            lastError = e
            if (canRetry(e)) {
                delay(currentDelay.milliseconds)
                currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
                retryCount++
            } else {
                throw e //不可重试
            }
        }
    }
    throw lastError ?: Exception("Failed after $maxRetries retries")
}

private fun canRetry(e: Exception): Boolean {
    return when (e) {
        is HttpException -> e.code in 500..599 //5xx错误可重试
        is UnresolvedAddressException -> true // DNS解析失败，可能是临时性故障
        is IOException -> true //网络错误
        else -> false
    }
}
