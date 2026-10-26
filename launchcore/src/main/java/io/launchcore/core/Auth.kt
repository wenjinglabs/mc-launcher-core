package io.launchcore.core

import android.content.Context

/**
 * Account management. The Microsoft flow is owned by the core (WebView /
 * custom-tab based); hosts only trigger it and receive an [Account].
 */
@UnstableApi
interface Auth {
    fun offline(name: String): Account.Offline

    /**
     * Runs the Microsoft device/web login flow.
     *
     * @param context an activity context (the core may need to start an intent).
     */
    suspend fun microsoft(context: Context): Account.Microsoft

    /** Refreshes tokens if expired; returns an equivalent account when still valid. */
    suspend fun refresh(account: Account.Microsoft): Account.Microsoft

    fun listSaved(): List<Account>

    suspend fun save(account: Account)

    suspend fun remove(account: Account)
}
