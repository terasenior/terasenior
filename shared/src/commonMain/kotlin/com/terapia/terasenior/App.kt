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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    NUMBER_SEARCH, ATTENTION_GAME, LANGUAGE_GAME, SHAPE_FITTING
}

// Terasenior App Entry Point (v1.3.44 - Memory Stability Update)
@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
fun App() {
    TeraseniorTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        
        var currentUserProfile by remember { mutableStateOf<com.terapia.terasenior.models.Profile?>(null) }
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var selectedPatientId by remember { mutableStateOf<String?>(null) }
        var activeTherapyPatientId by remember { mutableStateOf<String?>(null) }
        var selectedAppointmentId by remember { mutableStateOf<String?>(null) }
        var currentEntityName by remember { mutableStateOf<String?>(null) }
        var currentCenterName by remember { mutableStateOf<String?>(null) }
        var currentEntityLogoUrl by remember { mutableStateOf<String?>(null) }
        var activeSessionId by remember { mutableStateOf<String?>(null) }
        
        val authRepository = remember { AuthRepository() }
        val entityRepository = remember { SupabaseEntityRepository() }
        val therapyRepo = remember { SupabaseTherapySessionRepository() }
        val patientRepo = remember { SupabasePatientRepository() }
        val createSessionViewModel = remember { CreateSessionViewModel(therapyRepo, patientRepo) }

        // Cargar nombre de la entidad
        LaunchedEffect(currentUserProfile) {
            currentUserProfile?.let { profile ->
                currentCenterName = profile.centerName
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
            val canAdmin = userRole == UserRole.SUPER_ADMIN || userRole == UserRole.ADMIN_CENTRO

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = currentScreen != Screen.SESSION_RUNNER,
                drawerContent = {
                    ModalDrawerSheet {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "TERASENIOR", 
                            modifier = Modifier.padding(16.dp), 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()
                        
                        NavigationDrawerItem(
                            label = { Text("Panel de Terapia") },
                            selected = currentScreen == Screen.THERAPY_DASHBOARD,
                            onClick = { currentScreen = Screen.THERAPY_DASHBOARD; scope.launch { drawerState.close() } },
                            icon = { Icon(Icons.Default.Psychology, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )

                        NavigationDrawerItem(
                            label = { Text("Agenda") },
                            selected = currentScreen == Screen.AGENDA || currentScreen == Screen.APPOINTMENT_DETAIL,
                            onClick = { currentScreen = Screen.AGENDA; scope.launch { drawerState.close() } },
                            icon = { Icon(Icons.Default.DateRange, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )

                        NavigationDrawerItem(
                            label = { Text("Pacientes") },
                            selected = currentScreen == Screen.PATIENTS || currentScreen == Screen.PATIENT_DETAIL,
                            onClick = { currentScreen = Screen.PATIENTS; scope.launch { drawerState.close() } },
                            icon = { Icon(Icons.Default.People, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )

                        NavigationDrawerItem(
                            label = { Text("Informes") },
                            selected = currentScreen == Screen.REPORTS,
                            onClick = { currentScreen = Screen.REPORTS; scope.launch { drawerState.close() } },
                            icon = { Icon(Icons.Default.Description, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )

                        if (canAdmin) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("Administración", modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            
                            NavigationDrawerItem(
                                label = { Text("Gestión Centros") },
                                selected = currentScreen == Screen.ADMIN_ENTITIES,
                                onClick = { currentScreen = Screen.ADMIN_ENTITIES; scope.launch { drawerState.close() } },
                                icon = { Icon(Icons.Default.Business, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Gestión Usuarios") },
                                selected = currentScreen == Screen.ADMIN_USERS,
                                onClick = { currentScreen = Screen.ADMIN_USERS; scope.launch { drawerState.close() } },
                                icon = { Icon(Icons.Default.People, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }

                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                scope.launch {
                                    authRepository.logout()
                                    currentUserProfile = null
                                    currentEntityName = null
                                    currentScreen = Screen.LOGIN
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Cerrar Sesión")
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        if (currentScreen != Screen.SESSION_RUNNER) {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                                    }
                                },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        key(currentEntityLogoUrl) {
                                            Surface(
                                                modifier = Modifier.size(40.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (!currentEntityLogoUrl.isNullOrBlank()) {
                                                        KamelImage(
                                                            resource = { asyncPainterResource(currentEntityLogoUrl!!.trim()) },
                                                            contentDescription = "Logo Centro",
                                                            modifier = Modifier.fillMaxSize().padding(2.dp).clip(RoundedCornerShape(6.dp)),
                                                            onLoading = { CircularProgressIndicator(modifier = Modifier.size(12.dp)) },
                                                            onFailure = { Icon(Icons.Default.Business, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) }
                                                        )
                                                    } else {
                                                        Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(currentUserProfile?.fullName ?: "Usuario", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                            Text(
                                                text = buildString {
                                                    append(currentEntityName ?: "Cargando...")
                                                    if (!currentCenterName.isNullOrBlank()) append(" - $currentCenterName")
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            )
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(if (currentScreen == Screen.SESSION_RUNNER) PaddingValues(0.dp) else padding)) {
                        when (currentScreen) {
                            Screen.LOGIN -> { /* No accesible */ }
                            Screen.THERAPY_DASHBOARD -> {
                                val agendaRepo = remember { SupabaseAppointmentRepository() }
                                TherapyDashboardScreen(
                                    viewModel = remember { TherapyDashboardViewModel(therapyRepo, agendaRepo) },
                                    therapistId = currentUserProfile?.id ?: "",
                                    onNewSessionClick = { createSessionViewModel.resetWizard(); currentScreen = Screen.CREATE_SESSION },
                                    onStandardizedSessionClick = { 
                                        createSessionViewModel.resetWizard()
                                        createSessionViewModel.startQuickEvaluationFlow()
                                        currentScreen = Screen.CREATE_SESSION 
                                    },
                                    onNewPatientClick = { currentScreen = Screen.PATIENTS },
                                    onGoToAgenda = { currentScreen = Screen.AGENDA },
                                    onAppointmentClick = { selectedAppointmentId = it; currentScreen = Screen.APPOINTMENT_DETAIL },
                                    onPatientClick = { selectedPatientId = it; currentScreen = Screen.PATIENT_DETAIL }
                                )
                            }
                            Screen.REPORTS -> ReportsScreen()
                            Screen.CREATE_SESSION -> CreateSessionWizard(
                                viewModel = createSessionViewModel,
                                therapistId = currentUserProfile?.id ?: "",
                                onSessionCreated = { activeSessionId = it; currentScreen = Screen.SESSION_RUNNER },
                                onCancel = { currentScreen = Screen.THERAPY_DASHBOARD }
                            )
                            Screen.SESSION_RUNNER -> {
                                val sessionId = activeSessionId ?: ""
                                val runnerViewModel = remember(sessionId) { SessionRunnerViewModel(sessionId, therapyRepo, SupabaseAppointmentRepository()) }
                                SessionRunnerScreen(viewModel = runnerViewModel, onFinished = { currentScreen = Screen.THERAPY_DASHBOARD })
                            }
                            Screen.PATIENTS -> {
                                val repository = remember { SupabasePatientRepository() }
                                PatientListScreen(
                                    viewModel = remember { PatientListViewModel(GetPatientsUseCase(repository)) },
                                    onPatientClick = { selectedPatientId = it; currentScreen = Screen.PATIENT_DETAIL },
                                    onAddPatientClick = { /* Handled in screen */ }
                                )
                            }
                            Screen.PATIENT_DETAIL -> {
                                val patientId = selectedPatientId ?: ""
                                val resultsRepo = remember { SupabaseResultsRepository() }
                                val userRepo = remember { SupabaseUserProfileRepository() }
                                val viewModel = remember(patientId) { 
                                    PatientDetailViewModel(patientId, patientRepo, resultsRepo, therapyRepo, userRepo, UpdatePatientUseCase(patientRepo), UpdateTherapeuticProfileUseCase(patientRepo)) 
                                }
                                PatientDetailScreen(viewModel = viewModel, onBack = { currentScreen = Screen.PATIENTS })
                            }
                            Screen.AGENDA -> {
                                val agendaRepo = remember { SupabaseAppointmentRepository() }
                                val viewModel = remember { AgendaViewModel(agendaRepo) }
                                AgendaScreen(
                                    viewModel = viewModel,
                                    onAddAppointmentClick = { /* Handled in screen */ },
                                    onAppointmentClick = { selectedAppointmentId = it; currentScreen = Screen.APPOINTMENT_DETAIL }
                                )
                            }
                            Screen.APPOINTMENT_DETAIL -> {
                                val appointmentId = selectedAppointmentId ?: ""
                                val viewModel = remember(appointmentId) { AppointmentDetailViewModel(appointmentId, SupabaseAppointmentRepository(), patientRepo) }
                                AppointmentDetailScreen(
                                    viewModel = viewModel,
                                    onStartSession = { appt, patient -> createSessionViewModel.startFromAppointment(appt, patient); currentScreen = Screen.CREATE_SESSION },
                                    onBack = { currentScreen = Screen.AGENDA }
                                )
                            }
                            Screen.ADMIN_ENTITIES -> AdminEntitiesScreen(remember { AdminEntitiesViewModel(GetEntitiesUseCase(entityRepository), CreateEntityUseCase(entityRepository), UpdateEntityUseCase(entityRepository), DeleteEntityUseCase(entityRepository), CheckEntityDependenciesUseCase(entityRepository), DeactivateEntityUseCase(entityRepository)) })
                            Screen.ADMIN_USERS -> AdminUsersScreen(remember { AdminUsersViewModel(GetUserProfilesUseCase(SupabaseUserProfileRepository()), GetEntitiesUseCase(entityRepository), authRepository) })
                            
                            // Juegos directos (Demo)
                            Screen.NUMBER_SEARCH -> NumberSearchGame(remember { NumberSearchViewModel(SaveActivityResultUseCase(SupabaseResultsRepository())) }, null, null, null, "Demostración", { currentScreen = Screen.THERAPY_DASHBOARD })
                            Screen.ATTENTION_GAME -> VisualAttentionGame(remember { VisualAttentionViewModel(SaveActivityResultUseCase(SupabaseResultsRepository())) }, null, null, null, { currentScreen = Screen.THERAPY_DASHBOARD })
                            Screen.LANGUAGE_GAME -> LanguageGame(remember { LanguageViewModel(SaveActivityResultUseCase(SupabaseResultsRepository())) }, null, null, null, { currentScreen = Screen.THERAPY_DASHBOARD })
                            Screen.SHAPE_FITTING -> ShapeFittingGame(remember { ShapeFittingViewModel(SaveActivityResultUseCase(SupabaseResultsRepository())) }, null, null, null, { currentScreen = Screen.THERAPY_DASHBOARD })
                        }
                    }
                }
            }
        }
    }
}
