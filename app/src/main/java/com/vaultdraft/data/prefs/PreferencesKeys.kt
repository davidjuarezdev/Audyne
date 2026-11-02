package com.vaultdraft.data.prefs

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val VAULT_URI = stringPreferencesKey("vault_uri")
    val ROUTES_JSON = stringPreferencesKey("routes_json")
    val DEFAULT_ROUTE_ID = stringPreferencesKey("default_route_id")
    val WIDGET_DRAFT = stringPreferencesKey("widget_draft")
    val WIDGET_ROUTE_ID = stringPreferencesKey("widget_route_id")
}
