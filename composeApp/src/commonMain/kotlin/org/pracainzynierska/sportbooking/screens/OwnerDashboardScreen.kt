package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi

enum class OwnerTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Centrum", Icons.Default.Home),
    CALENDAR("Kalendarz", Icons.Default.DateRange),
    FACILITIES("Obiekty", Icons.Default.Settings),
    BUSINESS("Biznes", Icons.Default.Insights)
}

@Composable
fun OwnerDashboardScreen( // Twoja główna skorupa
    api: SportApi,
    currentUser: AuthResponse,
    onLogout: () -> Unit,
    onNavigateToFacilityDetails: (FacilityDto) -> Unit
) {
    var currentTab by remember { mutableStateOf(OwnerTab.DASHBOARD) }

    Scaffold { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // 1. LEWA STRONA: Pasek Boczny
            NavigationRail(
                header = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Wyloguj")
                    }
                }
            ) {
                OwnerTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = (currentTab == tab),
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }

            // 2. PRAWA STRONA: Główna treść
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {

                    // ZAKŁADKA 1: CENTRUM (KOKPIT)
                    OwnerTab.DASHBOARD -> {
                        // Tutaj w przyszłości wkleimy kod z wykresem 68% i kalendarzykiem.
                        // Na ten moment zostawiamy placeholder, żeby sprawdzić czy nawigacja nie wybucha.
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Właściwy kokpit (68% i aktywność) pojawi się tutaj.", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    // ZAKŁADKA 2: KALENDARZ
                    OwnerTab.CALENDAR -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Aby zarządzać kalendarzem konkretnego boiska...", style = MaterialTheme.typography.titleMedium)
                            Text("Przejdź do zakładki 'Obiekty', wybierz orlik i kliknij panel kalendarza.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // ZAKŁADKA 3: OBIEKTY (WYPŁASZCZONA INFRASTRUKTURA)
                    OwnerTab.FACILITIES -> {
                        OwnerFacilitiesScreen(
                            api = api,
                            currentUser = currentUser,
                            // Gdy z wnętrza detali ktoś kliknie "PANEL KALENDARZA", przekazujemy to wyżej!
                            onNavigateToManager = { kliknietyObiekt ->
                                onNavigateToFacilityDetails(kliknietyObiekt) // To odpali w App.kt OwnerCalendar
                            }
                        )
                    }

                    // ZAKŁADKA 4: BIZNES
                    OwnerTab.BUSINESS -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Moduł Business Intelligence", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    }
}