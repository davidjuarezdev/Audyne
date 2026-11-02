package com.vaultdraft

import android.app.Application
import com.vaultdraft.data.prefs.VaultPreferences
import com.vaultdraft.data.repository.RouteRepository
import com.vaultdraft.data.saf.VaultRepository

class VaultDraftApp : Application() {
    lateinit var preferences: VaultPreferences
        private set
    lateinit var routeRepository: RouteRepository
        private set
    lateinit var vaultRepository: VaultRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = VaultPreferences(this)
        routeRepository = RouteRepository(preferences)
        vaultRepository = VaultRepository(this)
    }
}
