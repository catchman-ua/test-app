package com.test.testapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dragPrefsDataStore by preferencesDataStore(name = "drag_prefs_mvi")

class PositionLocalDataSource(private val context: Context) {

    private val KEY_X = floatPreferencesKey("icon_x_fraction")
    private val KEY_Y = floatPreferencesKey("icon_y_fraction")

    val positions: Flow<Pair<Float, Float>> =
        context.dragPrefsDataStore.data.map { prefs ->
            val xf = (prefs[KEY_X] ?: 0.5f).coerceIn(0f, 1f)
            val yf = (prefs[KEY_Y] ?: 0.5f).coerceIn(0f, 1f)
            xf to yf
        }

    suspend fun save(xRatio: Float, yRatio: Float) {
        context.dragPrefsDataStore.edit { prefs ->
            prefs[KEY_X] = xRatio.coerceIn(0f, 1f)
            prefs[KEY_Y] = yRatio.coerceIn(0f, 1f)
        }
    }
}
