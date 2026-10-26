package com.movtery.zalithlauncher.utils

import com.google.gson.GsonBuilder

/**
 * 全局 Gson 实例（M1 自 App 层 LocalUtils 抽取，FQCN 不变）
 */
val GSON = GsonBuilder().setPrettyPrinting().create()
