package com.terapia.terasenior.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.models.Profile
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userProfile: Profile? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun login() {
        val currentEmail = _uiState.value.email
        val currentPassword = _uiState.value.password

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, rellena todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(currentEmail, currentPassword)

            result.onSuccess {
                val profile = Profile(
                    id = "",
                    email = currentEmail,
                    role = UserRole.THERAPIST
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al iniciar sesión"
                    )
                }
            }
        }
    }
}