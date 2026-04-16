package com.example.applinhamontagem.data.repository

import com.example.applinhamontagem.data.remote.RetrofitClient
import com.example.applinhamontagem.data.remote.dto.LoginRequest
import com.example.applinhamontagem.data.remote.dto.LoginResponse

class AuthRepository {
    private val api = RetrofitClient.api

    suspend fun login(username: String, pin: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, pin))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                RetrofitClient.setToken(body.token)
                Result.success(body)
            } else {
                Result.failure(Exception("Login falhou: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        RetrofitClient.setToken(null)
    }
}