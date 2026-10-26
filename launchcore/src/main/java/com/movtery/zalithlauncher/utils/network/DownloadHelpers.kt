package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.game.download.engine.DownloadEngine
import com.movtery.zalithlauncher.game.download.engine.DownloadRequest
import com.movtery.zalithlauncher.path.TIME_OUT
import com.movtery.zalithlauncher.path.DOWNLOAD_OKHTTP_CLIENT
import com.movtery.zalithlauncher.path.createRequestBuilder
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "DownloadHelpers"

/** 单文件下载整体超时 */
internal const val DOWNLOAD_PER_FILE_TIMEOUT = 3 * 60 * 1000L
/** 多源下载整体超时 */
internal const val DOWNLOAD_SOURCES_TIMEOUT = 5 * 60 * 1000L

/**
 * 下载与取文本助手（M1 自 App 层 NetWorkUtils 抽取，FQCN 不变；NetWorkUtils 保留 App 侧 UI 邻接函数）。
 */

/**
 * 下载单个文件到本地（动态分块、自动换源、降级重试）
 * @throws TimeoutException 整体超时
 */
suspend fun downloadFile(
    url: String,
    outputFile: File,
    sha1: String? = null,
    sizeCallback: (Long) -> Unit = {}
): Unit = withTimeout(DOWNLOAD_PER_FILE_TIMEOUT.milliseconds) {
    DownloadEngine.download(DownloadRequest(listOf(url), outputFile, sha1), sizeCallback = sizeCallback)
}

/**
 * 按优先级从多个候选源下载单个文件，失败时自动沿源列表轮转
 * @throws TimeoutException 整体超时
 */
suspend fun downloadFileFromSources(
    urls: List<String>,
    outputFile: File,
    sha1: String? = null,
    sizeCallback: (Long) -> Unit = {}
): Unit = withTimeout(DOWNLOAD_SOURCES_TIMEOUT.milliseconds) {
    DownloadEngine.download(DownloadRequest(urls, outputFile, sha1), sizeCallback = sizeCallback)
}

/**
 * 速率监测报告
 * @param onSpeedReport 在1秒延迟后汇报期间的数据量，单位：bytes
 */
suspend fun <T> withSpeedReport(
    onSpeedReport: (Long) -> Unit,
    onClear: () -> Unit = {},
    block: suspend (onBytesWritten: (Long) -> Unit) -> T
): T = coroutineScope {
    val bytesWritten = AtomicLong(0L)

    withSpeedReport(
        onTimeReport = {
            val currentBytes = bytesWritten.getAndSet(0L)
            //下载引擎的回滚增量可能使单秒采样为负，速率不应为负
            onSpeedReport(currentBytes.coerceAtLeast(0L))
        },
        onClear = {
            bytesWritten.set(0L)
            onClear()
        },
        block = {
            block { bytes ->
                bytesWritten.addAndGet(bytes)
            }
        }
    )
}

/**
 * 速率监测报告
 * @param onTimeReport 在1秒延迟后调用，可在此期间汇报
 */
suspend fun <T> withSpeedReport(
    onTimeReport: () -> Unit,
    onClear: () -> Unit,
    block: suspend () -> T
): T = coroutineScope {
    var reportJob: Job? = null

    try {
        onClear()
        reportJob = launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000L.milliseconds)
                onTimeReport()
            }
        }

        block()
    } finally {
        reportJob?.cancelAndJoin()
        onClear()
    }
}

/**
 * 同步获取 URL 返回的字符串内容
 * @param url 要请求的URL地址
 * @return 服务器返回的字符串内容
 * @throws IllegalArgumentException 当URL无效时
 * @throws IOException 当网络请求失败或响应解析失败时
 */
@Throws(IOException::class, IllegalArgumentException::class)
suspend fun fetchStringFromUrl(url: String): String = withContext(Dispatchers.IO) {
    try {
        withTimeout(TIME_OUT.milliseconds) {
            runInterruptible {
                DOWNLOAD_OKHTTP_CLIENT.newCall(createRequestBuilder(url).build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code} - ${response.message}")
                    }

                    response.body.use { it.string() }
                }
            }
        }
    } catch (_: TimeoutCancellationException) {
        throw TimeoutException("Request timed out after ${TIME_OUT}ms: $url")
    }
}

/**
 * 同步获取 URL 返回的字符串内容
 * @param urls 要请求的URL源地址
 * @return 服务器返回的字符串内容
 * @throws IllegalArgumentException 当URL无效时
 * @throws IOException 当网络请求失败或响应解析失败时
 */
@Throws(IOException::class, IllegalArgumentException::class)
suspend fun fetchStringFromUrls(urls: List<String>): String = withContext(Dispatchers.IO) {
    var result: String? = null
    var succeed = false
    var lastException: Throwable? = null

    loop@ for (url in urls) {
        runCatching {
            result = fetchStringFromUrl(url)
            succeed = true
            break@loop
        }.onFailure { th ->
            if (th is CancellationException || th.isInterruptedIOException()) throw th
            Logger.debug(TAG, "Source $url failed!", th)
            lastException = th
        }
    }

    if (!succeed || result == null) throw lastException ?: IOException("Failed to retrieve information from the source!")

    result
}
