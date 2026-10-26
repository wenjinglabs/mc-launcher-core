package io.launchcore.core

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** Everything needed to start one game session. */
@UnstableApi
data class LaunchConfig(
    val account: Account,
    /** Id of an [InstalledVersion] (see [Installer.listInstalled]). */
    val versionId: String,
    val renderer: Renderer = Renderer.Angle,
    /** Appended to the JVM command line, e.g. "-Xmx2G". */
    val jvmArgs: List<String> = emptyList(),
    /** Overrides the per-version default game directory when set. */
    val gameDirectory: File? = null,
    /** Extra environment entries handed to the game process. */
    val extraEnv: Map<String, String> = emptyMap(),
)

/** Lifecycle of a game session, observed via [LaunchHandle.state]. */
@UnstableApi
sealed interface LaunchState {
    /** Runtime packs / JVM image being prepared before the JVM starts. */
    data object PreparingRuntime : LaunchState

    /** JVM and game thread are starting up. */
    data object Launching : LaunchState

    /** The game reached its main loop and is rendering. */
    data object GameRunning : LaunchState

    /** The game terminated abnormally; [logTail] carries the last lines for reporting. */
    data class Crashed(val logTail: String?) : LaunchState

    /** The game exited normally with the given process exit code. */
    data class Exited(val exitCode: Int) : LaunchState
}

/**
 * A running (or finished) game session.
 *
 * The game renders into a Surface that the host provides — see the optional
 * `:launchcore-compose` artifact for a ready-made `MinecraftSurface`.
 */
@UnstableApi
interface LaunchHandle {
    val state: StateFlow<LaunchState>

    /** Game stdout/stderr lines, also the assertion surface for headless E2E tests. */
    val logs: SharedFlow<String>

    /** Asks the game to shut down gracefully. */
    fun requestExit()

    /** Forces the JVM down; use after [requestExit] when the host needs a hard stop. */
    fun kill()
}
