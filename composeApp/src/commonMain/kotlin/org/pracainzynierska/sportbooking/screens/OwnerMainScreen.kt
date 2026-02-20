package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi

enum class OwnerTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Centrum", Icons.Default.Home),
    CALENDAR("Kalendarz", Icons.Default.DateRange),
    FACILITIES("Obiekty", Icons.Default.Settings)
}

@Composable
fun OwnerMainScreen(
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
                    OwnerTab.DASHBOARD -> {
                        Text("TU BĘDZIE NOWY DASHBOARD (KPIs, Wykresy, Najbliższe rezerwacje)")
                    }
                    OwnerTab.CALENDAR -> {
                        Text("TU BĘDZIE NOWY KALENDARZ (Z zakładkami na obiekty)")
                    }
                    OwnerTab.FACILITIES -> {
                        OwnerDashboardScreen(
                            api = api,
                            currentUser = currentUser,
                            onNavigateToManager = { kliknietyObiekt ->
                                // Gdy klikniemy w kartę, przekazujemy obiekt przez kabel do App.kt
                                onNavigateToFacilityDetails(kliknietyObiekt)
                            }
                        )
                    }
                }
            }
        }
    }
}