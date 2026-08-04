package com.terapia.terasenior.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.model.admin.UserProfile
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.domain.usecase.admin.GetEntitiesUseCase
import com.terapia.terasenior.domain.usecase.admin.GetUserProfilesUseCase
import com.terapia.terasenior.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminUsersViewModel(
    private val getUserProfilesUseCase: GetUserProfilesUseCase,
    private val getEntitiesUseCase: GetEntitiesUseCase,
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow(UserStatusFilter.ALL)
    private val _entityFilter = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdminUsersUiState> = combine(
        _allUsers, 
        _searchQuery, 
        _statusFilter, 
        _entityFilter,
        _isLoading, 
        _entities,
        _errorMessage
    ) { args ->
        val users = args[0] as List<UserProfile>
        val query = args[1] as String
        val filter = args[2] as UserStatusFilter
        val entityId = args[3] as String?
        val loading = args[4] as Boolean
        val entitiesList = args[5] as List<Entity>
        val error = args[6] as String?
        
        buildUiState(users, query, filter, entityId, loading, entitiesList, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminUsersUiState.Loading)

    private fun buildUiState(
        users: List<UserProfile>,
        query: String,
        filter: UserStatusFilter,
        entityId: String?,
        loading: Boolean,
        entities: List<Entity>,
        error: String?
    ): AdminUsersUiState {
        if (loading) return AdminUsersUiState.Loading
        
        val filtered = users.filter { user ->
            val matchesQuery = user.fullName.contains(query, ignoreCase = true) || 
                             user.email.contains(query, ignoreCase = true)
            val matchesStatus = when (filter) {
                UserStatusFilter.ALL -> true
                UserStatusFilter.ACTIVE -> user.isActive
                UserStatusFilter.INACTIVE -> !user.isActive
            }
            val matchesEntity = entityId == null || user.entityId == entityId
            
            matchesQuery && matchesStatus && matchesEntity
        }
        
        return AdminUsersUiState.Success(
            users = filtered,
            entities = entities,
            searchQuery = query,
            selectedFilter = filter,
            selectedEntityFilter = entityId,
            errorMessage = error
        )
    }

    private var currentEntityId: String? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            getEntitiesUseCase()
                .onSuccess { list ->
                    _entities.value = list
                    loadUsers()
                }
                .onFailure { error ->
                    _errorMessage.value = "Error al cargar centros: ${error.message}"
                }
        }
    }

    fun loadUsers(entityId: String? = null) {
        currentEntityId = entityId
        viewModelScope.launch {
            _isLoading.value = true
            getUserProfilesUseCase(entityId)
                .onSuccess { users ->
                    _allUsers.value = users
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _errorMessage.value = "Error al cargar usuarios: ${error.message}"
                    _isLoading.value = false
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: UserStatusFilter) {
        _statusFilter.value = filter
    }

    fun onEntityFilterChanged(entityId: String?) {
        _entityFilter.value = entityId
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
            _isLoading.value = true
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
                _errorMessage.value = error.message
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            authRepository.deleteUser(userId)
                .onSuccess { loadUsers(currentEntityId) }
                .onFailure { error -> _errorMessage.value = error.message }
        }
    }

    fun updateUser(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            val profileToUpdate = com.terapia.terasenior.models.Profile(
                id = profile.id,
                email = profile.email,
                role = profile.role,
                entityId = profile.entityId,
                fullName = profile.fullName,
                isActive = profile.isActive,
                phone = profile.phone
            )
            authRepository.updateUserProfile(profileToUpdate)
                .onSuccess { loadUsers(currentEntityId) }
                .onFailure { error -> 
                    _errorMessage.value = error.message
                    _isLoading.value = false
                }
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            authRepository.changePassword(newPassword)
                .onSuccess { /* Éxito */ }
                .onFailure { error -> _errorMessage.value = error.message }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
