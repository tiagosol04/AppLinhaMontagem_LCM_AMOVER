package com.example.applinhamontagem.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applinhamontagem.data.remote.dto.LoginResponse
import com.example.applinhamontagem.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var loginError by mutableStateOf<String?>(null); private set
    var user by mutableStateOf<LoginResponse?>(null); private set

    fun login(u: String, p: String) {
        viewModelScope.launch {
            isLoading = true; loginError = null
            repository.login(u, p)
                .onSuccess { user = it }
                .onFailure { loginError = it.message }
            isLoading = false
        }
    }

    fun logout() {
        repository.logout()
        user = null
    }
}