package com.example.applinhamontagem.data.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

object SessionManager {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USERNAME_KEY = stringPreferencesKey("username")
    // ID operacional inteiro (tabela Utilizadores) — para chamadas como /api/utilizadores/{id}/motas
    private val ID_UTILIZADOR_KEY = stringPreferencesKey("id_utilizador_int")

    suspend fun saveSession(ctx: Context, token: String, userId: String, username: String, idUtilizador: Int?) {
        ctx.dataStore.edit {
            it[TOKEN_KEY] = token
            it[USER_ID_KEY] = userId
            it[USERNAME_KEY] = username
            it[ID_UTILIZADOR_KEY] = idUtilizador?.toString() ?: ""
        }
    }

    suspend fun getToken(ctx: Context): String? =
        ctx.dataStore.data.map { it[TOKEN_KEY] }.first()?.takeIf { it.isNotBlank() }

    suspend fun getUserId(ctx: Context): String? =
        ctx.dataStore.data.map { it[USER_ID_KEY] }.first()

    suspend fun getUsername(ctx: Context): String? =
        ctx.dataStore.data.map { it[USERNAME_KEY] }.first()

    suspend fun getIdUtilizador(ctx: Context): Int? =
        ctx.dataStore.data.map { it[ID_UTILIZADOR_KEY] }.first()?.toIntOrNull()

    suspend fun clear(ctx: Context) {
        ctx.dataStore.edit { it.clear() }
    }
}
