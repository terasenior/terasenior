package com.terapia.terasenior.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.usecase.admin.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminEntitiesViewModel(
    private val getEntitiesUseCase: GetEntitiesUseCase,
    private val createEntityUseCase: CreateEntityUseCase,
    private val updateEntityUseCase: UpdateEntityUseCase,
    private val deleteEntityUseCase: DeleteEntityUseCase,
    private val checkDependenciesUseCase: CheckEntityDependenciesUseCase,
    private val deactivateEntityUseCase: DeactivateEntityUseCase
) : ViewModel() {

    private val _allEntities = MutableStateFlow<List<Entity>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow(EntityStatusFilter.ALL)
    private val _currentPage = MutableStateFlow(1)
    private val _pageSize = 4
    
    private val _isLoading = MutableStateFlow(true)
    private val _globalError = MutableStateFlow<String?>(null)
    private val _tempErrorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdminEntitiesUiState> = combine(
        _allEntities, 
        _searchQuery, 
        _statusFilter, 
        _currentPage,
        _isLoading, 
        combine(_globalError, _tempErrorMessage) { g, t -> g to t }
    ) { values ->
        val entities = values[0] as List<Entity>
        val query = values[1] as String
        val filter = values[2] as EntityStatusFilter
        val page = values[3] as Int
        val loading = values[4] as Boolean
        val (gError, tempError) = values[5] as Pair<String?, String?>
        
        buildUiState(entities, query, filter, page, loading, gError, tempError)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminEntitiesUiState.Loading)

    private fun buildUiState(
        entities: List<Entity>,
        query: String,
        filter: EntityStatusFilter,
        page: Int,
        loading: Boolean,
        gError: String?,
        tempError: String?
    ): AdminEntitiesUiState {
        if (loading) return AdminEntitiesUiState.Loading
        if (gError != null) return AdminEntitiesUiState.Error(gError)

        val filtered = entities.filter { entity ->
            val matchesQuery = entity.name.contains(query, ignoreCase = true) || 
                             entity.cif.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                EntityStatusFilter.ALL -> true
                EntityStatusFilter.ACTIVE -> entity.status == "ACTIVE"
                EntityStatusFilter.INACTIVE -> entity.status == "INACTIVE"
            }
            matchesQuery && matchesFilter
        }

        val totalPages = kotlin.math.ceil(filtered.size.toDouble() / _pageSize).toInt().coerceAtLeast(1)
        val validatedPage = page.coerceIn(1, totalPages)
        
        val startIndex = (validatedPage - 1) * _pageSize
        val paginatedList = filtered.drop(startIndex).take(_pageSize)

        return AdminEntitiesUiState.Success(
            entities = paginatedList,
            searchQuery = query,
            selectedFilter = filter,
            currentPage = validatedPage,
            totalPages = totalPages,
            pageSize = _pageSize,
            errorMessage = tempError
        )
    }

    init {
        loadEntities()
    }

    fun loadEntities() {
        viewModelScope.launch {
            _isLoading.value = true
            _globalError.value = null
            getEntitiesUseCase()
                .onSuccess { entities ->
                    _allEntities.value = entities
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _globalError.value = error.message ?: "Error desconocido"
                    _isLoading.value = false
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
    }

    fun onFilterChanged(filter: EntityStatusFilter) {
        _statusFilter.value = filter
        _currentPage.value = 1
    }

    fun onPageChanged(page: Int) {
        _currentPage.value = page
    }

    fun createEntity(name: String, cif: String, address: String, licenseExpiry: String?, logoUrl: String? = null) {
        viewModelScope.launch {
            val newEntity = Entity(
                id = "", 
                name = name,
                cif = cif,
                address = address,
                status = "ACTIVE",
                createdAt = "",
                licenseExpiresAt = licenseExpiry,
                logoUrl = logoUrl
            )

            createEntityUseCase(newEntity)
                .onSuccess { loadEntities() }
                .onFailure { error ->
                    _tempErrorMessage.value = error.message ?: "Error al crear entidad"
                }
        }
    }

    fun updateEntity(entity: Entity) {
        viewModelScope.launch {
            updateEntityUseCase(entity)
                .onSuccess { loadEntities() }
                .onFailure { error ->
                    _tempErrorMessage.value = error.message ?: "Error al actualizar"
                }
        }
    }

    suspend fun checkDependencies(entityId: String): Boolean {
        return checkDependenciesUseCase(entityId).getOrDefault(true)
    }

    fun deleteEntity(entityId: String) {
        viewModelScope.launch {
            deleteEntityUseCase(entityId)
                .onSuccess { loadEntities() }
                .onFailure { error ->
                    _tempErrorMessage.value = error.message ?: "Error al eliminar"
                }
        }
    }

    fun deactivateEntity(entityId: String) {
        viewModelScope.launch {
            deactivateEntityUseCase(entityId)
                .onSuccess { loadEntities() }
                .onFailure { error ->
                    _tempErrorMessage.value = error.message ?: "Error al desactivar"
                }
        }
    }

    fun clearError() {
        _tempErrorMessage.value = null
    }
}
