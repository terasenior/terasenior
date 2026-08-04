package com.terapia.terasenior.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.domain.usecase.admin.GetEntitiesUseCase
import com.terapia.terasenior.domain.usecase.admin.GetUserProfilesUseCase
import com.terapia.terasenior.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel(
    private val getUserProfilesUseCase: GetUserProfilesUseCase,
    private val getEntitiesUseCase: GetEntitiesUseCase,
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUsersUiState>(AdminUsersUiState.Loading)
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    private var currentEntityId: String? = null
    private var entities: List<Entity> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = AdminUsersUiState.Loading
            getEntitiesUseCase()
                .onSuccess { list ->
                    entities = list
                    loadUsers()
                }
                .onFailure { error ->
                    _uiState.value = AdminUsersUiState.Error(error.message ?: "Error al cargar datos iniciales")
                }
        }
    }

    fun loadUsers(entityId: String? = null) {
        currentEntityId = entityId
        viewModelScope.launch {
            _uiState.value = AdminUsersUiState.Loading
            getUserProfilesUseCase(entityId)
                .onSuccess { users ->
                    _uiState.value = AdminUsersUiState.Success(users, entities)
                }
                .onFailure { error ->
                    _uiState.value = AdminUsersUiState.Error(error.message ?: "Error al cargar usuarios")
                }
        }
    }

    fun createUser(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        entityId: String?,
        phone: String?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = AdminUsersUiState.Loading
            authRepository.adminCreateUser(
                email = email,
                password = password,
                fullName = fullName,
                role = role,
                entityId = entityId,
                phone = phone,
                isActive = isActive
            ).onSuccess {
                loadUsers(currentEntityId)
            }.onFailure { error ->
                _uiState.value = AdminUsersUiState.Error(error.message ?: "Error al crear el usuario completo")
            }
        }
    }
}
