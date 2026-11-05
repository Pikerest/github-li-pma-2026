package com.example.myapp008bdatastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "gamer_profile")

data class PlayerProfile(
    val nickname: String = "",
    val hardcoreEnabled: Boolean = false
)

private object ProfileKeys {
    val nickname: Preferences.Key<String> = stringPreferencesKey("NICKNAME")
    val hardcore: Preferences.Key<Boolean> = booleanPreferencesKey("HARDCORE")
}

val Context.playerProfile: Flow<PlayerProfile>
    get() = profileDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            PlayerProfile(
                nickname = preferences[ProfileKeys.nickname].orEmpty(),
                hardcoreEnabled = preferences[ProfileKeys.hardcore] ?: false
            )
        }

suspend fun Context.savePlayerProfile(profile: PlayerProfile) {
    profileDataStore.edit { preferences ->
        preferences[ProfileKeys.nickname] = profile.nickname
        preferences[ProfileKeys.hardcore] = profile.hardcoreEnabled
    }
}
