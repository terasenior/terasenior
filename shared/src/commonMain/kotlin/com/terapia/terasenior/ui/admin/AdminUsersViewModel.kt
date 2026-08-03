package com.terapia.terasenior.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.domain.model.admin.UserRole
import com.terapia.terasenior.domain.usecase.admin.CreateUserProfileUseCase
import com.terapia.terasenior.domain.usecase.admin.GetUserProfilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel(
    private val getUserProfilesUseCase: GetUserProfilesUseCase,
    private val createUserProfileUseCase: CreateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUsersUiState>(AdminUsersUiState.Loading)
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    private var currentEntityId: String? = null

    fun loadUsers(entityId: String? = null) {
        currentEntityId = entityId
        viewModelScope.launch {
            _uiState.value = AdminUsersUiState.Loading
            getUserProfilesUseCase(entityId)
                .onSuccess { users ->
                    _uiState.value = AdminUsersUiState.Success(users)
                }
                .onFailure { error ->
                    _uiState.value = AdminUsersUiState.Error(error.message ?: "Error al cargar usuarios")
                }
        }
    }

    fun createUser(
        fullName: String,
        email: String,
        role: UserRole,
        entityId: String?,
        phone: String?
    ) {
        viewModelScope.launch {
            val newProfile = UserProfile(
                id = "", // Generado por Auth
                fullName = fullName,
                email = email,
                role = role,
                entityId = entityId,
                phone = phone,
                isActive = true
            )

            createUserProfileUseCase(newProfile)
                .onSuccess {
                    loadUsers(currentEntityId) // Recargar lista actual
                }
                .onFailure { error ->
                    _uiState.value = AdminUsersUiState.Error(error.message ?: "Error al crear usuario")
                }
        }
    }
}
