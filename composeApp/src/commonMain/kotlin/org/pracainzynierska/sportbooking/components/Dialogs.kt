package org.pracainzynierska.sportbooking.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

// --- HELPERY DO ZARZĄDZANIA CZASEM NA SUWAKACH ---
val polishDays = mapOf(
    DayOfWeekIso.MONDAY to "Poniedziałek",
    DayOfWeekIso.TUESDAY to "Wtorek",
    DayOfWeekIso.WEDNESDAY to "Środa",
    DayOfWeekIso.THURSDAY to "Czwartek",
    DayOfWeekIso.FRIDAY to "Piątek",
    DayOfWeekIso.SATURDAY to "Sobota",
    DayOfWeekIso.SUNDAY to "Niedziela"
)

fun timeToFloat(time: String?): Float {
    if (time == null) return 8f
    val parts = time.split(":")
    if (parts.size != 2) return 8f
    val h = parts[0].toFloatOrNull() ?: 8f
    val m = parts[1].toFloatOrNull() ?: 0f
    return h + (m / 60f)
}

fun floatToTime(value: Float): String {
    val h = value.toInt()
    val m = ((value - h) * 60).roundToInt()
    val finalH = if (h == 24 && m == 0) 23 else h
    val finalM = if (h == 24 && m == 0) 59 else m
    return "${finalH.toString().padStart(2, '0')}:${finalM.toString().padStart(2, '0')}"
}

@Composable
fun DayScheduleRow(
    day: DayOfWeekIso,
    schedule: DaySchedule,
    onScheduleChange: (DaySchedule) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(polishDays[day] ?: day.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(
                    checked = schedule.isOpen,
                    onCheckedChange = { isChecked -> onScheduleChange(schedule.copy(isOpen = isChecked)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = RacingGreen, checkedTrackColor = RacingGreen.copy(alpha = 0.5f))
                )
                Text(if (schedule.isOpen) "Otwarte" else "Zamknięte", modifier = Modifier.padding(start = 8.dp).width(70.dp), style = MaterialTheme.typography.bodySmall)
            }

            if (schedule.isOpen) {
                val start = timeToFloat(schedule.openTime)
                val end = timeToFloat(schedule.closeTime)

                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Od: ${floatToTime(start)}", color = RacingGreen, fontWeight = FontWeight.Bold)
                    Text("Do: ${floatToTime(end)}", color = RacingGreen, fontWeight = FontWeight.Bold)
                }

                RangeSlider(
                    value = start..end,
                    onValueChange = { range ->
                        onScheduleChange(schedule.copy(
                            openTime = floatToTime(range.start),
                            closeTime = floatToTime(range.endInclusive)
                        ))
                    },
                    valueRange = 0f..24f,
                    steps = 47,
                    colors = SliderDefaults.colors(
                        thumbColor = RacingGreen,
                        activeTrackColor = RacingGreen,
                        inactiveTrackColor = Color.LightGray
                    )
                )
            }
        }
    }
}

// --- 1. DODAWANIE OBIEKTU (Zaktualizowane) ---
@Composable
fun AddFacilityDialog(userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // NOWE POLA:
    var contactPhone by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("ACTIVE") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy Obiekt", color = RacingGreen, fontWeight = FontWeight.Bold) },
        text = {
            // Dodano verticalScroll, bo formularz urósł!
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa obiektu") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis (opcjonalnie)") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Telefon kontaktowy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone) // Wymusza klawiaturę numeryczną na telefonie
                )
                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Link do zdjęcia (URL)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = status == "ACTIVE",
                        onCheckedChange = { isChecked -> status = if(isChecked) "ACTIVE" else "INACTIVE" },
                        colors = SwitchDefaults.colors(checkedThumbColor = RacingGreen)
                    )
                    Text(if(status == "ACTIVE") "Aktywny" else "Nieaktywny", modifier = Modifier.padding(start = 8.dp))
                }

                errorMessage?.let { Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        // Aktualizacja żądania do serwera o nowe pola
                        val request = AddFacilityRequest(
                            name = name, location = location, description = description.ifBlank { null },
                            contactPhone = contactPhone.ifBlank { null }, photoUrl = photoUrl.ifBlank { null }, status = status
                        )
                        if (api.addFacility(userId, request)) {
                            onSuccess()
                            onDismiss()
                        } else errorMessage = "Błąd dodawania"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// --- 2. EDYCJA OBIEKTU (Zaktualizowane) ---
@Composable
fun EditFacilityDialog(facility: FacilityDto, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf(facility.name) }
    var location by remember { mutableStateOf(facility.location) }
    var description by remember { mutableStateOf(facility.description ?: "") }
    // NOWE POLA (inicjalizowane z danych z backendu):
    var contactPhone by remember { mutableStateOf(facility.contactPhone ?: "") }
    var photoUrl by remember { mutableStateOf(facility.photoUrl ?: "") }
    var status by remember { mutableStateOf(facility.status) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj obiekt", color = RacingGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Telefon kontaktowy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Link do zdjęcia (URL)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = status == "ACTIVE",
                        onCheckedChange = { isChecked -> status = if(isChecked) "ACTIVE" else "INACTIVE" },
                        colors = SwitchDefaults.colors(checkedThumbColor = RacingGreen)
                    )
                    Text(if(status == "ACTIVE") "Aktywny" else "Nieaktywny", modifier = Modifier.padding(start = 8.dp))
                }

                errorMessage?.let { Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        val request = AddFacilityRequest(
                            name = name, location = location, description = description.ifBlank { null },
                            contactPhone = contactPhone.ifBlank { null }, photoUrl = photoUrl.ifBlank { null }, status = status
                        )
                        if (api.updateFacility(userId, facility.id, request)) {
                            onSuccess()
                            onDismiss()
                        } else errorMessage = "Błąd edycji"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)) { Text("Zapisz Zmiany") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// --- 3. POTĘŻNY FORMULARZ: DODAWANIE BOISKA (Zaktualizowane) ---
@Composable
fun AddFieldDialog(facilityId: Int, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PILKA_NOZNA") }
    var minSlotStr by remember { mutableStateOf("60") }
    var maxDaysStr by remember { mutableStateOf("30") }
    var cancelHoursStr by remember { mutableStateOf("24") }

    // NOWE POLA DLA BOISKA
    var description by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

    var schedule by remember {
        mutableStateOf(DayOfWeekIso.entries.associateWith { DaySchedule(isOpen = true, openTime = "08:00", closeTime = "22:00") })
    }

    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxSize().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("Nowe Boisko", style = MaterialTheme.typography.headlineSmall, color = RacingGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa (np. Kort nr 1)") }, modifier = Modifier.fillMaxWidth())
                // Dodane pole Opis boiska
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis boiska (np. Sztuczna trawa)") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena (PLN/h)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = minSlotStr, onValueChange = { minSlotStr = it }, label = { Text("Czas slotu (min)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                // Dodane pole na zdjęcie boiska
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text("Link do zdjęcia boiska (URL)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

                Spacer(Modifier.height(8.dp))
                Text("Rodzaj nawierzchni/sportu:", fontWeight = FontWeight.Bold)
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (selectedType == type), onClick = { selectedType = type }, colors = RadioButtonDefaults.colors(selectedColor = RacingGreen))
                        Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Zasady rezerwacji:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = maxDaysStr, onValueChange = { maxDaysStr = it }, label = { Text("Dni w przód") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = cancelHoursStr, onValueChange = { cancelHoursStr = it }, label = { Text("Darmowe odwołanie (h)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Godziny otwarcia tygodniowe:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                DayOfWeekIso.entries.forEach { day ->
                    DayScheduleRow(
                        day = day,
                        schedule = schedule[day]!!,
                        onScheduleChange = { updatedSchedule ->
                            schedule = schedule.toMutableMap().apply { this[day] = updatedSchedule }
                        }
                    )
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

                            try {
                                val request = AddFieldRequest(
                                    facilityId = facilityId, name = name, fieldType = selectedType, price = price,
                                    minSlotDuration = minDur, maxDaysAdvance = maxD, cancellationHours = cancelH,
                                    status = "ACTIVE", weeklySchedule = schedule,
                                    // Przekazanie nowych zmiennych
                                    description = description.ifBlank { null }, photoUrl = photoUrl.ifBlank { null }
                                )
                                if (api.addField(userId, request)) { onSuccess(); onDismiss() } else errorMessage = "Błąd dodawania"
                            } catch (e: Exception) { errorMessage = e.message }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)) { Text("Zapisz Boisko") }
                }
            }
        }
    }
}

// --- 4. POTĘŻNY FORMULARZ: EDYCJA BOISKA (Zaktualizowane) ---
@Composable
fun EditFieldDialog(field: FieldDto, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf(field.name) }
    var priceStr by remember { mutableStateOf(field.price.toString()) }
    var selectedType by remember { mutableStateOf(field.type) }
    var minSlotStr by remember { mutableStateOf(field.minSlotDuration.toString()) }
    var maxDaysStr by remember { mutableStateOf(field.maxDaysAdvance.toString()) }
    var cancelHoursStr by remember { mutableStateOf(field.cancellationHours.toString()) }
    var status by remember { mutableStateOf(field.status) }

    // NOWE POLA DLA BOISKA
    var description by remember { mutableStateOf(field.description ?: "") }
    var photoUrl by remember { mutableStateOf(field.photoUrl ?: "") }

    var schedule by remember {
        mutableStateOf(field.weeklySchedule ?: DayOfWeekIso.entries.associateWith { DaySchedule(isOpen = true, openTime = "08:00", closeTime = "22:00") })
    }

    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxSize().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("Edycja Boiska", style = MaterialTheme.typography.headlineSmall, color = RacingGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis boiska") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena (PLN/h)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = minSlotStr, onValueChange = { minSlotStr = it }, label = { Text("Czas slotu (min)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text("Link do zdjęcia boiska (URL)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

                Spacer(Modifier.height(8.dp))
                Text("Rodzaj nawierzchni:", fontWeight = FontWeight.Bold)
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (selectedType == type), onClick = { selectedType = type }, colors = RadioButtonDefaults.colors(selectedColor = RacingGreen))
                        Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status Boiska: ", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = status == "ACTIVE",
                        onCheckedChange = { isChecked -> status = if(isChecked) "ACTIVE" else "INACTIVE" },
                        colors = SwitchDefaults.colors(checkedThumbColor = RacingGreen, checkedTrackColor = RacingGreen.copy(alpha = 0.5f))
                    )
                    Text(if(status == "ACTIVE") "Aktywne" else "Nieaktywne")
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Zasady rezerwacji:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = maxDaysStr, onValueChange = { maxDaysStr = it }, label = { Text("Dni w przód") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = cancelHoursStr, onValueChange = { cancelHoursStr = it }, label = { Text("Darmowe odwołanie (h)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Godziny otwarcia tygodniowe:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                DayOfWeekIso.entries.forEach { day ->
                    DayScheduleRow(
                        day = day,
                        schedule = schedule[day]!!,
                        onScheduleChange = { updatedSchedule ->
                            schedule = schedule.toMutableMap().apply { this[day] = updatedSchedule }
                        }
                    )
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

                            try {
                                val request = AddFieldRequest(
                                    facilityId = 0, name = name, fieldType = selectedType, price = price,
                                    minSlotDuration = minDur, maxDaysAdvance = maxD, cancellationHours = cancelH,
                                    status = status, weeklySchedule = schedule,
                                    description = description.ifBlank { null }, photoUrl = photoUrl.ifBlank { null }
                                )
                                if (api.updateField(userId, field.id, request)) { onSuccess(); onDismiss() } else errorMessage = "Błąd edycji"
                            } catch (e: Exception) { errorMessage = e.message }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)) { Text("Zapisz Zmiany") }
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