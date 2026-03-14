package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import org.pracainzynierska.sportbooking.components.FacilityCard
import org.pracainzynierska.sportbooking.components.SportFilterChip
import org.pracainzynierska.sportbooking.theme.RacingGreen

import org.pracainzynierska.sportbooking.viewmodels.FacilitiesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FacilitiesScreen(
    onNavigateToDetails: (FacilityDto) -> Unit,
    viewModel: FacilitiesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Znajdź obiekt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = RacingGreen)
                }
            }

            OutlinedTextField(
                value = uiState.searchQuery, onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Miasto, ulica lub nazwa...") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                item {
                    SportFilterChip("Wszystkie", (uiState.selectedSport == null)) { viewModel.onSportSelected(null) }
                }
                items(uiState.allSports.size) { index ->
                    val sport = uiState.allSports[index]
                    val niceName = sport.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                    SportFilterChip(niceName, (uiState.selectedSport == sport)) {
                        viewModel.onSportSelected(sport)
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(uiState.filteredFacilities.size) { index ->
                    FacilityCard(
                        facility = uiState.filteredFacilities[index],
                        onNavigate = { onNavigateToDetails(uiState.filteredFacilities[index]) }
                    )
                }
                if (uiState.filteredFacilities.isEmpty() && !uiState.isLoading) {
                    item { Text("Brak wyników.", modifier = Modifier.fillMaxWidth().padding(top = 24.dp), color = Color.Gray) }
                }
            }
        }
    }
}