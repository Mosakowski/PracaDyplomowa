package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.viewmodels.OwnerDashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToFacilityDetails: (FacilityDto) -> Unit,
    viewModel: OwnerDashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twój biznes", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        viewModel.logout()
                        onLogout() 
                    }) {
                        Icon(Icons.Default.ExitToApp, null)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Akcja dodawania obiektu */ },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Dodaj obiekt") },
                containerColor = RacingGreen,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Twoje obiekty", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = RacingGreen) 
                }
            } else if (uiState.myFacilities.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nie masz jeszcze żadnych obiektów.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.myFacilities.size) { index ->
                        val facility = uiState.myFacilities[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToFacilityDetails(facility) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = RacingGreen.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Business, null, tint = RacingGreen)
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(facility.name, fontWeight = FontWeight.Bold)
                                    Text(facility.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            
            uiState.errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}