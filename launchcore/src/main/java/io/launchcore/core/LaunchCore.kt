package io.launchcore.core

import android.content.Context
import kotlinx.coroutines.flow.SharedFlow

/**
 * Entry point of the library. Create once per process:
 *
 * ```
 * val core = LaunchCore.create(context)
 * core.installer.install(InstallRequest(mcVersion = "1.21.1")).collect { ... }
 * val account = core.auth.offline("Steve")
 * val handle = core.launch(LaunchConfig(account = account, versionId = "1.21.1"))
 * ```
 *
 * UI glue (surface + input) lives in the optional `:launchcore-compose`
 * artifact; everything else is here.
 */
@UnstableApi
interface LaunchCore {
    val installer: Installer
    val auth: Auth
    val runtimePacks: RuntimePacks

    /** See [CoreEvent]. */
    val events: SharedFlow<CoreEvent>

    fun launch(config: LaunchConfig): LaunchHandle

    companion object {
        /**
         * Bound to the Zalith2-derived implementation during M2
         * (see PROJECT.md milestones). Placeholder until then.
         */
        @JvmStatic
        fun create(context: Context, config: LaunchCoreConfig = LaunchCoreConfig()): LaunchCore =
            throw NotImplementedError(
                "LaunchCore implementation is wired up in milestone M2; " +
                    "see PROJECT.md for the roadmap."
            )
    }
}
