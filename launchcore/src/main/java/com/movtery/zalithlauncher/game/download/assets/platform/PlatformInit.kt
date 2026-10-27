package com.movtery.zalithlauncher.game.download.assets.platform

/**
 * 平台版本列表初始化扩展（M1 自 App 层 DownloadAssetsElements 抽取：纯逻辑）
 */
suspend fun <E: PlatformVersion> List<E>.initAllGeneric(
    currentProjectId: String,
    also: suspend (E) -> Unit = {}
): List<E> {
    return mapNotNull { version ->
        if (!version.initFile(currentProjectId)) return@mapNotNull null
        version.also {
            also(it)
        }
    }.sortedByDescending {
        //排序：最新的版本在前
        it.platformDatePublished()
    }
}

/**
 * 初始化全部版本数据，并筛选出成功初始化的所有版本
 */
suspend fun List<PlatformVersion>.initAll(
    currentProjectId: String,
    also: suspend (PlatformVersion) -> Unit = {}
): List<PlatformVersion> {
    return initAllGeneric<PlatformVersion>(currentProjectId, also)
}
