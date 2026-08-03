package com.terapia.terasenior.ui.admin

import com.terapia.terasenior.domain.model.admin.Entity
import com.terapia.terasenior.domain.model.admin.UserProfile

sealed interface AdminEntitiesUiState {
    data object Loading : AdminEntitiesUiState
    data class Success(val entities: List<Entity>) : AdminEntitiesUiState
    data class Error(val message: String) : AdminEntitiesUiState
}

sealed interface AdminUsersUiState {
    data object Loading : AdminUsersUiState
    data class Success(val users: List<UserProfile>) : AdminUsersUiState
    data class Error(val message: String) : AdminUsersUiState
}
