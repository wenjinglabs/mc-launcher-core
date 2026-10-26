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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_LINK
import com.movtery.zalithlauncher.game.download.engine.DownloadEngine
import com.movtery.zalithlauncher.game.download.engine.DownloadRequest
import com.movtery.zalithlauncher.path.DOWNLOAD_OKHTTP_CLIENT
import com.movtery.zalithlauncher.path.TIME_OUT
import com.movtery.zalithlauncher.path.createRequestBuilder
import com.movtery.zalithlauncher.ui.theme.showThemed
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
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
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "NetWorkUtils"

/** 单个文件下载的最大允许时间 */
private const val DOWNLOAD_PER_FILE_TIMEOUT = 3 * 60 * 1000L

/** 多候选源下载的最大允许时间 */
private const val DOWNLOAD_SOURCES_TIMEOUT = 5 * 60 * 1000L

/**
 * @return 当前网络是否可用
 */
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}

/**
 * @return 当前是否正在使用移动网络
 */
fun isUsingMobileData(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
}

/**
 * 下载与取文本助手已迁移至核心库（utils/network/DownloadHelpers.kt），FQCN 不变。
 */

/**
 * 展示一个提示弹窗，告知用户接下来将要在浏览器内访问的链接，用户可以选择不进行访问
 * @param link 要访问的链接
 */
fun Activity.openLink(link: String) {
    this.openLink(link, null)
}

/**
 * 展示一个提示弹窗，告知用户接下来将要在浏览器内访问的链接，用户可以选择不进行访问
 * @param link 要访问的链接
 * @param dataType 设置 intent 的数据以及显式 MIME 数据类型
 */
fun Activity.openLink(link: String, dataType: String?) {
    if (link.isEmptyOrBlank()) {
        return
    }

    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.generic_open_link)
        .setMessage(link)
        .setPositiveButton(R.string.generic_confirm) { _, _ ->
            openLinkInternal(link, dataType)
        }
        .setNegativeButton(R.string.generic_cancel) { dialog, _ ->
            dialog.dismiss()
        }
        .setNeutralButton(R.string.generic_copy) { dialog, _ ->
            copyText(COPY_LABEL_LINK, link, this)
            dialog.dismiss()
        }
        .showThemed()
}

/**
 * 直接在浏览器打开指定链接
 */
fun Activity.openLinkInternal(link: String, dataType: String? = null) {
    try {
        val uri = link.toUri()
        val browserIntent = if (dataType != null) {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, dataType)
            }
        } else {
            Intent(Intent.ACTION_VIEW, uri)
        }
        startActivity(browserIntent)
    } catch (e: Exception) {
        Logger.warning(TAG, "Failed to open link: $link", e)
    }
}