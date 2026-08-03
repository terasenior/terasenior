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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al obtener perfil: ${error.message}"
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Credenciales incorrectas o error de conexión."
                    )
                }
            }
        }
    }
}