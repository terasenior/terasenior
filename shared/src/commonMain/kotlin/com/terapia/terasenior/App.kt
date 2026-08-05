package com.terapia.terasenior

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.data.repository.admin.SupabaseEntityRepository
import com.terapia.terasenior.data.repository.admin.SupabaseUserProfileRepository
import com.terapia.terasenior.data.repository.agenda.SupabaseAppointmentRepository
import com.terapia.terasenior.data.repository.patient.SupabasePatientRepository
import com.terapia.terasenior.data.repository.results.SupabaseResultsRepository
import com.terapia.terasenior.domain.usecase.admin.*
import com.terapia.terasenior.domain.usecase.patient.*
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.repository.AuthRepository
import com.terapia.terasenior.treatment.ui.NumberSearchGame
import com.terapia.terasenior.treatment.ui.NumberSearchViewModel
import com.terapia.terasenior.treatment.ui.TreatmentMenuScreen
import com.terapia.terasenior.ui.admin.AdminEntitiesViewModel
import com.terapia.terasenior.ui.admin.AdminUsersViewModel
import com.terapia.terasenior.ui.admin.entities.AdminEntitiesScreen
import com.terapia.terasenior.ui.admin.users.AdminUsersScreen
import com.terapia.terasenior.ui.agenda.*
import com.terapia.terasenior.ui.login.LoginScreen
import com.terapia.terasenior.ui.patient.*
import com.terapia.terasenior.ui.theme.TeraseniorTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class Screen {
    LOGIN, THERAPY_PANEL, PATIENTS, PATIENT_DETAIL, AGENDA, APPOINTMENT_DETAIL, ADMIN_ENTITIES, ADMIN_USERS, NUMBER_SEARCH
}

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
fun App() {
    TeraseniorTheme {
        var currentUserProfile by remember { mutableStateOf<com.terapia.terasenior.models.Profile?>(null) }
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var selectedPatientId by remember { mutableStateOf<String?>(null) }
        var activeTherapyPatientId by remember { mutableStateOf<String?>(null) }
        var selectedAppointmentId by remember { mutableStateOf<String?>(null) }
        var currentEntityName by remember { mutableStateOf<String?>(null) }
        
        val scope = rememberCoroutineScope()
        val authRepository = remember { AuthRepository() }
        val entityRepository = remember { SupabaseEntityRepository() }

        // Cargar nombre de la entidad
        LaunchedEffect(currentUserProfile) {
            currentUserProfile?.let { profile ->
                if (profile.role != UserRole.SUPER_ADMIN && profile.entityId != null) {
                    entityRepository.getEntityById(profile.entityId)
                        .onSuccess { entity -> currentEntityName = entity?.name }
                } else {
                    currentEntityName = "Administración Global"
                }
            }
        }

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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userRole?.name ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(" • ", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = currentEntityName ?: "Cargando centro...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        authRepository.logout()
                                        currentUserProfile = null
                                        currentEntityName = null
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
                            selected = currentScreen == Screen.AGENDA || currentScreen == Screen.APPOINTMENT_DETAIL,
                            onClick = { currentScreen = Screen.AGENDA },
                            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            label = { Text("Agenda") }
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.PATIENTS || currentScreen == Screen.PATIENT_DETAIL,
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
                        
                        Screen.THERAPY_PANEL -> {
                            val patientRepository = remember { SupabasePatientRepository() }
                            val patientsState = remember { 
                                patientRepository.getPatients()
                            }.collectAsState(initial = Result.success(emptyList()))
                            
                            TreatmentMenuScreen(
                                patients = patientsState.value.getOrDefault(emptyList()),
                                selectedPatientId = activeTherapyPatientId,
                                onPatientSelected = { activeTherapyPatientId = it },
                                onNumberSearchClick = { currentScreen = Screen.NUMBER_SEARCH }
                            )
                        }
                        
                        Screen.NUMBER_SEARCH -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { 
                                NumberSearchViewModel(SaveActivityResultUseCase(resultsRepo)) 
                            }
                            NumberSearchGame(
                                viewModel = viewModel,
                                patientId = activeTherapyPatientId,
                                professionalId = currentUserProfile?.id,
                                appointmentId = null, // Próximamente vincular con agenda
                                onBack = { currentScreen = Screen.THERAPY_PANEL }
                            )
                        }

                        Screen.AGENDA -> {
                            val agendaRepo = remember { SupabaseAppointmentRepository() }
                            val patientRepo = remember { SupabasePatientRepository() }
                            val userRepo = remember { SupabaseUserProfileRepository() }
                            val entityRepo = remember { SupabaseEntityRepository() }
                            
                            val viewModel = remember { AgendaViewModel(agendaRepo) }
                            val createViewModel = remember { 
                                CreateAppointmentViewModel(agendaRepo, patientRepo, userRepo, entityRepo) 
                            }
                            
                            val createUiState by createViewModel.uiState.collectAsState()
                            var showCreateDialog by remember { mutableStateOf(false) }
                            val agendaState by viewModel.uiState.collectAsState()

                            if (createUiState is CreateAppointmentUiState.Created) {
                                showCreateDialog = false
                                createViewModel.resetState()
                                viewModel.loadAppointments()
                            }

                            AgendaScreen(
                                viewModel = viewModel,
                                onAddAppointmentClick = { 
                                    createViewModel.loadInitialData()
                                    showCreateDialog = true 
                                },
                                onAppointmentClick = { id ->
                                    selectedAppointmentId = id
                                    currentScreen = Screen.APPOINTMENT_DETAIL
                                }
                            )

                            if (showCreateDialog) {
                                val state = createUiState
                                if (state is CreateAppointmentUiState.Success || state is CreateAppointmentUiState.Loading) {
                                    val currentDay = (agendaState as? AgendaUiState.Success)?.selectedDate ?: kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                                    CreateAppointmentDialog(
                                        selectedDate = currentDay,
                                        patients = if (state is CreateAppointmentUiState.Success) state.patients else emptyList(),
                                        professionals = if (state is CreateAppointmentUiState.Success) state.professionals else emptyList(),
                                        onDismiss = { showCreateDialog = false },
                                        onConfirm = { t, d, s, e, type, staff, attendees ->
                                            scope.launch {
                                                val entityId = currentUserProfile?.entityId 
                                                    ?: (state as? CreateAppointmentUiState.Success)?.entities?.firstOrNull()?.id
                                                    ?: entityRepo.getEntities().getOrNull()?.firstOrNull()?.id
                                                    ?: ""
                                                
                                                createViewModel.createAppointment(
                                                    entityId = entityId,
                                                    title = t,
                                                    description = d,
                                                    startDate = currentDay,
                                                    startTime = s,
                                                    endTime = e,
                                                    type = type,
                                                    selectedStaffIds = staff,
                                                    selectedPatientIds = attendees
                                                )
                                            }
                                        },
                                        isLoading = state is CreateAppointmentUiState.Loading
                                    )
                                } else if (state is CreateAppointmentUiState.Error) {
                                    AlertDialog(
                                        onDismissRequest = { createViewModel.resetState() },
                                        title = { Text("Error") },
                                        text = { Text(state.message) },
                                        confirmButton = { TextButton(onClick = { createViewModel.resetState() }) { Text("OK") } }
                                    )
                                }
                            }
                        }

                        Screen.APPOINTMENT_DETAIL -> {
                            val appointmentId = selectedAppointmentId ?: ""
                            val repository = remember { SupabaseAppointmentRepository() }
                            val viewModel = remember(appointmentId) { 
                                AppointmentDetailViewModel(appointmentId, repository) 
                            }
                            AppointmentDetailScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.AGENDA }
                            )
                        }

                        Screen.PATIENTS -> {
                            val patientRepository = remember { SupabasePatientRepository() }
                            val entityRepository = remember { SupabaseEntityRepository() }
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
                                    onDismiss = { 
                                        showCreateDialog = false
                                        createViewModel.resetState()
                                    },
                                    onConfirm = { f, l, p, b -> 
                                        scope.launch {
                                            val entityId = currentUserProfile?.entityId 
                                                ?: entityRepository.getEntities().getOrNull()?.firstOrNull()?.id
                                                ?: ""
                                            
                                            createViewModel.createPatient(entityId, f, l, p, b)
                                        }
                                    },
                                    isLoading = createUiState is CreatePatientUiState.Loading,
                                    errorMessage = (createUiState as? CreatePatientUiState.Error)?.message
                                )
                            }
                        }

                        Screen.PATIENT_DETAIL -> {
                            val patientId = selectedPatientId ?: ""
                            val patientRepository = remember { SupabasePatientRepository() }
                            val resultsRepository = remember { SupabaseResultsRepository() }
                            val viewModel = remember(patientId) { 
                                PatientDetailViewModel(
                                    patientId = patientId, 
                                    repository = patientRepository,
                                    resultsRepository = resultsRepository,
                                    updatePatientUseCase = UpdatePatientUseCase(patientRepository),
                                    updateTherapeuticProfileUseCase = UpdateTherapeuticProfileUseCase(patientRepository)
                                ) 
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
