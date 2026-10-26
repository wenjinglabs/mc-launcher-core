package com.movtery.zalithlauncher.path

/**
 * 网络层的宿主注入点（模式同 [com.movtery.zalithlauncher.utils.logging.LoggerConfig]）：
 * 核心库不携带品牌与密钥——UA 标识与 CurseForge API Key 由宿主在初始化时注入。
 */
object NetIdentity {
    /** User-Agent 标识，如 "MyLauncher/Android_1.2.3" */
    @Volatile
    var userAgent: String = "LaunchCore/Android"

    /** CurseForge API Key 提供者；返回空白字符串表示未配置（请求将不带 x-api-key） */
    @Volatile
    var curseforgeApiKey: () -> String = { "" }
}
