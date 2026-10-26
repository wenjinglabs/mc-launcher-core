package io.launchcore.core

import java.io.File

/** Which mod loader to install alongside the game, if any. */
@UnstableApi
data class ModLoaderSpec(
    val kind: Kind,
    /** Exact loader version, or null for "latest compatible with [InstallRequest.mcVersion]". */
    val version: String? = null,
) {
    enum class Kind { FABRIC, FORGE, NEOFORGE, QUILT }
}

/** What to download and install. */
@UnstableApi
data class InstallRequest(
    /** Minecraft version id, e.g. "1.21.1". */
    val mcVersion: String,
    val modLoader: ModLoaderSpec? = null,
)

/** A version present in the managed game directory. */
@UnstableApi
data class InstalledVersion(
    /** Unique id of the installed profile (may differ from [mcVersion] for loader installs). */
    val id: String,
    val mcVersion: String,
    val modLoader: ModLoaderSpec?,
    val gameDirectory: File,
)

/** Streamed progress of [Installer.install]. */
@UnstableApi
sealed interface InstallEvent {
    enum class Phase { MANIFEST, RUNTIME_PACK, LIBRARIES, ASSETS, MOD_LOADER, VERIFY }

    data class Progress(
        val phase: Phase,
        val currentFile: String?,
        val downloadedBytes: Long,
        /** Null when the server does not report a content length. */
        val totalBytes: Long?,
    ) : InstallEvent

    data class Completed(val versionId: String) : InstallEvent

    data class Failed(
        val cause: Throwable,
        /** True when simply retrying (same request) may succeed, e.g. network errors. */
        val retriable: Boolean,
    ) : InstallEvent
}
