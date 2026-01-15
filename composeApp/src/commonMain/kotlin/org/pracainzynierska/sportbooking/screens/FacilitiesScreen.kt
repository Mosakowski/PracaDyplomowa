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
import org.pracainzynierska.sportbooking.components.AddFacilityDialog
import org.pracainzynierska.sportbooking.components.FacilityCard
import org.pracainzynierska.sportbooking.components.SportFilterChip

@Composable
fun FacilitiesScreen(
    api: SportApi,
    currentUser: AuthResponse?,
    onNavigateToDetails: (FacilityDto) -> Unit
) {
    var refreshTrigger by remember { mutableStateOf(0) }
    var facilities by remember { mutableStateOf<List<FacilityDto>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshTrigger) {
        try { facilities = api.getFacilities() } catch (e: Exception) { println(e) }
    }

    val filteredFacilities = remember(facilities, searchQuery, selectedSport) {
        facilities.filter { facility ->
            val matchesText = if (searchQuery.isBlank()) true else {
                facility.name.contains(searchQuery, ignoreCase = true) ||
                        facility.location.contains(searchQuery, ignoreCase = true)
            }
            val matchesSport = if (selectedSport == null) true else {
                facility.fields.any { it.type == selectedSport }
            }
            matchesText && matchesSport
        }
    }

    val allSports = remember(facilities) {
        facilities.flatMap { it.fields.map { field -> field.type } }.distinct().sorted()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Znajdź obiekt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                if (currentUser != null && currentUser.role == "FIELD_OWNER") {
                    FilledTonalButton(onClick = { showAddDialog = true }) { Text("+ Dodaj") }
                }
            }

            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
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
                    SportFilterChip("Wszystkie", (selectedSport == null)) { selectedSport = null }
                }
                items(allSports.size) { index ->
                    val sport = allSports[index]
                    val niceName = sport.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                    SportFilterChip(niceName, (selectedSport == sport)) {
                        selectedSport = if (selectedSport == sport) null else sport
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredFacilities.size) { index ->
                    FacilityCard(
                        facility = filteredFacilities[index],
                        onNavigate = { onNavigateToDetails(filteredFacilities[index]) }
                    )
                }
                if (filteredFacilities.isEmpty()) {
                    item { Text("Brak wyników.", modifier = Modifier.fillMaxWidth().padding(top = 24.dp), color = Color.Gray) }
                }
            }
        }

        if (showAddDialog && currentUser != null) {
            AddFacilityDialog(currentUser.userId, api, { showAddDialog = false }, { showAddDialog = false; refreshTrigger++ })
        }
    }
}