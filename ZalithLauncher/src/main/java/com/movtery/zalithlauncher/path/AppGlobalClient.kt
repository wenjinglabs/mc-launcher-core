package com.movtery.zalithlauncher.path

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

private fun isCurseForgeHost(host: String): Boolean =
    host == HOST_CURSEFORGE_API ||
            host == CURSEFORGE_CDN_SUFFIX ||
            host.endsWith(".$CURSEFORGE_CDN_SUFFIX")

/**
 * App 层的 ktor 全局客户端（ADR-13：ktor 只属于 App，core 不携带）。
 * M1 自 core 的 UrlManager 原样迁出，供 App 侧现有 ktor 调用方使用。
 */
val GLOBAL_CLIENT = HttpClient(OkHttp) {
    install(HttpTimeout) {
        requestTimeoutMillis = TIME_OUT
    }
    install(ContentNegotiation) {
        json(GLOBAL_JSON)
    }
    expectSuccess = true

    defaultRequest {
        header(HttpHeaders.UserAgent, URL_USER_AGENT)
    }
    engine {
        // 使用内置的 OkHttp 客户端
        preconfigured = createOkHttpClientBuilder().build()
    }
}.apply {
    requestPipeline.intercept(HttpRequestPipeline.State) {
        if (isCurseForgeHost(context.url.host)) {
            val apiKey = NetIdentity.curseforgeApiKey()
            if (apiKey.isNotBlank()) {
                context.header("x-api-key", apiKey)
            }
        }
    }
}
