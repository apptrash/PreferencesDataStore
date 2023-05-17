package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
private const val INT_KEY_VALUE = "SOME_INT_KEY"
private const val STRING_KEY_VALUE = "SOME_STRING_KEY"
private val FIRST_KEY = intPreferencesKey(INT_KEY_VALUE)
private val SECOND_KEY = stringPreferencesKey(STRING_KEY_VALUE)
private const val PREFERENCES_NAME = "settings"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_NAME)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startPreferencesScenario()
    }

    private fun startPreferencesScenario() {
        lifecycleScope.launch {
            val subscriptionDeferred = async { subscribeOnPreferencesUpdates() }
            createOrUpdatePreference(FIRST_KEY, 1234567)
            delay(1000)
            createOrUpdatePreference(SECOND_KEY, "Such a long string! Wow!")
            delay(1000)
            removePreferenceBy(SECOND_KEY)
            delay(1000)
            createOrUpdatePreference(SECOND_KEY, "Such a shor...")
            delay(1000)
            clearPreferences()
            delay(1000)
            val preferences: Preferences = getPreferences()
            val value: Int? = preferences[FIRST_KEY]
            Log.d(TAG, "Last value for $FIRST_KEY key: $value")
            subscriptionDeferred.cancel()
        }
    }

    private suspend fun<T> createOrUpdatePreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences: MutablePreferences ->
            preferences[key] = value
        }
    }

    private suspend fun subscribeOnPreferencesUpdates() {
        dataStore.data.collect { preferences: Preferences ->
            printStoredPreferences(preferences)
        }
    }

    private fun printStoredPreferences(preferences: Preferences) {
        for ((key, value) in preferences.asMap()) {
            Log.d(TAG, "$key -> $value")
        }
        Log.d(TAG, "---- ---- ---- ----")
    }

    private suspend fun getPreferences(): Preferences {
        return dataStore.data.first()
    }

    private suspend fun<T> removePreferenceBy(key: Preferences.Key<T>) {
        dataStore.edit { preferences: MutablePreferences ->
            preferences.remove(key)
        }
    }

    private suspend fun clearPreferences() {
        dataStore.edit { preferences: MutablePreferences ->
            preferences.clear()
        }
    }
}