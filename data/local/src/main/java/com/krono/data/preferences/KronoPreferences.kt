package com.krono.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.kronoDataStore: DataStore<Preferences> by preferencesDataStore(name = "krono_preferences")

class KronoPreferences(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_RULE_FILTER = stringPreferencesKey("rule_filter")
        val KEY_HISTORY_FILTER = stringPreferencesKey("history_filter")
    }

    val themeFlow: Flow<String> = context.kronoDataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "KRONO_DARK"
    }

    val ruleFilterFlow: Flow<String> = context.kronoDataStore.data.map { preferences ->
        preferences[KEY_RULE_FILTER] ?: "ALL"
    }

    val historyFilterFlow: Flow<String> = context.kronoDataStore.data.map { preferences ->
        preferences[KEY_HISTORY_FILTER] ?: "ALL"
    }

    suspend fun setTheme(theme: String) {
        context.kronoDataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setRuleFilter(filter: String) {
        context.kronoDataStore.edit { preferences ->
            preferences[KEY_RULE_FILTER] = filter
        }
    }

    suspend fun setHistoryFilter(filter: String) {
        context.kronoDataStore.edit { preferences ->
            preferences[KEY_HISTORY_FILTER] = filter
        }
    }
}
