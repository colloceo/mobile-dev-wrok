package com.example.billreminder.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billreminder.data.local.entity.UserEntity
import com.example.billreminder.data.repository.AuthRepository
import com.example.billreminder.util.Result
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: UserEntity) : AuthUiState()
    data class Error(val reason: String) : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val state: LiveData<AuthUiState> = _state

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = AuthUiState.Error("empty_field")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = repository.login(username.trim(), password)) {
                is Result.Success -> _state.value = AuthUiState.Success(result.data)
                is Result.Error -> _state.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun register(username: String, password: String, confirmPassword: String) {
        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _state.value = AuthUiState.Error("empty_field")
            return
        }
        if (password.length < 4) {
            _state.value = AuthUiState.Error("password_too_short")
            return
        }
        if (password != confirmPassword) {
            _state.value = AuthUiState.Error("password_mismatch")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = repository.register(username.trim(), password)) {
                is Result.Success -> _state.value = AuthUiState.Success(result.data)
                is Result.Error -> _state.value = AuthUiState.Error(result.message)
            }
        }
    }
}
