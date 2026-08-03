package com.terapia.terasenior

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.data.repository.admin.SupabaseEntityRepository
import com.terapia.terasenior.data.repository.admin.SupabaseUserProfileRepository
import com.terapia.terasenior.domain.usecase.admin.CreateEntityUseCase
import com.terapia.terasenior.domain.usecase.admin.CreateUserProfileUseCase
import com.terapia.terasenior.domain.usecase.admin.GetEntitiesUseCase
import com.terapia.terasenior.domain.usecase.admin.GetUserProfilesUseCase
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.treatment.ui.NumberSearchGame
import com.terapia.terasenior.treatment.ui.TreatmentMenuScreen
import com.terapia.terasenior.ui.admin.AdminEntitiesViewModel
import com.terapia.terasenior.ui.admin.AdminUsersViewModel
import com.terapia.terasenior.ui.admin.entities.AdminEntitiesScreen
import com.terapia.terasenior.ui.admin.users.AdminUsersScreen
import com.terapia.terasenior.ui.login.LoginScreen
import com.terapia.terasenior.ui.theme.TeraseniorTheme

enum class Screen {
    LOGIN, THERAPY_PANEL, ADMIN_ENTITIES, ADMIN_USERS, NUMBER_SEARCH
}

@Composable
fun App() {
    TeraseniorTheme {
        var currentUserProfile by remember { mutableStateOf<com.terapia.terasenior.models.Profile?>(null) }
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

        if (currentUserProfile == null) {
            LoginScreen { profile ->
                currentUserProfile = profile
                currentScreen = Screen.THERAPY_PANEL
                println("Login exitoso: ${profile.email} con rol ${profile.role}")
            }
        } else {
            val userRole = currentUserProfile?.role
            val canAdmin = userRole == UserRole.SUPER_ADMIN || 
                         userRole == UserRole.ADMIN_CENTRO

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == Screen.THERAPY_PANEL || currentScreen == Screen.NUMBER_SEARCH,
                            onClick = { currentScreen = Screen.THERAPY_PANEL },
                            icon = { Text("🎯", fontSize = 20.sp) },
                            label = { Text("Terapia") }
                        )
                        
                        if (canAdmin) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.ADMIN_ENTITIES,
                                onClick = { currentScreen = Screen.ADMIN_ENTITIES },
                                icon = { Text("🏢", fontSize = 20.sp) },
                                label = { Text("Centros") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.ADMIN_USERS,
                                onClick = { currentScreen = Screen.ADMIN_USERS },
                                icon = { Text("👥", fontSize = 20.sp) },
                                label = { Text("Usuarios") }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (currentScreen) {
                        Screen.LOGIN -> { /* No accesible si logged in */ }
                        
                        Screen.THERAPY_PANEL -> TreatmentMenuScreen(
                            onNumberSearchClick = { currentScreen = Screen.NUMBER_SEARCH }
                        )
                        
                        Screen.NUMBER_SEARCH -> NumberSearchGame(
                            onBack = { currentScreen = Screen.THERAPY_PANEL }
                        )

                        Screen.ADMIN_ENTITIES -> {
                            val repository = remember { SupabaseEntityRepository() }
                            val viewModel = remember { 
                                AdminEntitiesViewModel(
                                    GetEntitiesUseCase(repository),
                                    CreateEntityUseCase(repository)
                                ) 
                            }
                            AdminEntitiesScreen(viewModel)
                        }

                        Screen.ADMIN_USERS -> {
                            val profileRepository = remember { SupabaseUserProfileRepository() }
                            val entityRepository = remember { SupabaseEntityRepository() }
                            val viewModel = remember { 
                                AdminUsersViewModel(
                                    GetUserProfilesUseCase(profileRepository),
                                    CreateUserProfileUseCase(profileRepository),
                                    GetEntitiesUseCase(entityRepository)
                                ) 
                            }
                            AdminUsersScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
