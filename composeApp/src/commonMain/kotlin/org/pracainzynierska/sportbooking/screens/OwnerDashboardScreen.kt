package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.components.AddFacilityDialog

@Composable
fun OwnerDashboardScreen(
    api: SportApi,
    currentUser: AuthResponse, // model zalogowanego usera
    onNavigateToManager: (FacilityDto) -> Unit // Funkcja, która przenosi nas głębiej
) {
    // Tu będziemy trzymać pobrane obiekty (na start pusta lista)
    var myFacilities by remember { mutableStateOf<List<FacilityDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // flaga do pokazywania okienka (zabrałeś to z poprzedniego ekranu)
    var showAddDialog by remember { mutableStateOf(false) }

    // Mechanizm odświeżania (tłumaczę go niżej!)
    var refreshTrigger by remember { mutableStateOf(0) }

    // Dodajemy refreshTrigger do LaunchedEffect, żeby reagował na zmiany
    LaunchedEffect(currentUser.userId, refreshTrigger) {
        try {
            isLoading = true
            myFacilities = api.getMyFacilities(currentUser.userId)
        } catch (e: Exception) {
            errorMessage = "Nie udało się pobrać Twoich obiektów: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                // Kliknięcie przycisku zmienia flagę na true (pokazuje okienko)
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj obiekt")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Twoje obiekty",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red)
            } else if (myFacilities.isEmpty()) {
                Text(
                    "Nie masz jeszcze żadnych obiektów. Dodaj swój pierwszy obiekt!",
                    color = Color.Gray
                )
            } else {
                // Rysujemy listę obiektów Właściciela
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myFacilities) { facility ->
                        OwnerFacilityCard(
                            facility = facility,
                            // Kiedy właściciel kliknie kartę, wzywamy funkcję onNavigateToManager!
                            onClick = { onNavigateToManager(facility) }
                        )
                    }
                }
            }
        }
    }
    if (showAddDialog) {
        AddFacilityDialog(
            currentUser.userId,
            api,
            { showAddDialog = false },
            {
                showAddDialog = false
                refreshTrigger++
            }
        )
    }
}

// Komponent pojedynczej karty obiektu na liście
@Composable
fun OwnerFacilityCard(facility: FacilityDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(facility.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(facility.location, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Liczba boisk: ${facility.fields.size}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}