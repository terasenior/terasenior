package com.terapia.terasenior

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.terapia.terasenior.data.repository.admin.SupabaseEntityRepository
import com.terapia.terasenior.data.repository.admin.SupabaseUserProfileRepository
import com.terapia.terasenior.data.repository.patient.SupabasePatientRepository
import com.terapia.terasenior.domain.usecase.admin.*
import com.terapia.terasenior.domain.usecase.patient.CreatePatientUseCase
import com.terapia.terasenior.domain.usecase.patient.GetPatientsUseCase
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.repository.AuthRepository
import com.terapia.terasenior.treatment.ui.NumberSearchGame
import com.terapia.terasenior.treatment.ui.TreatmentMenuScreen
import com.terapia.terasenior.ui.admin.AdminEntitiesViewModel
import com.terapia.terasenior.ui.admin.AdminUsersViewModel
import com.terapia.terasenior.ui.admin.entities.AdminEntitiesScreen
import com.terapia.terasenior.ui.admin.users.AdminUsersScreen
import com.terapia.terasenior.ui.login.LoginScreen
import com.terapia.terasenior.ui.patient.*
import com.terapia.terasenior.ui.theme.TeraseniorTheme
import kotlinx.coroutines.launch

enum class Screen {
    LOGIN, THERAPY_PANEL, PATIENTS, PATIENT_DETAIL, ADMIN_ENTITIES, ADMIN_USERS, NUMBER_SEARCH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    TeraseniorTheme {
        var currentUserProfile by remember { mutableStateOf<com.terapia.terasenior.models.Profile?>(null) }
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var selectedPatientId by remember { mutableStateOf<String?>(null) }
        
        val scope = rememberCoroutineScope()
        val authRepository = remember { AuthRepository() }

        if (currentUserProfile == null) {
            LoginScreen { profile ->
                currentUserProfile = profile
                currentScreen = Screen.THERAPY_PANEL
            }
        } else {
            val userRole = currentUserProfile?.role
            val canAdmin = userRole == UserRole.SUPER_ADMIN || 
                         userRole == UserRole.ADMIN_CENTRO

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(
                                        text = currentUserProfile?.fullName ?: "Usuario",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = userRole?.name ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        authRepository.logout()
                                        currentUserProfile = null
                                        currentScreen = Screen.LOGIN
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cerrar Sesión")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == Screen.THERAPY_PANEL || currentScreen == Screen.NUMBER_SEARCH,
                            onClick = { currentScreen = Screen.THERAPY_PANEL },
                            icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                            label = { Text("Terapia") }
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.PATIENTS,
                            onClick = { currentScreen = Screen.PATIENTS },
                            icon = { Icon(Icons.Default.People, contentDescription = null) },
                            label = { Text("Pacientes") }
                        )
                        
                        if (canAdmin) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.ADMIN_ENTITIES,
                                onClick = { currentScreen = Screen.ADMIN_ENTITIES },
                                icon = { Icon(Icons.Default.Business, contentDescription = null) },
                                label = { Text("Centros") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.ADMIN_USERS,
                                onClick = { currentScreen = Screen.ADMIN_USERS },
                                icon = { Icon(Icons.Default.People, contentDescription = null) },
                                label = { Text("Usuarios") }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (currentScreen) {
                        Screen.LOGIN -> { /* No accesible */ }
                        
                        Screen.THERAPY_PANEL -> TreatmentMenuScreen(
                            onNumberSearchClick = { currentScreen = Screen.NUMBER_SEARCH }
                        )
                        
                        Screen.NUMBER_SEARCH -> NumberSearchGame(
                            onBack = { currentScreen = Screen.THERAPY_PANEL }
                        )

                        Screen.PATIENTS -> {
                            val patientRepository = remember { SupabasePatientRepository() }
                            val listViewModel = remember { 
                                PatientListViewModel(GetPatientsUseCase(patientRepository)) 
                            }
                            val createViewModel = remember {
                                CreatePatientViewModel(CreatePatientUseCase(patientRepository))
                            }
                            val createUiState by createViewModel.uiState.collectAsState()
                            var showCreateDialog by remember { mutableStateOf(false) }

                            if (createUiState is CreatePatientUiState.Success) {
                                showCreateDialog = false
                                createViewModel.resetState()
                                listViewModel.loadPatients()
                            }

                            PatientListScreen(
                                viewModel = listViewModel,
                                onPatientClick = { id -> 
                                    selectedPatientId = id
                                    currentScreen = Screen.PATIENT_DETAIL 
                                },
                                onAddPatientClick = { showCreateDialog = true }
                            )

                            if (showCreateDialog) {
                                CreatePatientDialog(
                                    onDismiss = { showCreateDialog = false },
                                    onConfirm = { f, l, p, b -> 
                                        createViewModel.createPatient(currentUserProfile?.entityId ?: "", f, l, p, b)
                                    },
                                    isLoading = createUiState is CreatePatientUiState.Loading
                                )
                            }
                        }

                        Screen.PATIENT_DETAIL -> {
                            val patientId = selectedPatientId ?: ""
                            val repository = remember { SupabasePatientRepository() }
                            val viewModel = remember(patientId) { 
                                PatientDetailViewModel(patientId, repository) 
                            }
                            PatientDetailScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.PATIENTS }
                            )
                        }

                        Screen.ADMIN_ENTITIES -> {
                            val repository = remember { SupabaseEntityRepository() }
                            val viewModel = remember { 
                                AdminEntitiesViewModel(
                                    getEntitiesUseCase = GetEntitiesUseCase(repository),
                                    createEntityUseCase = CreateEntityUseCase(repository),
                                    updateEntityUseCase = UpdateEntityUseCase(repository),
                                    deleteEntityUseCase = DeleteEntityUseCase(repository),
                                    checkDependenciesUseCase = CheckEntityDependenciesUseCase(repository),
                                    deactivateEntityUseCase = DeactivateEntityUseCase(repository)
                                ) 
                            }
                            AdminEntitiesScreen(viewModel)
                        }

                        Screen.ADMIN_USERS -> {
                            val profileRepository = remember { SupabaseUserProfileRepository() }
                            val entityRepository = remember { SupabaseEntityRepository() }
                            val viewModel = remember { 
                                AdminUsersViewModel(
                                    getUserProfilesUseCase = GetUserProfilesUseCase(profileRepository),
                                    getEntitiesUseCase = GetEntitiesUseCase(entityRepository),
                                    authRepository = authRepository
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
