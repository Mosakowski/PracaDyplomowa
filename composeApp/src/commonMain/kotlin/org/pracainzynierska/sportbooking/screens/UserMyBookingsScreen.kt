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

@Composable
fun MyBookingsScreen(api: SportApi, userId: Int) {
    var bookings by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var bookingToCancel by remember { mutableStateOf<BookingDto?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            try {
                isLoading = true
                val fetched = api.getMyBookings(userId)
                bookings = fetched.sortedByDescending { it.id }
            } catch (e: Exception) { println(e) } finally { isLoading = false }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Twoja historia", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RacingGreen) }
            } else if (bookings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Text("Brak rezerwacji", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(bookings.size) { index ->
                        BookingCard(bookings[index], onCancel = { bookingToCancel = bookings[index] })
                    }
                }
            }
        }

        if (bookingToCancel != null) {
            AlertDialog(
                onDismissRequest = { bookingToCancel = null },
                title = { Text("Odwołać rezerwację?") },
                text = { Text("Czy na pewno chcesz zrezygnować z rezerwacji na boisku \"${bookingToCancel?.fieldName}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                if (api.cancelBooking(userId, bookingToCancel!!.id)) refresh()
                                bookingToCancel = null
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) { Text("Tak, odwołaj") }
                },
                dismissButton = { TextButton(onClick = { bookingToCancel = null }) { Text("Nie, zostaw") } }
            )
        }
    }
}