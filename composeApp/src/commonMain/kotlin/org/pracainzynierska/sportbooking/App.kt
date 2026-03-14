package org.pracainzynierska.sportbooking

// Importy UI
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

import org.pracainzynierska.sportbooking.theme.*
import org.pracainzynierska.sportbooking.screens.*
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

// --- DEFINICJA NAWIGACJI ---
sealed class Screen {
    data object Login : Screen()
    data object Register : Screen()
    data object List : Screen()
    data object MyBookings : Screen()
    data class Details(val facility: FacilityDto) : Screen()
    data class Scheduler(val facility: FacilityDto) : Screen()
    data class Manager(val facility: FacilityDto) : Screen()
    data object Admin : Screen()
    data object OwnerMain : Screen()
    data class OwnerFacilityDetails(val facility: FacilityDto) : Screen()
}

// --- GŁÓWNA APLIKACJA ---
@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(sharedModule, appModule)
    }) {
        MaterialTheme(colorScheme = AppColorScheme) {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
            val sessionManager: SessionManager = koinInject()
            val currentUser = sessionManager.currentUser.value
            val api: SportApi = koinInject()

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {

                // --- 1. NAGŁÓWEK (HEADER) ---
                // UWAGA: Kiedy Właściciel jest w OwnerDashboardScreen, ten górny pasek nadal tu jest.
                // Będziemy mogli go w przyszłości ukryć dla właściciela, ale na razie niech zostanie.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsSoccer, null, tint = RacingGreen)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("RezerwacjaBoisk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RacingGreen)
                                if (currentUser != null) {
                                    Text("Witaj, ${currentUser?.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }

                        // --- IKONY NAWIGACJI ---
                        if (currentUser != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                if (currentUser?.role == "ADMIN") {
                                    IconButton(onClick = { currentScreen = Screen.Admin }) {
                                        Icon(Icons.Default.ManageAccounts, "Panel Admina", tint = ErrorRed)
                                    }
                                }

                                // Przycisk kieruje teraz do nowej Skorupy (OwnerMain)
                                if (currentUser?.role == "FIELD_OWNER") {
                                    TextButton(
                                        onClick = { currentScreen = Screen.OwnerMain },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1565C0)),
                                    ){
                                        Text("PANEL WŁAŚCICIELA", fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(onClick = { currentScreen = Screen.List }) {
                                    Icon(Icons.Default.Home, "Obiekty", tint = RacingGreen)
                                }
                                IconButton(onClick = { currentScreen = Screen.MyBookings }) {
                                    Icon(Icons.Default.DateRange, "Rezerwacje", tint = RacingGreen)
                                }

                                IconButton(onClick = {
                                    sessionManager.currentUser.value = null
                                    currentScreen = Screen.Login
                                }) {
                                    Icon(Icons.Default.ExitToApp, "Wyloguj", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // --- 2. ZAWARTOŚĆ ---
                Box(Modifier.padding(16.dp)) {
                    when (val screen = currentScreen) {

                        is Screen.Login -> LoginScreen(
                            onLoginSuccess = { user ->
                                sessionManager.currentUser.value = user
                                // Rozwidlenie po logowaniu!
                                if (user.role == "FIELD_OWNER") {
                                    currentScreen = Screen.OwnerMain // Właściciel ląduje u siebie
                                } else {
                                    currentScreen = Screen.List // Klient leci do listy obiektów
                                }
                            },
                            onNavigateToRegister = { currentScreen = Screen.Register }
                        )

                        is Screen.Register -> RegisterScreen(
                            onRegisterSuccess = { currentScreen = Screen.Login },
                            onNavigateToLogin = { currentScreen = Screen.Login }
                        )

                        is Screen.List -> FacilitiesScreen(
                            onNavigateToDetails = { facility -> currentScreen = Screen.Details(facility) }
                        )

                        is Screen.MyBookings -> {
                            if (currentUser != null) MyBookingsScreen()
                            else currentScreen = Screen.Login
                        }

                        is Screen.Details -> {
                            FacilityDetailsScreen(
                                facility = screen.facility,
                                onNavigateToScheduler = { currentScreen = Screen.Scheduler(screen.facility) },
                                onBack = { currentScreen = Screen.List },
                            )
                        }

                        is Screen.Scheduler -> {
                            if (currentUser != null) SchedulerScreen(
                                facility = screen.facility,
                                onBack = { currentScreen = Screen.Details(screen.facility) }
                            )
                            else currentScreen = Screen.Login
                        }

                        // Ten menedżer kalendarza na razie zostaje tu globalnie
                        is Screen.Manager -> {
                            if (currentUser != null) FacilityManagerScreen(
                                facility = screen.facility,
                                onBack = { currentScreen = Screen.Details(screen.facility) }
                            )
                            else currentScreen = Screen.Login
                        }

                        is Screen.Admin -> {
                            if (currentUser?.role == "ADMIN") {
                                AdminPanelScreen(onLogout = {
                                    sessionManager.currentUser.value = null
                                    currentScreen = Screen.Login
                                })
                            } else currentScreen = Screen.List
                        }

                        // Rejestrujemy naszą nową Skorupę
                        is Screen.OwnerMain -> {
                            if (currentUser != null) {
                                OwnerDashboardScreen(
                                    onLogout = {
                                        sessionManager.currentUser.value = null
                                        currentScreen = Screen.Login
                                    },
                                    onNavigateToFacilityDetails = { selectedFacility ->
                                        currentScreen = Screen.OwnerFacilityDetails(selectedFacility)
                                    }
                                )
                            } else currentScreen = Screen.Login
                        }

                        // Ten ekran otwiera się po kliknięciu orlika na liście w zakładce "Obiekty"
                        is Screen.OwnerFacilityDetails -> {
                            if (currentUser != null) {
                                FacilityDetailsOwnerScreen(
                                    facility = screen.facility,
                                    onNavigateToManager = { currentScreen = Screen.Manager(screen.facility) },
                                    onBack = { currentScreen = Screen.OwnerMain } // Wróć do skorupy
                                )
                            } else currentScreen = Screen.Login
                        }
                    } // when
                } // Box
            } // Column
            } // Surface
        } // MaterialTheme
    } // KoinApplication
}