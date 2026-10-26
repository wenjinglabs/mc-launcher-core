package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlin.coroutines.CoroutineContext

/**
 * App 层的 ktor HTTP 助手（ADR-13：ktor 只属于 App）。
 * M1 自 core 的 NetWorks.kt 原样迁出，保持 FQCN 与签名不变，App 现有调用方零改动。
 * core 侧等价能力见 launchcore 的 HttpJson.kt（OkHttp + kotlinx-serialization）。
 */
@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> HttpResponse.safeBodyAsJson(): T {
    return GLOBAL_JSON.decodeFromStream(bodyAsChannel().toInputStream())
}

suspend fun HttpResponse.safeBodyAsText(): String {
    val channel = bodyAsChannel()
    return channel.toInputStream().bufferedReader().use { reader ->
        val buffer = CharArray(8 * 1024)
        val sb = StringBuilder()
        var read: Int
        while (reader.read(buffer).also { read = it } != -1) {
            sb.appendRange(buffer, 0, read)
        }
        sb.toString()
    }
}

suspend inline fun <reified T> submitForm(
    url: String,
    parameters: Parameters,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    GLOBAL_CLIENT.submitForm(
        url = url,
        formParameters = parameters
    ) {
        contentType(ContentType.Application.FormUrlEncoded)
    }.body()
}

suspend inline fun <reified T> httpPostJson(
    url: String,
    headers: List<Pair<String, Any?>>? = null,
    body: Any,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    GLOBAL_CLIENT.post(url) {
        contentType(ContentType.Application.Json)
        headers?.takeIf { it.isNotEmpty() }?.forEach { (k, v) -> header(k, v) }
        setBody(body)
    }.safeBodyAsJson()
}

suspend inline fun <reified T> httpGetJson(
    url: String,
    headers: List<Pair<String, Any?>>? = null,
    parameters: Parameters? = null,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    GLOBAL_CLIENT.get(url) {
        headers?.takeIf { it.isNotEmpty() }?.forEach { (k, v) -> header(k, v) }
        parameters?.let { value ->
            url {
                this.parameters.appendAll(value)
            }
        }
    }.safeBodyAsJson()
}
