package com.nat.finalstoryapp.ui.authpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.nat.finalstoryapp.data.network.ApiService
import com.nat.finalstoryapp.data.api.response.LoginResponse
import com.nat.finalstoryapp.data.api.response.RegisterResponse
import kotlinx.coroutines.Dispatchers

class AuthViewModel(private val apiService: ApiService) : ViewModel() {

    fun register(name: String, email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            val response: RegisterResponse = apiService.register(name, email, password)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure<RegisterResponse>(e))
        }
    }

    fun login(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            val response: LoginResponse = apiService.login(email, password)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure<LoginResponse>(e))
        }
    }
}

class AuthViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return AuthViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}