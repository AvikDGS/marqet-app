package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.AppUser
import com.example.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AuthRepository(application)
    val dbRepository = com.example.repository.DatabaseRepository()
    
    val currentUser: StateFlow<AppUser?> = repository.currentUser
    
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState

    fun updateCategories(categoryIds: List<String>) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                dbRepository.updateCategories(user.uid, categoryIds)
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.signIn(email, pass)
            if (result.isSuccess) {
                _authState.value = AuthUiState.Success
            } else {
                _authState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun signUp(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.signUp(name, email, pass)
            if (result.isSuccess) {
                _authState.value = AuthUiState.Success
            } else {
                _authState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.resetPassword(email)
            if (result.isSuccess) {
                _authState.value = AuthUiState.Success
            } else {
                _authState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Reset password failed")
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val result = repository.signInWithGoogle()
            if (result.isSuccess) {
                _authState.value = AuthUiState.Success
            } else {
                _authState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Google Sign In failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
