package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.BookingDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.components.BookingCard
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen

import org.koin.compose.koinInject

import org.pracainzynierska.sportbooking.viewmodels.MyBookingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyBookingsScreen(
    viewModel: MyBookingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Twoja historia", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RacingGreen) }
            } else if (uiState.bookings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Text("Brak rezerwacji", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.bookings.size) { index ->
                        BookingCard(uiState.bookings[index], onCancel = { viewModel.showCancelConfirmation(uiState.bookings[index]) })
                    }
                }
            }
        }

        if (uiState.bookingToCancel != null) {
            AlertDialog(
                onDismissRequest = { viewModel.hideCancelConfirmation() },
                title = { Text("Odwołać rezerwację?") },
                text = { Text("Czy na pewno chcesz zrezygnować z rezerwacji na boisku \"${uiState.bookingToCancel?.fieldName}\"?") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.cancelBooking() }, 
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) { Text("Tak, odwołaj") }
                },
                dismissButton = { TextButton(onClick = { viewModel.hideCancelConfirmation() }) { Text("Nie, zostaw") } }
            )
        }
    }
}