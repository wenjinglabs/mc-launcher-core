package io.launchcore.core

import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(UnstableApi::class)
class AccountTest {
    @Test
    fun `offline uuid matches vanilla derivation`() {
        // Vanilla derives offline UUIDs from "OfflinePlayer:<name>"; the ecosystem
        // (skins, offline servers) depends on this exact value.
        assertEquals(
            "b50ad385-829d-3141-a216-7e7d7539ba7f",
            Account.offlineUuid("Notch"),
        )
    }

    @Test
    fun `offline factory derives id from name`() {
        val account = Account.offline("Steve")
        assertEquals("Steve", account.name)
        assertEquals(Account.offlineUuid("Steve"), account.id)
    }
}
