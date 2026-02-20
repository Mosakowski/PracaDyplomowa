package org.pracainzynierska.sportbooking.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Importy modeli z głównej paczki
import org.pracainzynierska.sportbooking.AddFacilityRequest
import org.pracainzynierska.sportbooking.AddFieldRequest
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.FieldDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.DayOfWeekIso
import org.pracainzynierska.sportbooking.DaySchedule
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen

// --- 1. DODAWANIE OBIEKTU ---
@Composable
fun AddFacilityDialog(userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                errorMessage?.let { Text(it, color = ErrorRed) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        // Poprawione wywołanie - tylko 3 parametry
                        if (api.addFacility(userId, AddFacilityRequest(name, location, description))) {
                            onSuccess()
                            onDismiss()
                        } else errorMessage = "Błąd dodawania"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// --- 2. EDYCJA OBIEKTU ---
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
                        // Poprawione wywołanie - tylko 3 parametry
                        if (api.updateFacility(userId, facility.id, AddFacilityRequest(name, location, description))) {
                            onSuccess()
                            onDismiss()
                        } else errorMessage = "Błąd edycji"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// --- 3. POTĘŻNY FORMULARZ: DODAWANIE BOISKA ---
@Composable
fun AddFieldDialog(facilityId: Int, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PILKA_NOZNA") }
    var minSlotStr by remember { mutableStateOf("60") }
    var maxDaysStr by remember { mutableStateOf("30") }
    var cancelHoursStr by remember { mutableStateOf("24") }

    // Słownik trzymający godziny otwarcia dla 7 dni (domyślnie otwarte 8-22)
    var schedule by remember {
        mutableStateOf(
            DayOfWeekIso.entries.associateWith { DaySchedule(isOpen = true, openTime = "08:00", closeTime = "22:00") }
        )
    }

    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun isValidTime(time: String?) = time != null && time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))

    // Używamy pełnego ekranu
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                Text("Nowe Boisko", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa (np. Kort nr 1)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena (PLN/h)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = minSlotStr, onValueChange = { minSlotStr = it }, label = { Text("Czas slotu (min)") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))
                Text("Rodzaj nawierzchni/sportu:")
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (selectedType == type), onClick = { selectedType = type })
                        Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Zasady rezerwacji:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = maxDaysStr, onValueChange = { maxDaysStr = it }, label = { Text("Dni w przód") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = cancelHoursStr, onValueChange = { cancelHoursStr = it }, label = { Text("Darmowe odwołanie (h)") }, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Godziny otwarcia tygodniowe:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                DayOfWeekIso.entries.forEach { day ->
                    val currentDaySchedule = schedule[day]!!
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(day.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = currentDaySchedule.isOpen,
                                    onCheckedChange = { isChecked ->
                                        schedule = schedule.toMutableMap().apply { this[day] = currentDaySchedule.copy(isOpen = isChecked) }
                                    }
                                )
                                Text(if(currentDaySchedule.isOpen) "Otwarte" else "Zamkn.", modifier = Modifier.padding(start = 8.dp))
                            }

                            if (currentDaySchedule.isOpen) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentDaySchedule.openTime ?: "",
                                        onValueChange = { newTime -> schedule = schedule.toMutableMap().apply { this[day] = currentDaySchedule.copy(openTime = newTime) } },
                                        label = { Text("Od") }, modifier = Modifier.weight(1f), singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = currentDaySchedule.closeTime ?: "",
                                        onValueChange = { newTime -> schedule = schedule.toMutableMap().apply { this[day] = currentDaySchedule.copy(closeTime = newTime) } },
                                        label = { Text("Do") }, modifier = Modifier.weight(1f), singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                errorMessage?.let { Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp)) }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    Button(onClick = {
                        scope.launch {
                            val price = priceStr.toDoubleOrNull()
                            val minDur = minSlotStr.toIntOrNull()
                            val maxD = maxDaysStr.toIntOrNull()
                            val cancelH = cancelHoursStr.toIntOrNull()

                            if (price == null || minDur == null || maxD == null || cancelH == null) {
                                errorMessage = "Błędne wartości liczbowe"
                                return@launch
                            }

                            val hasTimeErrors = schedule.values.any { it.isOpen && (!isValidTime(it.openTime) || !isValidTime(it.closeTime)) }
                            if (hasTimeErrors) { errorMessage = "Popraw format godzin (HH:MM)"; return@launch }

                            try {
                                val request = AddFieldRequest(
                                    facilityId = facilityId, name = name, fieldType = selectedType, price = price,
                                    minSlotDuration = minDur, maxDaysAdvance = maxD, cancellationHours = cancelH,
                                    status = "ACTIVE", weeklySchedule = schedule
                                )
                                if (api.addField(userId, request)) { onSuccess(); onDismiss() } else errorMessage = "Błąd dodawania"
                            } catch (e: Exception) { errorMessage = e.message }
                        }
                    }) { Text("Zapisz Boisko") }
                }
            }
        }
    }
}

// --- 4. POTĘŻNY FORMULARZ: EDYCJA BOISKA ---
@Composable
fun EditFieldDialog(field: FieldDto, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf(field.name) }
    var priceStr by remember { mutableStateOf(field.price.toString()) }
    var selectedType by remember { mutableStateOf(field.type) }
    var minSlotStr by remember { mutableStateOf(field.minSlotDuration.toString()) }
    var maxDaysStr by remember { mutableStateOf(field.maxDaysAdvance.toString()) }
    var cancelHoursStr by remember { mutableStateOf(field.cancellationHours.toString()) }
    var status by remember { mutableStateOf(field.status) }

    // Wczytujemy z bazy lub dajemy domyślny
    var schedule by remember {
        mutableStateOf(field.weeklySchedule ?: DayOfWeekIso.entries.associateWith { DaySchedule(isOpen = true, openTime = "08:00", closeTime = "22:00") })
    }

    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun isValidTime(time: String?) = time != null && time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxSize().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("Edycja Boiska", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena (PLN/h)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = minSlotStr, onValueChange = { minSlotStr = it }, label = { Text("Czas slotu (min)") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))
                Text("Rodzaj nawierzchni:")
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (selectedType == type), onClick = { selectedType = type })
                        Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status Boiska: ", fontWeight = FontWeight.Bold)
                    Switch(checked = status == "ACTIVE", onCheckedChange = { isChecked -> status = if(isChecked) "ACTIVE" else "INACTIVE" })
                    Text(if(status == "ACTIVE") "Aktywne" else "Nieaktywne")
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Zasady rezerwacji:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = maxDaysStr, onValueChange = { maxDaysStr = it }, label = { Text("Dni w przód") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = cancelHoursStr, onValueChange = { cancelHoursStr = it }, label = { Text("Darmowe odwołanie (h)") }, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Godziny otwarcia tygodniowe:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                DayOfWeekIso.entries.forEach { day ->
                    val currentDaySchedule = schedule[day]!!
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(day.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = currentDaySchedule.isOpen,
                                    onCheckedChange = { isChecked -> schedule = schedule.toMutableMap().apply { this[day] = currentDaySchedule.copy(isOpen = isChecked) } }
                                )
                                Text(if(currentDaySchedule.isOpen) "Otwarte" else "Zamkn.", modifier = Modifier.padding(start = 8.dp))
                            }

                            if (currentDaySchedule.isOpen) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentDaySchedule.openTime ?: "",
                                        onValueChange = { newTime -> schedule = schedule.toMutableMap().apply { this[day] = currentDaySchedule.copy(openTime = newTime) } },
                                        label = { Text("Od") }, modifier = Modifier.weight(1f), singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = currentDaySchedule.closeTime ?: "",
                                        onValueChange = { newTime -> schedule = schedule.toMutableMap().apply { this[day] = currentDaySchedule.copy(closeTime = newTime) } },
                                        label = { Text("Do") }, modifier = Modifier.weight(1f), singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                errorMessage?.let { Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp)) }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    Button(onClick = {
                        scope.launch {
                            val price = priceStr.toDoubleOrNull()
                            val minDur = minSlotStr.toIntOrNull()
                            val maxD = maxDaysStr.toIntOrNull()
                            val cancelH = cancelHoursStr.toIntOrNull()

                            if (price == null || minDur == null || maxD == null || cancelH == null) { errorMessage = "Błędne wartości liczbowe"; return@launch }
                            val hasTimeErrors = schedule.values.any { it.isOpen && (!isValidTime(it.openTime) || !isValidTime(it.closeTime)) }
                            if (hasTimeErrors) { errorMessage = "Popraw format godzin (HH:MM)"; return@launch }

                            try {
                                val request = AddFieldRequest(
                                    facilityId = 0, name = name, fieldType = selectedType, price = price,
                                    minSlotDuration = minDur, maxDaysAdvance = maxD, cancellationHours = cancelH,
                                    status = status, weeklySchedule = schedule
                                )
                                if (api.updateField(userId, field.id, request)) { onSuccess(); onDismiss() } else errorMessage = "Błąd edycji"
                            } catch (e: Exception) { errorMessage = e.message }
                        }
                    }) { Text("Zapisz Zmiany") }
                }
            }
        }
    }
}

// --- 5. USUWANIE ---
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

// --- 6. PŁATNOŚĆ MOCK ---
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