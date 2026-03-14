package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.RegisterRequest
import org.pracainzynierska.sportbooking.SportApi

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isOwner: Boolean = false,
    val emailError: Boolean = false,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

class RegisterViewModel(
    private val api: SportApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = false, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPassword = password, errorMessage = null) }
    }

    fun onIsOwnerChanged(isOwner: Boolean) {
        _uiState.update { it.copy(isOwner = isOwner) }
    }

    fun register() {
        val state = _uiState.value
        
        // Validation logic
        if (state.email.isBlank() || state.password.isBlank() || state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Wypełnij wszystkie pola.") }
            return
        }

        if (!isValidEmail(state.email)) {
            _uiState.update { it.copy(emailError = true, errorMessage = "Podaj poprawny adres email.") }
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Hasła nie są identyczne!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            try {
                val request = RegisterRequest(
                    name = state.name,
                    email = state.email,
                    password = state.password,
                    isOwner = state.isOwner
                )
                val success = api.register(request)
                if (success) {
                    _uiState.update { it.copy(registerSuccess = true, isProcessing = false) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Rejestracja nieudana. Email może być zajęty.", isProcessing = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Błąd: ${e.message}", isProcessing = false) }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
        return email.matches(emailRegex.toRegex())
    }
}
