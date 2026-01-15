package org.pracainzynierska.sportbooking.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.AddFacilityRequest
import org.pracainzynierska.sportbooking.AddFieldRequest
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.FieldDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen

@Composable
fun AddFacilityDialog(userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("08:00") }
    var closeTime by remember { mutableStateOf("22:00") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun isValidTime(time: String) = time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy Obiekt") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Adres") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis") })
                Spacer(Modifier.height(16.dp))
                Text("Godziny otwarcia (HH:MM):", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = openTime, onValueChange = { openTime = it }, label = { Text("Od") }, modifier = Modifier.weight(1f), isError = !isValidTime(openTime))
                    OutlinedTextField(value = closeTime, onValueChange = { closeTime = it }, label = { Text("Do") }, modifier = Modifier.weight(1f), isError = !isValidTime(closeTime))
                }
                errorMessage?.let { Text(it, color = ErrorRed) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    if (!isValidTime(openTime) || !isValidTime(closeTime)) { errorMessage = "Popraw godziny"; return@launch }
                    try {
                        if (api.addFacility(userId, AddFacilityRequest(name, location, description, openTime, closeTime, 30))) { onSuccess(); onDismiss() } else errorMessage = "Błąd"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun AddFieldDialog(facilityId: Int, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PILKA_NOZNA") }
    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowe Boisko") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena (PLN/h)") })
                Spacer(Modifier.height(16.dp))
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (selectedType == type), onClick = { selectedType = type })
                        Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    }
                }
                errorMessage?.let { Text(it, color = ErrorRed) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        val price = priceStr.toDoubleOrNull() ?: return@launch
                        if (api.addField(userId, AddFieldRequest(facilityId, name, selectedType, price))) { onSuccess(); onDismiss() } else errorMessage = "Błąd"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun EditFacilityDialog(facility: FacilityDto, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf(facility.name) }
    var location by remember { mutableStateOf(facility.location) }
    var description by remember { mutableStateOf(facility.description ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj obiekt") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Adres") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis") })
                errorMessage?.let { Text(it, color = ErrorRed) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        if (api.updateFacility(userId, facility.id, AddFacilityRequest(name, location, description))) { onSuccess(); onDismiss() } else errorMessage = "Błąd"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun DeleteConfirmationDialog(facilityName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usuwanie") },
        text = { Text("Czy usunąć \"$facilityName\"?") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Usuń") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun EditFieldDialog(field: FieldDto, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf(field.name) }
    var priceStr by remember { mutableStateOf(field.price.toString()) }
    var selectedType by remember { mutableStateOf(field.type) }
    var minDurationStr by remember { mutableStateOf(field.minSlotDuration.toString()) }
    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj boisko") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena (PLN/h)") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = minDurationStr, onValueChange = { minDurationStr = it }, label = { Text("Czas slotu (min)") })
                Spacer(Modifier.height(16.dp))
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (selectedType == type), onClick = { selectedType = type })
                        Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    }
                }
                errorMessage?.let { Text(it, color = ErrorRed) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val price = priceStr.toDoubleOrNull(); val duration = minDurationStr.toIntOrNull()
                    if (price == null || duration == null) { errorMessage = "Popraw liczby"; return@launch }
                    try {
                        if (api.updateField(userId, field.id, AddFieldRequest(0, name, selectedType, price, duration))) onSuccess() else errorMessage = "Błąd"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun BlikPaymentDialog(totalPrice: Double, onDismiss: () -> Unit, onConfirmPayment: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = { Row(verticalAlignment = Alignment.CenterVertically) { Text("Płatność BLIK (MOCK)", fontWeight = FontWeight.Bold) } },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Kwota do zapłaty:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("$totalPrice PLN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = RacingGreen)
                Spacer(Modifier.height(16.dp))
                if (isProcessing) { CircularProgressIndicator(color = RacingGreen); Spacer(Modifier.height(8.dp)); Text("Potwierdź płatność w aplikacji banku...", style = MaterialTheme.typography.bodySmall) }
                else {
                    OutlinedTextField(value = code, onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) code = it }, label = { Text("Kod BLIK (6 cyfr)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), textStyle = TextStyle.Default.copy(textAlign = TextAlign.Center, fontSize = 24.sp, letterSpacing = 4.sp))
                    Spacer(Modifier.height(8.dp)); Text("Wpisz dowolny kod, np. 777123", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        confirmButton = { if (!isProcessing) { Button(onClick = { if (code.length == 6) { isProcessing = true; scope.launch { delay(2500); onConfirmPayment(); isProcessing = false } } }, enabled = code.length == 6, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen), modifier = Modifier.fillMaxWidth()) { Text("ZAPŁAĆ") } } },
        dismissButton = { if (!isProcessing) { TextButton(onClick = onDismiss) { Text("Anuluj") } } }
    )
}