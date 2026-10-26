package com.movtery.zalithlauncher.utils.logging

import java.io.File

/**
 * Logger 的宿主侧配置。核心库不依赖构建插件、路径管理器或设置存储——
 * 这些决定权由宿主 App 在初始化时注入。
 */
class LoggerConfig(
    /** 日志文件写入与清理的目录 */
    val logsDir: File,
    /** 日志保留天数；每次清理时读取，宿主可接自己的设置项 */
    val retentionDays: () -> Int = { 7 },
    /** 日志横幅标识（替代对构建插件的依赖） */
    val banner: String = "Log",
    /** 启动时输出的环境信息（设备/版本等，由宿主提供） */
    val printEnvironmentInfo: ((String) -> Unit) -> Unit = { },
)
