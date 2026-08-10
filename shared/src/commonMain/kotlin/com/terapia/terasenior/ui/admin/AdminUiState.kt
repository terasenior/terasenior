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
        val currentPage: Int = 1,
        val totalPages: Int = 1,
        val pageSize: Int = 4,
        val errorMessage: String? = null
    ) : AdminEntitiesUiState
    data class Error(val message: String) : AdminEntitiesUiState
}

enum class UserStatusFilter {
    ALL, ACTIVE, INACTIVE
}

sealed interface AdminUsersUiState {
    data object Loading : AdminUsersUiState
    data class Success(
        val users: List<UserProfile>,
        val entities: List<Entity> = emptyList(),
        val searchQuery: String = "",
        val selectedFilter: UserStatusFilter = UserStatusFilter.ALL,
        val selectedEntityFilter: String? = null,
        val errorMessage: String? = null
    ) : AdminUsersUiState
    data class Error(val message: String) : AdminUsersUiState
}
