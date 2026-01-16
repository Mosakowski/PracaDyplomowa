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
fun FacilityDetailsScreen(
    facility: FacilityDto,
    currentUser: AuthResponse?,
    onNavigateToScheduler: () -> Unit,
    onBack: () -> Unit,
    onNavigateToManager: () -> Unit
) {
    val api = remember { SportApi() }
    var currentFacility by remember { mutableStateOf(facility) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Stany dialogów
    var showEditFacilityDialog by remember { mutableStateOf(false) }
    var showDeleteFacilityDialog by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) }

    var fieldToEdit by remember { mutableStateOf<FieldDto?>(null) }
    var fieldToDelete by remember { mutableStateOf<FieldDto?>(null) }

    // Odświeżanie danych po edycji
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            try {
                val all = api.getFacilities()
                val updated = all.find { it.id == facility.id }
                if (updated != null) {
                    currentFacility = updated
                } else {
                    // jeśli obiekt został usunięty to wracamy
                    onBack()
                }
            } catch (e: Exception) { println(e) }
        }
    }

    val isOwner = (currentUser != null && currentUser.userId == currentFacility.ownerId)
    val groupedFields = remember(currentFacility.fields) { currentFacility.fields.groupBy { it.type } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // --- 1. NAGŁÓWEK (Info o obiekcie + Przyciski Właściciela) ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(RacingGreen, RacingGreenLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).alpha(0.3f),
                        tint = Color.White
                    )

                    // Przycisk powrotu (musi zostać)
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                }

                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(currentFacility.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(currentFacility.location, color = Color.Gray)
                        }

                        // IKONY EDYCJI OBIEKTU (TYLKO DLA WŁAŚCICIELA)
                        if (isOwner) {
                            Row {
                                IconButton(onClick = { showEditFacilityDialog = true }) {
                                    Icon(Icons.Default.Edit, "Edytuj obiekt", tint = RacingGreen)
                                }
                                IconButton(onClick = { showDeleteFacilityDialog = true }) {
                                    Icon(Icons.Default.Delete, "Usuń obiekt", tint = ErrorRed)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Otwarte: ${currentFacility.openingTime} - ${currentFacility.closingTime}", style = MaterialTheme.typography.labelMedium)
                    currentFacility.description?.let { if (it.isNotEmpty()) Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp)) }
                }
            }
        }

        // --- 2. PANEL WŁAŚCICIELA (Statystyki) ---
        if (isOwner) {
            Button(
                onClick = onNavigateToManager,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Icon(Icons.Default.Assessment, null)
                Spacer(Modifier.width(8.dp))
                Text("Panel Zarządzania")
            }
            Spacer(Modifier.height(16.dp))
        }

        // --- 3. LISTA BOISK (OFERTA) ---
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Oferta sportowa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            //PRZYCISK DODAWANIA BOISKA (TYLKO DLA WŁAŚCICIELA)
            if (isOwner) {
                TextButton(onClick = { showAddFieldDialog = true }) {
                    Text("+ Dodaj boisko", color = RacingGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (currentFacility.fields.isEmpty()) {
            Text("Brak boisk w tym obiekcie.", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
        } else {
            // Widok dla Klienta (pogrupowany)
            if (!isOwner) {
                groupedFields.forEach { (type, fieldsList) ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when(type) {
                                    "PILKA_NOZNA" -> Icons.Default.SportsSoccer
                                    "KOSZYKOWKA" -> Icons.Default.SportsBasketball
                                    "KORT_TENISOWY" -> Icons.Default.SportsTennis
                                    else -> Icons.Default.Sports
                                }
                                Icon(icon, null, tint = RacingGreen)
                                Spacer(Modifier.width(16.dp))
                                Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)
                            }
                            Surface(color = RacingGreenLight, shape = RoundedCornerShape(16.dp)) {
                                Text("${fieldsList.size} szt.", color = Color.White, modifier = Modifier.padding(12.dp, 4.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else {
                //  Widok dla WŁAŚCICIELA (Lista edytowalna)
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
        }

        Spacer(Modifier.height(24.dp))

        // Przycisk rezerwacji (tylko jeśli są boiska)
        if (currentFacility.fields.isNotEmpty()) {
            Button(
                onClick = onNavigateToScheduler,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)
            ) {
                Text("Sprawdź dostępność i zarezerwuj")
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    // --- OBSŁUGA DIALOGÓW ---

    // 1. Edycja Obiektu
    if (showEditFacilityDialog && currentUser != null) {
        EditFacilityDialog(
            facility = currentFacility,
            userId = currentUser.userId,
            api = api,
            onDismiss = { showEditFacilityDialog = false },
            onSuccess = { showEditFacilityDialog = false; refreshTrigger++ }
        )
    }

    // 2. Usuwanie Obiektu
    if (showDeleteFacilityDialog && currentUser != null) {
        DeleteConfirmationDialog(
            facilityName = currentFacility.name,
            onConfirm = {
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
            },
            onDismiss = { showDeleteFacilityDialog = false }
        )
        val scope = rememberCoroutineScope()
        if (showDeleteFacilityDialog) {
            DeleteConfirmationDialog(
                facilityName = currentFacility.name,
                onConfirm = {
                    scope.launch {
                        if (api.deleteFacility(currentUser.userId, currentFacility.id)) {
                            showDeleteFacilityDialog = false
                            onBack()
                        }
                    }
                },
                onDismiss = { showDeleteFacilityDialog = false }
            )
        }
    }

    // 3. Dodawanie Boiska
    if (showAddFieldDialog && currentUser != null) {
        AddFieldDialog(
            facilityId = currentFacility.id,
            userId = currentUser.userId,
            api = api,
            onDismiss = { showAddFieldDialog = false },
            onSuccess = { showAddFieldDialog = false; refreshTrigger++ }
        )
    }

    // 4. Edycja Boiska
    if (fieldToEdit != null && currentUser != null) {
        EditFieldDialog(
            field = fieldToEdit!!,
            userId = currentUser.userId,
            api = api,
            onDismiss = { fieldToEdit = null },
            onSuccess = { fieldToEdit = null; refreshTrigger++ }
        )
    }

    // 5. Usuwanie Boiska
    if (fieldToDelete != null && currentUser != null) {
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { fieldToDelete = null },
            title = { Text("Usuń boisko") },
            text = { Text("Czy na pewno usunąć \"${fieldToDelete!!.name}\"?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    onClick = {
                        scope.launch {
                            if(api.deleteField(currentUser.userId, fieldToDelete!!.id)) {
                                refreshTrigger++
                                fieldToDelete = null
                            }
                        }
                    }
                ) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { fieldToDelete = null }) { Text("Anuluj") } }
        )
    }
}