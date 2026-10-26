package io.launchcore.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Installs game versions (and their runtime packs) into the core-managed
 * game directory.
 */
@UnstableApi
interface Installer {
    /**
     * Cold flow: collecting it starts the install; cancelling the collector
     * cancels the install (partial downloads are resumed on retry).
     */
    fun install(request: InstallRequest): Flow<InstallEvent>

    fun listInstalled(): List<InstalledVersion>

    fun remove(versionId: String)
}
