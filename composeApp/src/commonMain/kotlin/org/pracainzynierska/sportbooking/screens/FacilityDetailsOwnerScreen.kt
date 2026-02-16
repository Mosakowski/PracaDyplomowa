package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.FieldDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.components.*
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.theme.RacingGreenLight

@Composable
fun FacilityDetailsOwnerScreen(
    facility: FacilityDto,
    currentUser: AuthResponse,
    onNavigateToManager: () -> Unit,
    onBack: () -> Unit
) {
    val api = remember { SportApi() }
    var currentFacility by remember { mutableStateOf(facility) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Stany dialogów administracyjnych
    var showEditFacilityDialog by remember { mutableStateOf(false) }
    var showDeleteFacilityDialog by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var fieldToEdit by remember { mutableStateOf<FieldDto?>(null) }
    var fieldToDelete by remember { mutableStateOf<FieldDto?>(null) }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            try {
                // Pobieramy Twoje obiekty z serwera, żeby zaktualizować ten konkretny
                val myFacilities = api.getMyFacilities(currentUser.userId)
                val updated = myFacilities.find { it.id == facility.id }
                if (updated != null) currentFacility = updated
                else onBack() // Usunięto z bazy? Wróć.
            } catch (e: Exception) { println(e) }
        }
    }

    Scaffold(
        floatingActionButton = {
            // Wielki pływający przycisk do Panelu Zarządzania
            ExtendedFloatingActionButton(
                onClick = onNavigateToManager,
                icon = { Icon(Icons.Default.Assessment, null) },
                text = { Text("PANEL ZARZĄDZANIA KALENDARZEM") },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {

            // --- 1. NAGŁÓWEK EDYTOWALNY ---
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(RacingGreen, RacingGreenLight))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Sports, null, modifier = Modifier.size(100.dp).alpha(0.3f), tint = Color.White)
                        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Black.copy(alpha = 0.2f), CircleShape)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    }

                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(currentFacility.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(currentFacility.location, color = Color.Gray)
                            }
                            Row {
                                IconButton(onClick = { showEditFacilityDialog = true }) { Icon(Icons.Default.Edit, "Edytuj obiekt", tint = RacingGreen) }
                                IconButton(onClick = { showDeleteFacilityDialog = true }) { Icon(Icons.Default.Delete, "Usuń obiekt", tint = ErrorRed) }
                            }
                        }
                    }
                }
            }

            // --- 2. ZARZĄDZANIE BOISKAMI (ZASOBAMI) ---
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Zasoby obiektu (Boiska)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showAddFieldDialog = true }) { Text("+ Dodaj boisko", color = RacingGreen, fontWeight = FontWeight.Bold) }
            }

            if (currentFacility.fields.isEmpty()) {
                Text("Musisz dodać co najmniej jedno boisko, by klienci mogli rezerwować termin.", color = ErrorRed, modifier = Modifier.padding(16.dp))
            } else {
                currentFacility.fields.forEach { field ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(field.name, fontWeight = FontWeight.Bold)
                                Text("${field.price} PLN / ${field.minSlotDuration} min", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(field.type, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                            }
                            Row {
                                IconButton(onClick = { fieldToEdit = field }) { Icon(Icons.Default.Edit, "Edytuj", tint = RacingGreen) }
                                IconButton(onClick = { fieldToDelete = field }) { Icon(Icons.Default.Delete, "Usuń", tint = ErrorRed) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(80.dp)) // Miejsce na przycisk pływający
        }
    }

    // --- DIALOGI ADMINISTRACYJNE (Z Twojego kodu) ---
    if (showEditFacilityDialog) EditFacilityDialog(currentFacility, currentUser.userId, api, { showEditFacilityDialog = false }, { showEditFacilityDialog = false; refreshTrigger++ })
    if (showAddFieldDialog) AddFieldDialog(currentFacility.id, currentUser.userId, api, { showAddFieldDialog = false }, { showAddFieldDialog = false; refreshTrigger++ })
    if (fieldToEdit != null) EditFieldDialog(fieldToEdit!!, currentUser.userId, api, { fieldToEdit = null }, { fieldToEdit = null; refreshTrigger++ })

    if (showDeleteFacilityDialog) {
        val scope = rememberCoroutineScope()
        DeleteConfirmationDialog(
            facilityName = currentFacility.name,
            onConfirm = { scope.launch { if (api.deleteFacility(currentUser.userId, currentFacility.id)) { showDeleteFacilityDialog = false; onBack() } } },
            onDismiss = { showDeleteFacilityDialog = false }
        )
    }

    if (fieldToDelete != null) {
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { fieldToDelete = null }, title = { Text("Usuń boisko") },
            text = { Text("Czy na pewno usunąć \"${fieldToDelete!!.name}\"?") },
            confirmButton = { Button(colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), onClick = { scope.launch { if(api.deleteField(currentUser.userId, fieldToDelete!!.id)) { refreshTrigger++; fieldToDelete = null } } }) { Text("Usuń") } },
            dismissButton = { TextButton(onClick = { fieldToDelete = null }) { Text("Anuluj") } }
        )
    }
}