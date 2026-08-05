package com.terapia.terasenior.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.models.Profile
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
    val showResetDialog: Boolean = false,
    val resetMessage: String? = null,
    val isResetLoading: Boolean = false
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

    fun onShowResetDialog(show: Boolean) {
        _uiState.update { it.copy(showResetDialog = show, resetMessage = null) }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(resetMessage = "Introduce un correo válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResetLoading = true, resetMessage = null) }
            authRepository.resetPassword(email)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isResetLoading = false, 
                            resetMessage = "Se ha enviado un enlace de recuperación a tu correo."
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isResetLoading = false, 
                            resetMessage = "Error: ${error.message}"
                        ) 
                    }
                }
        }
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
            
            // 1. Login con Supabase Auth
            val loginResult = authRepository.login(currentEmail, currentPassword)

            loginResult.onSuccess {
                // 2. Obtener el perfil del usuario
                val profileResult = authRepository.getCurrentProfile()
                
                profileResult.onSuccess { profile ->
                    if (profile != null) {
                        // 3. Validar Licencia y registrar acceso
                        val licenseResult = authRepository.checkLicenseAndRecordLogin(profile)
                        
                        licenseResult.onSuccess {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    userProfile = profile
                                )
                            }
                        }.onFailure { licenseError ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = licenseError.message
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No se pudo recuperar tu perfil de usuario."
                            )
                        }
                    }
                }.onFailure { error ->
                    val userFriendlyError = when {
                        error.message?.contains("invalid_credentials") == true -> "Correo o contraseña incorrectos."
                        error.message?.contains("rate_limit") == true -> "Demasiados intentos. Inténtalo más tarde."
                        else -> error.message ?: "Credenciales incorrectas o error de conexión."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = userFriendlyError
                        )
                    }
                }
            }.onFailure { error ->
                val userFriendlyError = when {
                    error.message?.contains("invalid_credentials") == true -> "Correo o contraseña incorrectos."
                    error.message?.contains("rate_limit") == true -> "Demasiados intentos. Inténtalo más tarde."
                    else -> error.message ?: "Error al conectar con el servidor."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = userFriendlyError
                    )
                }
            }
        }
    }
}