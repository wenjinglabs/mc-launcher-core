package com.movtery.zalithlauncher.utils

/**
 * 存储权限状态镜像（M1：状态由宿主 App 计算后推送，核心库只读）。
 */
object StoragePermissionState {
    /** 宿主声明了能处理 SAF 目录授权的 Activity */
    @Volatile
    var canHandlePermission: Boolean = false

    /** 已获得（或不需要）存储权限 */
    @Volatile
    var hasStoragePermission: Boolean = false
}
