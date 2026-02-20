package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun SchedulerScreen(
    facility: FacilityDto,
    api: SportApi,
    userId: Int,
    onBack: () -> Unit
) {
    val today = remember { LocalDate(2026, 2, 16) }
    var selectedDate by remember { mutableStateOf(today) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // Rezerwacje
    var takenSlots by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    var selectedSlots by remember { mutableStateOf<Set<Pair<Int, LocalTime>>>(emptySet()) }

    // Płatności i komunikaty
    var showPaymentDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Zakładki (Które boisko aktualnie oglądamy)
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(selectedDate, refreshTrigger) {
        isLoading = true
        takenSlots = emptyList()
        selectedSlots = emptySet()

        try {
            takenSlots = api.getTakenSlots(facility.id, selectedDate.toString())
        } catch (e: Exception) {
            println("Błąd: $e")
        } finally {
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // --- NAGŁÓWEK ---
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("< Wróć") }
                Text(facility.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(48.dp))
            }

            if (facility.fields.isEmpty()) {
                Text("Obiekt nie posiada żadnych boisk.", Modifier.padding(16.dp), color = Color.Gray)
                return@Column
            }

            // --- POBIERAMY OBECNIE WYBRANE BOISKO ---
            val selectedField = facility.fields.getOrNull(selectedTabIndex) ?: facility.fields.first()
            val safeTabIndex = facility.fields.indexOf(selectedField)

            // --- ZAKŁADKI BOISK ---
            ScrollableTabRow(
                selectedTabIndex = safeTabIndex,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = RacingGreen
            ) {
                facility.fields.forEachIndexed { index, field ->
                    Tab(
                        selected = safeTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            // Opcjonalnie: czyścimy wybrane sloty przy zmianie boiska,
                            // żeby użytkownik przypadkiem nie zarezerwował dwóch różnych na raz.
                            selectedSlots = emptySet()
                        },
                        text = { Text(field.name, fontWeight = if(safeTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- WYBÓR DATY (Zależny od wybranego boiska) ---
            val daysList = remember(selectedField.maxDaysAdvance) {
                (0 until selectedField.maxDaysAdvance).map { today.plus(DatePeriod(days = it)) }
            }

            fun getPolishDayAbbr(day: DayOfWeek): String = when(day) { DayOfWeek.MONDAY -> "PON."; DayOfWeek.TUESDAY -> "WT."; DayOfWeek.WEDNESDAY -> "ŚR."; DayOfWeek.THURSDAY -> "CZW."; DayOfWeek.FRIDAY -> "PT."; DayOfWeek.SATURDAY -> "SOB."; DayOfWeek.SUNDAY -> "ND."; else -> day.name.take(3) }
            fun getPolishMonthAbbr(month: Month): String = when(month) { Month.JANUARY -> "Sty"; Month.FEBRUARY -> "Lut"; Month.MARCH -> "Mar"; Month.APRIL -> "Kwi"; Month.MAY -> "Maj"; Month.JUNE -> "Cze"; Month.JULY -> "Lip"; Month.AUGUST -> "Sie"; Month.SEPTEMBER -> "Wrz"; Month.OCTOBER -> "Paź"; Month.NOVEMBER -> "Lis"; Month.DECEMBER -> "Gru"; else -> month.name.take(3) }

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

            // --- MIEJSCE NA NOWĄ OŚ CZASU ---
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RacingGreen)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TU ZBUDUJEMY NOWĄ OŚ CZASU", fontWeight = FontWeight.Bold, color = RacingGreen)
                            Text("Dla boiska: ${selectedField.name}", color = Color.Gray)
                            Text("Dla daty: $selectedDate", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // --- PASEK PODSUMOWANIA I PŁATNOŚCI (Na dole) ---
        if (selectedSlots.isNotEmpty()) {
            Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), shadowElevation = 16.dp, color = Color.White) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val totalPrice = calculateTotalPrice(selectedSlots, facility.fields)
                    Column {
                        Text("Wybrano: ${selectedSlots.size}", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalPrice PLN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RacingGreen)
                    }
                    Button(onClick = { showPaymentDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = RacingGreen), shape = RoundedCornerShape(8.dp)) {
                        Text("ZAREZERWUJ")
                    }
                }
            }
        }

        // --- DIALOG PŁATNOŚCI ---
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
                        message = "Płatność przyjęta! Zarezerwowano pomyślnie."
                        selectedSlots = emptySet()
                        refreshTrigger++
                    } else {
                        message = "Błąd: $errorMsg"
                    }
                }
            })
        }

        message?.let {
            Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp, start = 16.dp, end = 16.dp), action = { TextButton(onClick = { message = null }) { Text("OK") } }) {
                Text(it)
            }
        }
    }
}