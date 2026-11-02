package com.vaultdraft.data.prefs

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vaultdraft_prefs")

class VaultPreferences(private val context: Context) {

    val dataStore: DataStore<Preferences> get() = context.dataStore

    val vaultUri: Flow<Uri?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.VAULT_URI]?.let { Uri.parse(it) }
    }

    val routesJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ROUTES_JSON]
    }

    val defaultRouteId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DEFAULT_ROUTE_ID]
    }

    suspend fun saveVaultUri(uri: Uri) {
        context.dataStore.edit { it[PreferencesKeys.VAULT_URI] = uri.toString() }
    }

    suspend fun clearVaultUri() {
        context.dataStore.edit { it.remove(PreferencesKeys.VAULT_URI) }
    }

    suspend fun saveRoutesJson(json: String) {
        context.dataStore.edit { it[PreferencesKeys.ROUTES_JSON] = json }
    }

    suspend fun saveDefaultRouteId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_ROUTE_ID] = id }
    }

    suspend fun vaultUriSnapshot(): Uri? =
        context.dataStore.data.first()[PreferencesKeys.VAULT_URI]?.let { Uri.parse(it) }
}
