package com.example.applinhamontagem.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applinhamontagem.data.remote.RetrofitClient
import com.example.applinhamontagem.data.remote.dto.LoginResponse
import com.example.applinhamontagem.data.repository.AuthRepository
import com.example.applinhamontagem.data.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var loginError by mutableStateOf<String?>(null); private set
    var user by mutableStateOf<LoginResponse?>(null); private set
    var isCheckingSession by mutableStateOf(true); private set

    /** Tenta restaurar sessão guardada. Chamado ao iniciar a app. */
    fun restoreSession(ctx: Context) {
        viewModelScope.launch {
            isCheckingSession = true
            val token = SessionManager.getToken(ctx)
            val userId = SessionManager.getUserId(ctx)
            val username = SessionManager.getUsername(ctx)
            val idUtilizador = SessionManager.getIdUtilizador(ctx)
            if (!token.isNullOrBlank() && !userId.isNullOrBlank() && !username.isNullOrBlank()) {
                RetrofitClient.setToken(token)
                user = LoginResponse(
                    token = token,
                    userId = userId,
                    username = username,
                    email = "",
                    roles = emptyList(),
                    idUtilizador = idUtilizador
                )
            }
            isCheckingSession = false
        }
    }

    fun login(u: String, p: String, ctx: Context) {
        viewModelScope.launch {
            isLoading = true
            loginError = null
            repository.login(u, p, ctx)
                .onSuccess { user = it }
                .onFailure { loginError = it.message }
            isLoading = false
        }
    }

    fun logout(ctx: Context) {
        viewModelScope.launch {
            repository.logout(ctx)
            user = null
        }
    }
}