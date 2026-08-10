package com.terapia.terasenior

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terapia.terasenior.data.repository.admin.SupabaseEntityRepository
import com.terapia.terasenior.data.repository.admin.SupabaseUserProfileRepository
import com.terapia.terasenior.data.repository.agenda.SupabaseAppointmentRepository
import com.terapia.terasenior.data.repository.patient.SupabasePatientRepository
import com.terapia.terasenior.data.repository.results.SupabaseResultsRepository
import com.terapia.terasenior.data.repository.therapy.SupabaseTherapySessionRepository
import com.terapia.terasenior.domain.model.therapy.ExerciseConfig
import com.terapia.terasenior.domain.usecase.admin.*
import com.terapia.terasenior.domain.usecase.patient.*
import com.terapia.terasenior.domain.usecase.results.SaveActivityResultUseCase
import com.terapia.terasenior.models.UserRole
import com.terapia.terasenior.repository.AuthRepository
import com.terapia.terasenior.treatment.ui.*
import com.terapia.terasenior.ui.admin.AdminEntitiesViewModel
import com.terapia.terasenior.ui.admin.AdminUsersViewModel
import com.terapia.terasenior.ui.admin.entities.AdminEntitiesScreen
import com.terapia.terasenior.ui.admin.users.AdminUsersScreen
import com.terapia.terasenior.ui.agenda.*
import com.terapia.terasenior.ui.login.LoginScreen
import com.terapia.terasenior.ui.patient.*
import com.terapia.terasenior.ui.reports.ReportsScreen
import com.terapia.terasenior.ui.therapy.*
import com.terapia.terasenior.ui.theme.TeraseniorTheme
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class Screen {
    LOGIN, THERAPY_DASHBOARD, CREATE_SESSION, SESSION_RUNNER, PATIENTS, PATIENT_DETAIL, AGENDA, APPOINTMENT_DETAIL, REPORTS, ADMIN_ENTITIES, ADMIN_USERS,
    NUMBER_SEARCH, ATTENTION_GAME, LANGUAGE_GAME, COLOR_SHAPE_SEQUENCE, COLOR_IDENTIFICATION, SIZE_ORDERING, TRACING, EXECUTIVE_FUNCTIONS, LITERACY
}

// Terasenior App Entry Point (v1.3.8 - Entity Branding & UX Fixes)
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
        var currentEntityLogoUrl by remember { mutableStateOf<String?>(null) }
        var activeSessionId by remember { mutableStateOf<String?>(null) }
        
        val scope = rememberCoroutineScope()
        val authRepository = remember { AuthRepository() }
        val entityRepository = remember { SupabaseEntityRepository() }
        
        // Repositorios y ViewModels compartidos para navegación fluida
        val therapyRepo = remember { SupabaseTherapySessionRepository() }
        val patientRepo = remember { SupabasePatientRepository() }
        val createSessionViewModel = remember { CreateSessionViewModel(therapyRepo, patientRepo) }

        // Cargar nombre de la entidad
        LaunchedEffect(currentUserProfile) {
            currentUserProfile?.let { profile ->
                if (profile.role != UserRole.SUPER_ADMIN && profile.entityId != null) {
                    entityRepository.getEntityById(profile.entityId)
                        .onSuccess { entity -> 
                            currentEntityName = entity?.name 
                            currentEntityLogoUrl = entity?.logoUrl
                        }
                } else {
                    currentEntityName = "Administración Global"
                    currentEntityLogoUrl = null
                }
            }
        }

        if (currentUserProfile == null) {
            LoginScreen { profile ->
                currentUserProfile = profile
                currentScreen = Screen.THERAPY_DASHBOARD
            }
        } else {
            val userRole = currentUserProfile?.role
            val canAdmin = userRole == UserRole.SUPER_ADMIN || 
                         userRole == UserRole.ADMIN_CENTRO

            Scaffold(
                topBar = {
                    if (currentScreen != Screen.SESSION_RUNNER) {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // LOGO DE LA ENTIDAD O ICONO DE USUARIO (v1.3.8)
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (!currentEntityLogoUrl.isNullOrBlank()) {
                                                KamelImage(
                                                    resource = { asyncPainterResource(currentEntityLogoUrl!!.trim()) },
                                                    contentDescription = "Logo Centro",
                                                    modifier = Modifier.fillMaxSize().padding(3.dp).clip(RoundedCornerShape(10.dp)),
                                                    onLoading = { CircularProgressIndicator(modifier = Modifier.size(16.dp)) },
                                                    onFailure = {
                                                        Icon(
                                                            Icons.Default.Business,
                                                            contentDescription = "Error carga",
                                                            modifier = Modifier.size(28.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.AccountCircle,
                                                    contentDescription = "Perfil",
                                                    modifier = Modifier.size(36.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
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
                    }
                },
                bottomBar = {
                    if (currentScreen != Screen.SESSION_RUNNER && currentScreen != Screen.CREATE_SESSION) {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == Screen.THERAPY_DASHBOARD,
                                onClick = { currentScreen = Screen.THERAPY_DASHBOARD },
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

                            NavigationBarItem(
                                selected = currentScreen == Screen.REPORTS,
                                onClick = { currentScreen = Screen.REPORTS },
                                icon = { Icon(Icons.Default.Description, contentDescription = null) },
                                label = { Text("Informes") }
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
                }
            ) { padding ->
                Box(modifier = Modifier.padding(if (currentScreen == Screen.SESSION_RUNNER) PaddingValues(0.dp) else padding)) {
                    when (currentScreen) {
                        Screen.LOGIN -> { /* No accesible */ }
                        
                        Screen.THERAPY_DASHBOARD -> {
                            val agendaRepo = remember { SupabaseAppointmentRepository() }
                            val dashboardViewModel = remember { TherapyDashboardViewModel(therapyRepo, agendaRepo) }
                            TherapyDashboardScreen(
                                viewModel = dashboardViewModel,
                                therapistId = currentUserProfile?.id ?: "",
                                onNewSessionClick = { 
                                    createSessionViewModel.resetWizard()
                                    currentScreen = Screen.CREATE_SESSION 
                                },
                                onNewPatientClick = {
                                    currentScreen = Screen.PATIENTS
                                    // Podemos añadir un flag para abrir el dialogo directamente si fuera necesario
                                },
                                onGoToAgenda = { currentScreen = Screen.AGENDA },
                                onAppointmentClick = { id ->
                                    selectedAppointmentId = id
                                    currentScreen = Screen.APPOINTMENT_DETAIL
                                },
                                onPatientClick = { id ->
                                    selectedPatientId = id
                                    currentScreen = Screen.PATIENT_DETAIL
                                }
                            )
                        }

                        Screen.REPORTS -> {
                            ReportsScreen()
                        }

                        Screen.CREATE_SESSION -> {
                            CreateSessionWizard(
                                viewModel = createSessionViewModel,
                                therapistId = currentUserProfile?.id ?: "",
                                onSessionCreated = { sessionId ->
                                    activeSessionId = sessionId
                                    currentScreen = Screen.SESSION_RUNNER
                                },
                                onCancel = { currentScreen = Screen.THERAPY_DASHBOARD }
                            )
                        }

                        Screen.SESSION_RUNNER -> {
                            val sessionId = activeSessionId ?: ""
                            val agendaRepo = remember { SupabaseAppointmentRepository() }
                            
                            val runnerViewModel = remember(sessionId) { 
                                SessionRunnerViewModel(sessionId, therapyRepo, agendaRepo) 
                            }
                            
                            SessionRunnerScreen(
                                viewModel = runnerViewModel,
                                onFinished = { currentScreen = Screen.THERAPY_DASHBOARD }
                            )
                        }
                        
                        Screen.NUMBER_SEARCH -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { NumberSearchViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame(3) }
                            NumberSearchGame(
                                viewModel = viewModel,
                                patientId = activeTherapyPatientId,
                                professionalId = currentUserProfile?.id,
                                appointmentId = null,
                                onBack = { currentScreen = Screen.THERAPY_DASHBOARD }
                            )
                        }

                        Screen.ATTENTION_GAME -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { VisualAttentionViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame("attention_different", 3) }
                            VisualAttentionGame(
                                viewModel = viewModel,
                                patientId = activeTherapyPatientId,
                                professionalId = currentUserProfile?.id,
                                appointmentId = null,
                                onBack = { currentScreen = Screen.THERAPY_DASHBOARD }
                            )
                        }

                        Screen.LANGUAGE_GAME -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { LanguageViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame("language_denomination", 3) }
                            LanguageGame(viewModel = viewModel, patientId = activeTherapyPatientId, professionalId = currentUserProfile?.id, appointmentId = null, onBack = { currentScreen = Screen.THERAPY_DASHBOARD })
                        }

                        Screen.COLOR_SHAPE_SEQUENCE -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { ColorShapeSequenceViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame(3) }
                            ColorShapeSequenceGame(viewModel = viewModel, patientId = activeTherapyPatientId, professionalId = currentUserProfile?.id, appointmentId = null, onBack = { currentScreen = Screen.THERAPY_DASHBOARD })
                        }

                        Screen.COLOR_IDENTIFICATION -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { ColorIdentificationViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame(3) }
                            ColorIdentificationGame(viewModel = viewModel, patientId = activeTherapyPatientId, professionalId = currentUserProfile?.id, appointmentId = null, onBack = { currentScreen = Screen.THERAPY_DASHBOARD })
                        }

                        Screen.SIZE_ORDERING -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { SizeOrderingViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame(3) }
                            SizeOrderingGame(viewModel = viewModel, patientId = activeTherapyPatientId, professionalId = currentUserProfile?.id, appointmentId = null, onBack = { currentScreen = Screen.THERAPY_DASHBOARD })
                        }

                        Screen.TRACING -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { TracingViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame(3) }
                            TracingGame(viewModel = viewModel, patientId = activeTherapyPatientId, professionalId = currentUserProfile?.id, appointmentId = null, onBack = { currentScreen = Screen.THERAPY_DASHBOARD })
                        }

                        Screen.EXECUTIVE_FUNCTIONS -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { ExecutiveFunctionsViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame("executive_planning_steps", 3) }
                            ExecutiveFunctionsGame(viewModel = viewModel, patientId = activeTherapyPatientId, professionalId = currentUserProfile?.id, appointmentId = null, onBack = { currentScreen = Screen.THERAPY_DASHBOARD })
                        }

                        Screen.LITERACY -> {
                            val resultsRepo = remember { SupabaseResultsRepository() }
                            val viewModel = remember { LiteracyViewModel(SaveActivityResultUseCase(resultsRepo)) }
                            LaunchedEffect(Unit) { viewModel.startNewGame(LiteracyVariation.TRACING_BASIC, 3) }
                            LiteracyGame(
                                viewModel = viewModel,
                                patientId = activeTherapyPatientId,
                                professionalId = currentUserProfile?.id,
                                appointmentId = null,
                                onBack = { currentScreen = Screen.THERAPY_DASHBOARD }
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
                                    val currentDay = (agendaState as? AgendaUiState.Success)?.selectedDate ?: (kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
                                    CreateAppointmentDialog(
                                        selectedDate = currentDay,
                                        patients = if (state is CreateAppointmentUiState.Success) state.patients else emptyList(),
                                        professionals = if (state is CreateAppointmentUiState.Success) state.professionals else emptyList(),
                                        existingAppointments = (agendaState as? AgendaUiState.Success)?.filteredAppointments?.map { it.first } ?: emptyList(),
                                        onDismiss = { showCreateDialog = false },
                                    onConfirm = { t, d, s, e, type, staff, attendees, exercises ->
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
                                                    selectedPatientIds = attendees,
                                                    plannedExercises = exercises
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
                            val agendaRepository = remember { SupabaseAppointmentRepository() }
                            
                            val viewModel = remember(appointmentId) { 
                                AppointmentDetailViewModel(appointmentId, agendaRepository, patientRepo) 
                            }
                            AppointmentDetailScreen(
                                viewModel = viewModel,
                                onStartSession = { appt, patient ->
                                    createSessionViewModel.startFromAppointment(appt, patient)
                                    currentScreen = Screen.CREATE_SESSION
                                },
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
                                    onConfirm = { f, l, p, b, ext, nif, adm, addr, city, cp, prov, ph, cName, cPh, notes, st -> 
                                        scope.launch {
                                            val entityId = currentUserProfile?.entityId 
                                                ?: entityRepository.getEntities().getOrNull()?.firstOrNull()?.id
                                                ?: ""
                                            
                                            createViewModel.createPatient(entityId, f, l, p, b, ext, nif, adm, addr, city, cp, prov, ph, cName, cPh, notes, st)
                                        }
                                    },
                                    isLoading = createUiState is CreatePatientUiState.Loading,
                                    errorMessage = (createUiState as? CreatePatientUiState.Error)?.message
                                )
                            }
                        }

                        Screen.PATIENT_DETAIL -> {
                            val patientId = selectedPatientId ?: ""
                            val repository = remember { SupabasePatientRepository() }
                            val resultsRepository = remember { SupabaseResultsRepository() }
                            val therapyRepo = remember { SupabaseTherapySessionRepository() }
                            val userRepo = remember { SupabaseUserProfileRepository() }
                            val viewModel = remember(patientId) { 
                                PatientDetailViewModel(
                                    patientId, 
                                    repository,
                                    resultsRepository,
                                    therapyRepo,
                                    userRepo,
                                    UpdatePatientUseCase(repository),
                                    UpdateTherapeuticProfileUseCase(repository)
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
