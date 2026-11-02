package com.vaultdraft

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vaultdraft.data.prefs.VaultPreferences
import com.vaultdraft.data.repository.RouteRepository
import com.vaultdraft.data.saf.VaultRepository
import com.vaultdraft.ui.capture.CaptureScreen
import com.vaultdraft.ui.capture.CaptureViewModel
import com.vaultdraft.ui.routes.RoutesScreen
import com.vaultdraft.ui.routes.RoutesViewModel
import com.vaultdraft.ui.theme.VaultDraftTheme

class MainActivity : ComponentActivity() {

    private val pickVault = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            val app = application as VaultDraftApp
            app.vaultRepository.takePersistablePermission(uri)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                app.preferences.saveVaultUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as VaultDraftApp
        val draftRouteId = intent?.getStringExtra(CaptureActivity.EXTRA_ROUTE_ID)

        setContent {
            VaultDraftTheme {
                val navController = rememberNavController()
                val vaultUri by app.preferences.vaultUri.collectAsState(initial = null)

                LaunchedEffect(Unit) {
                    app.routeRepository.seedDefaultsIfEmpty()
                }

                NavHost(navController = navController, startDestination = "capture") {
                    composable("capture") {
                        val vm: CaptureViewModel = viewModel(
                            factory = CaptureViewModel.Factory(
                                app.preferences, app.routeRepository, app.vaultRepository,
                                onPickVault = { pickVault.launch(null) },
                                initialRouteId = draftRouteId
                            )
                        )
                        CaptureScreen(
                            viewModel = vm,
                            onNavigateToRoutes = { navController.navigate("routes") }
                        )
                    }
                    composable("routes") {
                        val vm: RoutesViewModel = viewModel(
                            factory = RoutesViewModel.Factory(app.routeRepository)
                        )
                        RoutesScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
