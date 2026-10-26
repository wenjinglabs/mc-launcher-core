package io.launchcore.core

import kotlinx.coroutines.flow.StateFlow

/**
 * The heavyweight binaries (JREs, renderers, LWJGL) are NOT bundled in the
 * AAR; they are downloaded on demand as versioned packs with sha256
 * verification and resumable downloads.
 *
 * Sources are tried in order; hosts may prepend their own mirrors (server
 * owners pointing at their own CDN). The built-in defaults are
 * GitHub Releases (authoritative) plus a China mirror.
 */
@UnstableApi
interface RuntimePacks {
    enum class PackId {
        JRE_8, JRE_17, JRE_21, JRE_25,
        RENDERER_ANGLE, RENDERER_GL4ES, RENDERER_ZINK,
        LWJGL_3_3, LWJGL_3_4,
        AWT_ADAPTER,
    }

    sealed interface PackState {
        data object NotDownloaded : PackState
        data class Downloading(val downloadedBytes: Long, val totalBytes: Long?) : PackState
        data class Ready(val localPath: String) : PackState
        data object Corrupted : PackState
    }

    val status: StateFlow<Map<PackId, PackState>>

    /**
     * Replaces the source list. Earlier entries win; the built-in defaults are
     * appended automatically when not present.
     */
    fun configureSources(vararg urls: String)

    /** Downloads a pack ahead of time (no-op when already [PackState.Ready]). */
    suspend fun prefetch(pack: PackId)
}
