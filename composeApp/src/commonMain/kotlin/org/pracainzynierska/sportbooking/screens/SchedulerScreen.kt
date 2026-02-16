package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.pracainzynierska.sportbooking.BookingDto
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.components.BlikPaymentDialog
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.theme.RacingGreenLight
import org.pracainzynierska.sportbooking.utils.calculateTotalPrice
import org.pracainzynierska.sportbooking.utils.mergeSlotsToRequests
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun SchedulerScreen(
    facility: FacilityDto,
    api: SportApi,
    userId: Int,
    onBack: () -> Unit
) {
    val today = remember { LocalDate(2026, 2, 16) }
    var selectedDate by remember { mutableStateOf(today) }
    var takenSlots by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedSlots by remember { mutableStateOf<Set<Pair<Int, LocalTime>>>(emptySet()) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    // 👇 NOWE: Flaga ładowania
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedDate, refreshTrigger) {
        isLoading = true // Startujemy ładowanie
        takenSlots = emptyList() // Czyścimy widok z poprzednich danych
        selectedSlots = emptySet()

        try {
            takenSlots = api.getTakenSlots(facility.id, selectedDate.toString())
        } catch (e: Exception) {
            println("Błąd: $e")
        } finally {
            isLoading = false // Kończymy ładowanie
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("< Wróć") }
                Text(facility.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(48.dp))
            }

            val daysList = remember(facility.maxDaysAdvance) { (0 until facility.maxDaysAdvance).map { today.plus(DatePeriod(days = it)) } }

            fun getPolishDayAbbr(day: DayOfWeek): String = when(day) {
                DayOfWeek.MONDAY -> "PON."; DayOfWeek.TUESDAY -> "WT."; DayOfWeek.WEDNESDAY -> "ŚR."; DayOfWeek.THURSDAY -> "CZW."; DayOfWeek.FRIDAY -> "PT."; DayOfWeek.SATURDAY -> "SOB."; DayOfWeek.SUNDAY -> "ND."; else -> day.name.take(3)
            }
            fun getPolishMonthAbbr(month: Month): String = when(month) {
                Month.JANUARY -> "Sty"; Month.FEBRUARY -> "Lut"; Month.MARCH -> "Mar"; Month.APRIL -> "Kwi"; Month.MAY -> "Maj"; Month.JUNE -> "Cze"; Month.JULY -> "Lip"; Month.AUGUST -> "Sie"; Month.SEPTEMBER -> "Wrz"; Month.OCTOBER -> "Paź"; Month.NOVEMBER -> "Lis"; Month.DECEMBER -> "Gru"; else -> month.name.take(3)
            }

            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(daysList.size) { index ->
                    val date = daysList[index]
                    val isSelected = (date == selectedDate)
                    val contentColor = if (isSelected) RacingGreen else Color.Gray
                    val circleColor = if (isSelected) RacingGreen else Color.Transparent
                    val numberColor = if (isSelected) Color.White else Color.Black
                    val borderColor = if (isSelected) RacingGreen else Color.LightGray

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedDate = date }.padding(4.dp)) {
                        Text(getPolishDayAbbr(date.dayOfWeek), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = contentColor)
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = CircleShape, color = circleColor, border = BorderStroke(1.dp, borderColor), modifier = Modifier.size(42.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = numberColor)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(getPolishMonthAbbr(date.month), style = MaterialTheme.typography.labelSmall, color = contentColor)
                    }
                }
            }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                // 👇 NOWE: Spinner zamiast pustej przestrzeni
                if (isLoading) {
                    Box(Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RacingGreen)
                    }
                } else {
                    facility.fields.forEach { field ->
                        Text(field.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                            val scrollState = rememberScrollState()
                            val coroutineScope = rememberCoroutineScope()
                            Row(Modifier.fillMaxWidth().horizontalScroll(scrollState).pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.changes.isNotEmpty() && event.type == PointerEventType.Scroll) {
                                            val change = event.changes.first()
                                            coroutineScope.launch { scrollState.scrollBy(change.scrollDelta.y * 50f) }
                                            change.consume()
                                        }
                                    }
                                }
                            }) {
                                val startHour = facility.openingTime.split(":").first().toInt()
                                val endHour = facility.closingTime.split(":").first().toInt()
                                val step = field.minSlotDuration
                                for (currentMinutes in (startHour * 60) until (endHour * 60) step step) {
                                    val h = currentMinutes / 60
                                    val m = currentMinutes % 60
                                    val slotStart = LocalTime(h, m)
                                    val endTotal = currentMinutes + step
                                    val slotEnd = if (endTotal >= 24 * 60) LocalTime(23, 59) else LocalTime(endTotal / 60, endTotal % 60)
                                    val startStr = "${if(h<10)"0$h" else h}:${if(m<10)"0$m" else m}"

                                    val isTaken = takenSlots.any { booking ->
                                        if (booking.fieldId != field.id) return@any false
                                        val s = booking.startDate.replace("Z", ""); val e = booking.endDate.replace("Z", "")
                                        val bs = LocalDateTime.parse(s).time; val be = LocalDateTime.parse(e).time
                                        slotStart < be && slotEnd > bs
                                    }
                                    val isSelected = selectedSlots.contains(field.id to slotStart)
                                    val slotColor = if (isTaken) Color.LightGray else if (isSelected) Color(0xFFFFD700) else RacingGreenLight
                                    val textColor = if (isSelected) Color.Black else Color.White

                                    Column(Modifier.padding(end = 4.dp).width(85.dp)) {
                                        Text(startStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Spacer(Modifier.height(4.dp))
                                        Button(
                                            onClick = { if (!isTaken) { val key = field.id to slotStart; selectedSlots = if (isSelected) selectedSlots - key else selectedSlots + key } },
                                            colors = ButtonDefaults.buttonColors(containerColor = slotColor),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.fillMaxWidth().height(50.dp),
                                            enabled = !isTaken
                                        ) { Text(
                                            text = "${if (field.price % 1 == 0.0) field.price.toInt() else field.price} zł",

                                            style = MaterialTheme.typography.labelSmall,

                                            color = if (isSelected) Color.Black else Color.White
                                        ) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedSlots.isNotEmpty()) {
            Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), shadowElevation = 16.dp, color = Color.White) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val totalPrice = calculateTotalPrice(selectedSlots, facility.fields)
                    Column {
                        Text("Wybrano: ${selectedSlots.size}", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalPrice PLN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RacingGreen)
                    }
                    Button(onClick = { showPaymentDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen), shape = RoundedCornerShape(8.dp)) { Text("ZAREZERWUJ") }
                }
            }
        }

        if (showPaymentDialog) {
            val priceToPay = calculateTotalPrice(selectedSlots, facility.fields)
            BlikPaymentDialog(totalPrice = priceToPay, onDismiss = { showPaymentDialog = false }, onConfirmPayment = {
                scope.launch {
                    val requests = mergeSlotsToRequests(selectedSlots, facility.fields, selectedDate.toString())
                    var successCount = 0
                    var errorMsg: String? = null
                    requests.forEach { req ->
                        try { if (api.createBooking(userId, req)) successCount++ } catch (e: Exception) { errorMsg = e.message }
                    }
                    showPaymentDialog = false
                    if (successCount > 0) {
                        message = "Płatność przyjęta! Zarezerwowano pomyślnie."; selectedSlots = emptySet(); refreshTrigger++
                    } else { message = "Błąd: $errorMsg" }
                }
            })
        }

        message?.let {
            Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp), action = { TextButton(onClick = { message = null }) { Text("OK") } }) { Text(it) }
        }
    }
}