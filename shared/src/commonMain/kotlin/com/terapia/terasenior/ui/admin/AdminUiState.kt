package com.terapia.terasenior.ui.admin

import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.model.admin.UserProfile

enum class EntityStatusFilter {
    ALL, ACTIVE, INACTIVE
}

sealed interface AdminEntitiesUiState {
    data object Loading : AdminEntitiesUiState
    data class Success(
        val entities: List<Entity>,
        val searchQuery: String = "",
        val selectedFilter: EntityStatusFilter = EntityStatusFilter.ALL,
        val errorMessage: String? = null
    ) : AdminEntitiesUiState
    data class Error(val message: String) : AdminEntitiesUiState
}

sealed interface AdminUsersUiState {
    data object Loading : AdminUsersUiState
    data class Success(
        val users: List<UserProfile>,
        val entities: List<Entity> = emptyList()
    ) : AdminUsersUiState
    data class Error(val message: String) : AdminUsersUiState
}
