package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.viewmodels.AdminViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administratora", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        viewModel.logout()
                        onLogout() 
                    }) {
                        Icon(Icons.Default.ExitToApp, "Wyloguj")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ErrorRed, 
                    titleContentColor = Color.White, 
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Zarządzanie obiektami", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.loadData() }) { Icon(Icons.Default.Refresh, null) }
            }
            
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = RacingGreen) 
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.facilities.size) { index ->
                        val facility = uiState.facilities[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = RacingGreen.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Business, null, tint = RacingGreen) }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(facility.name, fontWeight = FontWeight.Bold)
                                    Text(facility.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { /* Brak akcji usuwania w wersji demo */ }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.LightGray)
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