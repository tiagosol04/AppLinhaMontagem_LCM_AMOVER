package com.example.applinhamontagem.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applinhamontagem.data.remote.RetrofitClient
import com.example.applinhamontagem.data.remote.dto.OrdemProducaoDto
import com.example.applinhamontagem.data.repository.FactoryRepository
import kotlinx.coroutines.launch

class OrdersViewModel(private val repository: FactoryRepository) : ViewModel() {
    var ordens by mutableStateOf<List<OrdemProducaoDto>>(emptyList()); private set
    var isLoading by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set

    fun loadOrdens() {
        viewModelScope.launch {
            isLoading = true
            repository.getOrdens()
                .onSuccess { ordens = it }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    suspend fun getOrdemById(id: Int): Result<OrdemProducaoDto> {
        return try {
            val r = RetrofitClient.api.getOrdem(id)
            if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
            else Result.failure(Exception("Erro HTTP"))
        } catch(e: Exception) { Result.failure(e) }
    }
}