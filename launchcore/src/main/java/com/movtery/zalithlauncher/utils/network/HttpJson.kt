package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.path.createOkHttpClientBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

/**
 * core 侧共享的 API 调用客户端（含 UA / CurseForge 拦截器，见 UrlManager）。
 */
val API_CLIENT: OkHttpClient by lazy { createOkHttpClientBuilder().build() }

/** 非 2xx 响应；[code] 供重试策略判断（5xx 可重试） */
class HttpException(val code: Int, message: String) : IOException("HTTP $code: $message")

@PublishedApi
internal val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

@PublishedApi
internal fun encodeQuery(query: List<Pair<String, String>>): String =
    query.joinToString("&") { (k, v) ->
        "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
    }

@PublishedApi
internal fun buildUrl(url: String, query: List<Pair<String, String>>?): String {
    if (query.isNullOrEmpty()) return url
    return buildString {
        append(url)
        append(if (url.contains('?')) '&' else '?')
        append(encodeQuery(query))
    }
}

/**
 * OkHttp + kotlinx-serialization 的极简 JSON API 层（ADR-13：core 单一 HTTP 栈，无 ktor）。
 *
 * 非 2xx 抛 [HttpException]；网络错误抛 OkHttp 原生异常；重试请配合 [withRetry]。
 */
suspend inline fun <reified T> fetchJson(
    url: String,
    headers: List<Pair<String, String>>? = null,
    query: List<Pair<String, String>>? = null,
    json: Json = GLOBAL_JSON,
    client: OkHttpClient = API_CLIENT,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    val builder = Request.Builder().url(buildUrl(url, query)).header("Accept", "application/json")
    headers?.forEach { (k, v) -> builder.header(k, v) }
    client.newCall(builder.build()).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw HttpException(response.code, body.take(200))
        json.decodeFromString(body)
    }
}

/**
 * POST JSON 并解析响应。[body] 经 [json] 序列化。
 */
suspend inline fun <reified R, reified B> postJson(
    url: String,
    body: B,
    headers: List<Pair<String, String>>? = null,
    json: Json = GLOBAL_JSON,
    client: OkHttpClient = API_CLIENT,
    context: CoroutineContext = Dispatchers.IO
): R = withContext(context) {
    val builder = Request.Builder().url(url).post(json.encodeToString(body).toRequestBody(JSON_MEDIA_TYPE))
    headers?.forEach { (k, v) -> builder.header(k, v) }
    client.newCall(builder.build()).execute().use { response ->
        val bodyText = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw HttpException(response.code, bodyText.take(200))
        json.decodeFromString(bodyText)
    }
}

/**
 * POST 表单并解析 JSON 响应（application/x-www-form-urlencoded）。
 */
suspend inline fun <reified T> postForm(
    url: String,
    form: List<Pair<String, String>>,
    headers: List<Pair<String, String>>? = null,
    json: Json = GLOBAL_JSON,
    client: OkHttpClient = API_CLIENT,
    context: CoroutineContext = Dispatchers.IO
): T = withContext(context) {
    val formBody = FormBody.Builder().apply { form.forEach { (k, v) -> add(k, v) } }.build()
    val builder = Request.Builder().url(url).post(formBody)
    headers?.forEach { (k, v) -> builder.header(k, v) }
    client.newCall(builder.build()).execute().use { response ->
        val bodyText = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw HttpException(response.code, bodyText.take(200))
        json.decodeFromString(bodyText)
    }
}

/**
 * GET 文本响应（非 JSON 场景）。
 */
suspend fun fetchText(
    url: String,
    headers: List<Pair<String, String>>? = null,
    client: OkHttpClient = API_CLIENT,
    context: CoroutineContext = Dispatchers.IO
): String = withContext(context) {
    val builder = Request.Builder().url(url)
    headers?.forEach { (k, v) -> builder.header(k, v) }
    client.newCall(builder.build()).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw HttpException(response.code, body.take(200))
        body
    }
}
