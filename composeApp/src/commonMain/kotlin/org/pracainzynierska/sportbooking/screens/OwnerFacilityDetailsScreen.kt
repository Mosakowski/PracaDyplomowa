package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import org.pracainzynierska.sportbooking.*
import org.pracainzynierska.sportbooking.components.*
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.theme.RacingGreenLight
import org.pracainzynierska.sportbooking.viewmodels.OwnerFacilityDetailsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityDetailsOwnerScreen(
    facility: FacilityDto,
    onNavigateToManager: () -> Unit,
    onBack: () -> Unit,
    viewModel: OwnerFacilityDetailsViewModel = koinViewModel { parametersOf(facility) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val api: SportApi = koinInject()
    val sessionManager: SessionManager = koinInject()
    val currentUser = sessionManager.currentUser.value!!
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
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
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp).background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(RacingGreen, RacingGreenLight))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Sports, null, modifier = Modifier.size(100.dp).alpha(0.3f), tint = Color.White)
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Black.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    }

                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(uiState.facility.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(uiState.facility.location, color = Color.Gray)
                            }
                            Row {
                                IconButton(onClick = { viewModel.setShowEditFacilityDialog(true) }) { 
                                    Icon(Icons.Default.Edit, "Edytuj obiekt", tint = RacingGreen) 
                                }
                                IconButton(onClick = { viewModel.setShowDeleteFacilityDialog(true) }) { 
                                    Icon(Icons.Default.Delete, "Usuń obiekt", tint = ErrorRed) 
                                }
                            }
                        }
                    }
                }
            }

            // --- 2. ZARZĄDZANIE BOISKAMI ---
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Zasoby obiektu (Boiska)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.setShowAddFieldDialog(true) }) { 
                    Text("+ Dodaj boisko", color = RacingGreen, fontWeight = FontWeight.Bold) 
                }
            }

            if (uiState.facility.fields.isEmpty()) {
                Text("Musisz dodać co najmniej jedno boisko, by klienci mogli rezerwować termin.", color = ErrorRed, modifier = Modifier.padding(16.dp))
            } else {
                uiState.facility.fields.forEach { field ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(field.name, fontWeight = FontWeight.Bold)
                                Text("${field.price} PLN / ${field.minSlotDuration} min", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(field.type, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                            }
                            Row {
                                IconButton(onClick = { viewModel.setFieldToEdit(field) }) { 
                                    Icon(Icons.Default.Edit, "Edytuj", tint = RacingGreen) 
                                }
                                IconButton(onClick = { viewModel.setFieldToDelete(field) }) { 
                                    Icon(Icons.Default.Delete, "Usuń", tint = ErrorRed) 
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    // --- DIALOGI ADMINISTRACYJNE ---
    if (uiState.showEditFacilityDialog) {
        EditFacilityDialog(
            facility = uiState.facility,
            userId = currentUser.userId,
            api = api,
            onDismiss = { viewModel.setShowEditFacilityDialog(false) },
            onSuccess = { viewModel.setShowEditFacilityDialog(false); viewModel.refresh() }
        )
    }

    if (uiState.showAddFieldDialog) {
        AddFieldDialog(
            facilityId = uiState.facility.id,
            userId = currentUser.userId,
            api = api,
            onDismiss = { viewModel.setShowAddFieldDialog(false) },
            onSuccess = { viewModel.setShowAddFieldDialog(false); viewModel.refresh() }
        )
    }

    if (uiState.fieldToEdit != null) {
        EditFieldDialog(
            field = uiState.fieldToEdit!!,
            userId = currentUser.userId,
            api = api,
            onDismiss = { viewModel.setFieldToEdit(null) },
            onSuccess = { viewModel.setFieldToEdit(null); viewModel.refresh() }
        )
    }

    if (uiState.showDeleteFacilityDialog) {
        DeleteConfirmationDialog(
            facilityName = uiState.facility.name,
            onConfirm = { 
                scope.launch { 
                    if (viewModel.deleteFacility()) {
                        viewModel.setShowDeleteFacilityDialog(false)
                        onBack()
                    }
                } 
            },
            onDismiss = { viewModel.setShowDeleteFacilityDialog(false) }
        )
    }

    if (uiState.fieldToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.setFieldToDelete(null) },
            title = { Text("Usuń boisko") },
            text = { Text("Czy na pewno usunąć \"${uiState.fieldToDelete!!.name}\"?") },
            confirmButton = { 
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    onClick = { 
                        scope.launch { 
                            if(viewModel.deleteField(uiState.fieldToDelete!!.id)) { 
                                viewModel.refresh()
                                viewModel.setFieldToDelete(null) 
                            } 
                        } 
                    }
                ) { Text("Usuń") } 
            },
            dismissButton = { 
                TextButton(onClick = { viewModel.setFieldToDelete(null) }) { Text("Anuluj") } 
            }
        )
    }
}