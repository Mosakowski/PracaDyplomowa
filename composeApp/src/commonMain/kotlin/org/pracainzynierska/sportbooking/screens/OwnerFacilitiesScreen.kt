package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.components.AddFacilityDialog
import org.pracainzynierska.sportbooking.theme.RacingGreen

import org.koin.compose.koinInject
import org.pracainzynierska.sportbooking.SessionManager

@Composable
fun OwnerFacilitiesScreen(
    onNavigateToManager: (FacilityDto) -> Unit // Przekazujemy dalej, żeby móc wejść w kalendarz
) {
    val api: SportApi = koinInject()
    val sessionManager: SessionManager = koinInject()
    val currentUser = sessionManager.currentUser.value!!
    var myFacilities by remember { mutableStateOf<List<FacilityDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Zmienne do wypłaszczonej nawigacji
    var selectedFacility by remember { mutableStateOf<FacilityDto?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(currentUser.userId, refreshTrigger) {
        try {
            isLoading = true
            val facilities = api.getMyFacilities(currentUser.userId)
            myFacilities = facilities

            // Jeśli mamy jakieś obiekty, a żaden nie jest wybrany (lub poprzedni został usunięty),
            // wybieramy pierwszy z brzegu jako domyślny.
            if (facilities.isNotEmpty() && (selectedFacility == null || facilities.none { it.id == selectedFacility?.id })) {
                selectedFacility = facilities.first()
            } else if (facilities.isEmpty()) {
                selectedFacility = null
            } else {
                // Aktualizujemy wybrany obiekt (np. po zmianie nazwy)
                selectedFacility = facilities.find { it.id == selectedFacility?.id }
            }
        } catch (e: Exception) {
            errorMessage = "Błąd: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- GÓRNY PASEK WYBORU OBIEKTU (Nawigacja płaska) ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (myFacilities.isNotEmpty()) {
                    // Rozwijane menu z wyborem obiektu
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F4F8))
                                .clickable { expandedDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = selectedFacility?.name ?: "Wybierz obiekt",
                                fontWeight = FontWeight.Bold,
                                color = RacingGreen
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Zmień obiekt", tint = RacingGreen)
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            myFacilities.forEach { facility ->
                                DropdownMenuItem(
                                    text = { Text(facility.name) },
                                    onClick = {
                                        selectedFacility = facility
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text("Brak obiektów", color = Color.Gray, fontWeight = FontWeight.Bold)
                }

                // Przycisk dodawania nowego obiektu
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nowy")
                }
            }
        }

        // --- GŁÓWNA ZAWARTOŚĆ (Detale wybranego obiektu) ---
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isLoading && myFacilities.isEmpty()) {
                // Pokazujemy loader tylko przy pierwszym ładowaniu
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else if (selectedFacility == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nie masz jeszcze żadnych obiektów.", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)) {
                        Text("Dodaj swój pierwszy orlik")
                    }
                }
            } else {
                // ŁADUJEMY TWÓJ GŁÓWNY PANEL ZARZĄDZANIA!
                FacilityDetailsOwnerScreen(
                    facility = selectedFacility!!,
                    onNavigateToManager = { onNavigateToManager(selectedFacility!!) },
                    onBack = { /* Ponieważ jesteśmy płascy, przycisk Back wewnątrz detali można będzie docelowo usunąć lub zignorować */ }
                )
            }
        }
    }

    if (showAddDialog) {
        AddFacilityDialog(currentUser.userId, api, { showAddDialog = false }, { showAddDialog = false; refreshTrigger++ })
    }
}