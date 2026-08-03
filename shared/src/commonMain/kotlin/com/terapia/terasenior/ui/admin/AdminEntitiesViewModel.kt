package com.terapia.terasenior.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.usecase.admin.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminEntitiesViewModel(
    private val getEntitiesUseCase: GetEntitiesUseCase,
    private val createEntityUseCase: CreateEntityUseCase,
    private val updateEntityUseCase: UpdateEntityUseCase,
    private val deleteEntityUseCase: DeleteEntityUseCase,
    private val checkDependenciesUseCase: CheckEntityDependenciesUseCase,
    private val deactivateEntityUseCase: DeactivateEntityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminEntitiesUiState>(AdminEntitiesUiState.Loading)
    val uiState: StateFlow<AdminEntitiesUiState> = _uiState.asStateFlow()

    init {
        loadEntities()
    }

    fun loadEntities() {
        viewModelScope.launch {
            _uiState.value = AdminEntitiesUiState.Loading
            getEntitiesUseCase()
                .onSuccess { entities ->
                    _uiState.value = AdminEntitiesUiState.Success(entities)
                }
                .onFailure { error ->
                    _uiState.value = AdminEntitiesUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    fun createEntity(name: String, cif: String, address: String) {
        viewModelScope.launch {
            val newEntity = Entity(
                id = "", 
                name = name,
                cif = cif,
                address = address,
                status = "ACTIVE",
                createdAt = ""
            )

            createEntityUseCase(newEntity)
                .onSuccess { loadEntities() }
                .onFailure { error ->
                    showTempError(error.message ?: "Error al crear entidad")
                }
        }
    }

    fun updateEntity(entity: Entity) {
        viewModelScope.launch {
            updateEntityUseCase(entity)
                .onSuccess { loadEntities() }
                .onFailure { error ->
                    showTempError(error.message ?: "Error al actualizar")
                }
        }
    }

    suspend fun checkDependencies(entityId: String): Boolean {
        return checkDependenciesUseCase(entityId).getOrDefault(true)
    }

    fun deleteEntity(entityId: String) {
        viewModelScope.launch {
            deleteEntityUseCase(entityId)
                .onSuccess { 
                    loadEntities() 
                }
                .onFailure { error ->
                    showTempError(error.message ?: "Error al eliminar: Verifica permisos en Supabase")
                }
        }
    }

    fun deactivateEntity(entityId: String) {
        viewModelScope.launch {
            deactivateEntityUseCase(entityId)
                .onSuccess { loadEntities() }
                .onFailure { error ->
                    showTempError(error.message ?: "Error al desactivar")
                }
        }
    }

    private fun showTempError(message: String) {
        val currentState = _uiState.value
        if (currentState is AdminEntitiesUiState.Success) {
            _uiState.value = currentState.copy(errorMessage = message)
        } else {
            _uiState.value = AdminEntitiesUiState.Error(message)
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is AdminEntitiesUiState.Success) {
            _uiState.value = currentState.copy(errorMessage = null)
        }
    }
}
