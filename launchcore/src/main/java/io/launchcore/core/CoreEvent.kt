package io.launchcore.core

import kotlinx.coroutines.flow.SharedFlow

/**
 * Non-fatal occurrences the host UI may want to surface (toasts, banners, log
 * panels...). The core never shows UI itself; it only publishes here.
 */
@UnstableApi
sealed interface CoreEvent {
    enum class Severity { INFO, WARNING, ERROR }

    data class Notice(
        val message: String,
        val severity: Severity = Severity.INFO,
    ) : CoreEvent

    /** A download source failed and the core moved to the next mirror. */
    data class MirrorFallback(
        val failedSource: String,
        val usingSource: String,
    ) : CoreEvent

    /** A required runtime pack is missing and was not auto-downloadable. */
    data class RuntimePackMissing(
        val pack: RuntimePacks.PackId,
    ) : CoreEvent
}
