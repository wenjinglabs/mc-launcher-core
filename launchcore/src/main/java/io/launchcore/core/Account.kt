package io.launchcore.core

import java.util.UUID

/**
 * A player identity used to launch the game.
 *
 * The core never surfaces credentials to the UI layer; host apps only pass
 * [Account] values around (persist, select, launch).
 */
@UnstableApi
sealed interface Account {
    /** Stable player id (offline: derived UUID; Microsoft: Mojang UUID). */
    val id: String

    /** In-game player name. */
    val name: String

    /** Skin/accessory state is internal to the core; hosts only need id + name today. */
    val isMicrosoft: Boolean

    /** Offline account, no authentication involved. */
    data class Offline(
        override val name: String,
        override val id: String,
    ) : Account {
        override val isMicrosoft: Boolean get() = false
    }

    /** Microsoft account. Tokens are opaque to hosts; the core owns refresh logic. */
    data class Microsoft(
        override val id: String,
        override val name: String,
        val accessToken: String,
        val refreshToken: String,
        /** Epoch millis when [accessToken] stops working; the core refreshes proactively. */
        val expiresAtEpochMs: Long,
    ) : Account {
        override val isMicrosoft: Boolean get() = true
    }

    companion object {
        /**
         * Deterministic offline UUID, identical to vanilla's
         * `UUID.nameUUIDFromBytes("OfflinePlayer:<name>")` so skins/servers agree
         * with the rest of the ecosystem.
         */
        fun offlineUuid(name: String): String =
            UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray()).toString()

        fun offline(name: String): Offline = Offline(name, offlineUuid(name))
    }
}
