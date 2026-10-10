package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "word_processor_settings")

/**
 * DataStore preferences manager for editor and application settings.
 */
class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_AUTOSAVE_ENABLED = booleanPreferencesKey("autosave_enabled")
        val KEY_AUTOSAVE_INTERVAL_SEC = intPreferencesKey("autosave_interval_sec")
        val KEY_DEFAULT_PAGE_SIZE = stringPreferencesKey("default_page_size")
        val KEY_SHOW_RULER = booleanPreferencesKey("show_ruler")
        val KEY_SHOW_WORD_COUNT = booleanPreferencesKey("show_word_count")
        val KEY_DEFAULT_RTL = booleanPreferencesKey("default_rtl")
    }

    val isAutosaveEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTOSAVE_ENABLED] ?: true
    }

    val autosaveIntervalSec: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTOSAVE_INTERVAL_SEC] ?: 3
    }

    val defaultPageSize: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_PAGE_SIZE] ?: "A4"
    }

    val showRuler: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SHOW_RULER] ?: true
    }

    val showWordCount: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SHOW_WORD_COUNT] ?: true
    }

    val defaultRtl: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_RTL] ?: false
    }

    suspend fun setAutosaveEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTOSAVE_ENABLED] = enabled }
    }

    suspend fun setShowRuler(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_RULER] = show }
    }

    suspend fun setShowWordCount(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_WORD_COUNT] = show }
    }

    suspend fun setDefaultRtl(isRtl: Boolean) {
        context.dataStore.edit { it[KEY_DEFAULT_RTL] = isRtl }
    }
}
