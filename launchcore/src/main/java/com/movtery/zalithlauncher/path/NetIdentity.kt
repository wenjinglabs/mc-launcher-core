package com.movtery.zalithlauncher.path

/**
 * 网络层的宿主注入点（模式同 [com.movtery.zalithlauncher.utils.logging.LoggerConfig]）：
 * 核心库不携带品牌与密钥——UA 标识与 CurseForge API Key 由宿主在初始化时注入。
 */
object NetIdentity {
    /** User-Agent 标识，如 "MyLauncher/Android_1.2.3" */
    @Volatile
    var userAgent: String = "LaunchCore/Android"

    /** 启动器品牌名（导出文件元数据等品牌场景） */
    @Volatile
    var launcherName: String = "LaunchCore"

    /** 启动器内部标识（版本内品牌目录名等；宿主注入以延续既有安装的目录名） */
    @Volatile
    var launcherIdentifier: String = "LaunchCore"

    /** 启动器版本号（版本自定义信息模板变量等） */
    @Volatile
    var launcherVersion: String = "unknown"

    /** CurseForge API Key 提供者；返回空白字符串表示未配置（请求将不带 x-api-key） */
    @Volatile
    var curseforgeApiKey: () -> String = { "" }
}
