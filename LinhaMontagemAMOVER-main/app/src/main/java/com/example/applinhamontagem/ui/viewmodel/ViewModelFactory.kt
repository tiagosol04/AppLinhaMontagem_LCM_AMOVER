package com.example.applinhamontagem.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.applinhamontagem.data.repository.AuthRepository
import com.example.applinhamontagem.data.repository.FactoryRepository

class ViewModelFactory : ViewModelProvider.Factory {
    private val authRepo = AuthRepository()
    private val factoryRepo = FactoryRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepo) as T
            modelClass.isAssignableFrom(ProductionViewModel::class.java) -> ProductionViewModel(factoryRepo) as T
            modelClass.isAssignableFrom(OrdersViewModel::class.java) -> OrdersViewModel(factoryRepo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}