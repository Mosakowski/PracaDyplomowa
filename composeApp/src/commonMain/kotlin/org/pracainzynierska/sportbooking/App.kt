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
import org.pracainzynierska.sportbooking.FacilityDto

// --- DEFINICJA NAWIGACJI ---
sealed class Screen {
    data object Login : Screen()
    data object Register : Screen()
    data object List : Screen()
    data object MyBookings : Screen()
    data class Details(val facility: FacilityDto) : Screen()
    data class Scheduler(val facility: FacilityDto) : Screen()
    data class Manager(val facility: FacilityDto) : Screen()
    data object OwnerDashboard : Screen()
    data object Admin : Screen()
    data class OwnerFacilityDetails(val facility: FacilityDto) : Screen()
}

// --- GŁÓWNA APLIKACJA ---
@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = AppColorScheme) {

        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
        var currentUser by remember { mutableStateOf<AuthResponse?>(null) }
        val api = remember { SportApi() }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {

                // --- 1. NAGŁÓWEK (HEADER) ---
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
                                //  1. SPECJALNA IKONA TYLKO DLA ADMINA
                                if (currentUser?.role == "ADMIN") {
                                    IconButton(onClick = { currentScreen = Screen.Admin }) {
                                        // Ikonka "Manage Accounts" lub "Settings"
                                        Icon(Icons.Default.ManageAccounts, "Panel Admina", tint = ErrorRed)
                                    }
                                }

                                // 1.2 SPECJALNA IKONA TYLKO DLA WLASCICIELA BOISKA
                                if (currentUser?.role == "FIELD_OWNER") {
                                     TextButton(
                                         onClick = {
                                             currentScreen = Screen.OwnerDashboard
                                         },
                                         colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1565C0)),
                                     ){
                                         Text("PANEL WŁAŚCICIELA", fontWeight = FontWeight.Bold)
                                     }
                                }



                                //  2. IKONY STANDARDOWE
                                IconButton(onClick = { currentScreen = Screen.List }) {
                                    Icon(Icons.Default.Home, "Obiekty", tint = RacingGreen)
                                }
                                IconButton(onClick = { currentScreen = Screen.MyBookings }) {
                                    Icon(Icons.Default.DateRange, "Rezerwacje", tint = RacingGreen)
                                }

                                IconButton(onClick = {
                                    currentUser = null
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
                            api = api,
                            onLoginSuccess = { user ->
                                currentUser = user
                                // 👇 ZMIANA: Admin idzie tam gdzie wszyscy (na Listę), nie do panelu
                                currentScreen = Screen.List
                            },
                            onNavigateToRegister = { currentScreen = Screen.Register }
                        )

                        // ... Reszta ekranów bez zmian (Register, List, Details, etc.) ...
                        is Screen.Register -> RegisterScreen(
                            api = api,
                            onRegisterSuccess = { currentScreen = Screen.Login },
                            onNavigateToLogin = { currentScreen = Screen.Login }
                        )
                        is Screen.List -> FacilitiesScreen(
                            api = api,
                            currentUser = currentUser,
                            onNavigateToDetails = { facility -> currentScreen = Screen.Details(facility) }
                        )
                        is Screen.MyBookings -> {
                            if (currentUser != null) MyBookingsScreen(api, currentUser!!.userId)
                            else currentScreen = Screen.Login
                        }
                        is Screen.Details -> {
                            FacilityDetailsScreen(
                                facility = screen.facility,
                                currentUser = currentUser,
                                onNavigateToScheduler = { currentScreen = Screen.Scheduler(screen.facility) },
                                onBack = { currentScreen = Screen.List },
                            )
                        }
                        is Screen.Scheduler -> {
                            if (currentUser != null) SchedulerScreen(screen.facility, api, currentUser!!.userId, { currentScreen = Screen.Details(screen.facility) })
                            else currentScreen = Screen.Login
                        }
                        is Screen.Manager -> {
                            if (currentUser != null) FacilityManagerScreen(screen.facility, api, currentUser!!, { currentScreen = Screen.Details(screen.facility) })
                            else currentScreen = Screen.Login
                        }

                        // Ekran Admina
                        is Screen.Admin -> {
                            if (currentUser?.role == "ADMIN") {
                                AdminPanelScreen(onLogout = {
                                    // Kliknięcie wyloguj wewnątrz panelu (opcjonalne, bo jest w headerze)
                                    currentUser = null
                                    currentScreen = Screen.Login
                                })
                            } else currentScreen = Screen.List
                        }

                        is Screen.OwnerDashboard -> {
                            if (currentUser != null) {
                                OwnerDashboardScreen(
                                    api = api,
                                    currentUser = currentUser!!,
                                    onNavigateToManager = { facility ->
                                        currentScreen = Screen.OwnerFacilityDetails(facility)
                                    }
                                )
                            } else currentScreen = Screen.Login
                        }

                        is Screen.OwnerFacilityDetails -> {
                            if (currentUser != null) {
                                FacilityDetailsOwnerScreen(
                                    facility = screen.facility,
                                    currentUser = currentUser!!,
                                    onNavigateToManager = { currentScreen = Screen.Manager(screen.facility) }, // Stąd idziemy do panelu
                                    onBack = { currentScreen = Screen.OwnerDashboard } // Powrót na pulpit właściciela
                                )
                            } else currentScreen = Screen.Login
                        }
                    }
                }
            }
        }
    }
}