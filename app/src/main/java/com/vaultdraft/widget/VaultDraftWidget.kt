package com.vaultdraft.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.material3.Button
import androidx.glance.material3.Text
import androidx.glance.text.TextStyle
import com.vaultdraft.CaptureActivity
import com.vaultdraft.data.repository.RouteRepository
import com.vaultdraft.data.prefs.VaultPreferences
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class VaultDraftWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = VaultPreferences(context)
        val routes = RouteRepository(prefs).routes.first()
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "VaultDraft",
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                    Button(
                        text = "New capture",
                        onClick = actionStartActivity<CaptureActivity>(),
                        modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp)) {
                        items(routes.take(5), key = { it.id }) { route ->
                            val key = ActionParameters.Key<String>("route_id")
                            Button(
                                text = route.name,
                                onClick = actionStartActivity<CaptureActivity>(
                                    parameters = actionParametersOf(key to route.id)
                                ),
                                modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

class VaultDraftWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VaultDraftWidget()
}
