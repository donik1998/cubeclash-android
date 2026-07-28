package com.donik1998.cubeclash.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.model.AppSettings
import com.donik1998.cubeclash.core.model.ThemeMode
import com.donik1998.cubeclash.core.model.TimerStyle
import com.donik1998.cubeclash.core.model.WcaEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("cubeclash_settings")

/**
 * Settings live in Preferences DataStore, and DataStore *is* the repository — wrapping it in a
 * second layer would only restate the same six keys in a different file.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            timerStyle = prefs[Keys.TIMER_STYLE]?.let { runCatching { TimerStyle.valueOf(it) }.getOrNull() }
                ?: TimerStyle.HOLD,
            inspectionEnabled = prefs[Keys.INSPECTION] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            soundEnabled = prefs[Keys.SOUND] ?: false,
            lastEventId = prefs[Keys.LAST_EVENT] ?: WcaEvent.DEFAULT.id,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) = put(Keys.THEME, mode.name)

    override suspend fun setTimerStyle(style: TimerStyle) = put(Keys.TIMER_STYLE, style.name)

    override suspend fun setInspectionEnabled(enabled: Boolean) = put(Keys.INSPECTION, enabled)

    override suspend fun setHapticsEnabled(enabled: Boolean) = put(Keys.HAPTICS, enabled)

    override suspend fun setLastEvent(event: WcaEvent) = put(Keys.LAST_EVENT, event.id)

    private suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        context.settingsDataStore.edit { it[key] = value }
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val TIMER_STYLE = stringPreferencesKey("timer_style")
        val INSPECTION = booleanPreferencesKey("inspection_enabled")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val LAST_EVENT = stringPreferencesKey("last_event")
    }
}
