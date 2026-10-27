package com.movtery.zalithlauncher.game.launch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 快速启动参数（M1 自 App 层 LauncherElements 抽取：纯启动数据，非 UI）
 */
@Parcelize
sealed interface QuickPlay : Parcelable {
    /** 快速启动游玩存档  仅支持 1.20+ 23w14a+ */
    @Parcelize
    data class Save(val saveName: String): QuickPlay

    /** 快速启动游玩服务器 */
    @Parcelize
    data class Server(val serverAddress: String): QuickPlay
}
