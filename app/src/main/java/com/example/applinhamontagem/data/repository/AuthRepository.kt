package com.example.applinhamontagem.data.repository

import android.content.Context
import com.example.applinhamontagem.data.remote.RetrofitClient
import com.example.applinhamontagem.data.remote.dto.LoginRequest
import com.example.applinhamontagem.data.remote.dto.LoginResponse
import com.example.applinhamontagem.data.utils.ScannerInputHelper
import com.example.applinhamontagem.data.utils.SessionManager

class AuthRepository {
    private val api = RetrofitClient.api

    suspend fun login(username: String, pin: String, ctx: Context): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, pin))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                RetrofitClient.setToken(body.token)
                SessionManager.saveSession(ctx, body.token, body.userId, body.username, body.idUtilizador)
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(ScannerInputHelper.mapApiError(errorBody, response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ScannerInputHelper.mapApiError(e.message)))
        }
    }

    suspend fun logout(ctx: Context) {
        SessionManager.clear(ctx)
        RetrofitClient.setToken(null)
    }
}