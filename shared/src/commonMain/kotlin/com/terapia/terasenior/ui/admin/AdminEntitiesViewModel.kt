package com.terapia.terasenior.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.usecase.admin.CreateEntityUseCase
import com.terapia.terasenior.domain.usecase.admin.GetEntitiesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminEntitiesViewModel(
    private val getEntitiesUseCase: GetEntitiesUseCase,
    private val createEntityUseCase: CreateEntityUseCase
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
            // No cambiamos a Loading global para no ocultar la lista actual, 
            // en una UI real usaríamos un estado de "procesando" específico para el diálogo.
            val newEntity = Entity(
                id = "", // Generado por Supabase
                name = name,
                cif = cif,
                address = address,
                status = "active",
                createdAt = ""
            )

            createEntityUseCase(newEntity)
                .onSuccess {
                    loadEntities() // Recargar lista tras éxito
                }
                .onFailure { error ->
                    _uiState.value = AdminEntitiesUiState.Error(error.message ?: "Error al crear entidad")
                }
        }
    }
}
