package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.FacilityStatsDto
import org.pracainzynierska.sportbooking.OwnerBookingDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.components.RecentBookingCard
import org.pracainzynierska.sportbooking.components.StatsCard
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.utils.mergeSlotsToRequests

@Composable
fun FacilityManagerScreen(
    facility: FacilityDto,
    api: SportApi,
    currentUser: AuthResponse,
    onBack: () -> Unit
) {
    var stats by remember { mutableStateOf<FacilityStatsDto?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate(2026, 1, 25)) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // 👇 NOWE: Flaga ładowania
    var isLoading by remember { mutableStateOf(false) }

    var recentBookings by remember { mutableStateOf<List<OwnerBookingDto>>(emptyList()) }
    var bookings by remember { mutableStateOf<List<OwnerBookingDto>>(emptyList()) }
    var selectedSlotsToBlock by remember { mutableStateOf<Set<Pair<Int, LocalTime>>>(emptySet()) }
    var bookingToManage by remember { mutableStateOf<OwnerBookingDto?>(null) }
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshTrigger, selectedDate) {
        // 👇 RESETUJEMY DANE I WŁĄCZAMY ŁADOWANIE
        isLoading = true
        bookings = emptyList() // Czyścimy stare, żeby nie myliły
        selectedSlotsToBlock = emptySet()

        try {
            if (stats == null || refreshTrigger > 0) stats = api.getFacilityStats(currentUser.userId, facility.id)
            bookings = api.getOwnerBookings(currentUser.userId, facility.id, selectedDate.toString())
            recentBookings = api.getRecentBookings(currentUser.userId, facility.id)
        } catch (e: Exception) {
            println(e)
        } finally {
            // 👇 WYŁĄCZAMY ŁADOWANIE (niezależnie czy sukces czy błąd)
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("Panel Zarządzania", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (stats != null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatsCard("Przychód (Msc)", "${stats!!.monthlyRevenue} PLN", Icons.Default.AttachMoney, RacingGreen, Modifier.weight(1f))
                    StatsCard("Rezerwacje", "${stats!!.totalBookings}", Icons.Default.DateRange, Color(0xFF1976D2), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
                        Spacer(Modifier.width(8.dp))
                        Text("Top Boisko: ${stats!!.mostPopularField}", fontWeight = FontWeight.Bold)
                    }
                }
            } else { Text("Ładowanie statystyk...", Modifier.padding(16.dp), color = Color.Gray) }

            Spacer(Modifier.height(16.dp))
            Divider()

            val daysList = remember(facility.maxDaysAdvance) { (0 until facility.maxDaysAdvance).map { LocalDate(2026, 1, 25).plus(DatePeriod(days = it)) } }
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
                            Box(contentAlignment = Alignment.Center) { Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = numberColor) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(getPolishMonthAbbr(date.month), style = MaterialTheme.typography.labelSmall, color = contentColor)
                    }
                }
            }

            Text("Grafik wizualny", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            // 👇 NOWE: Obsługa Spinnera. Jeśli ładuje - kręcioł. Jeśli nie - lista.
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RacingGreen)
                }
            } else {
                facility.fields.forEach { field ->
                    Text(field.name, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 16.dp))
                    Box(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        val scrollState = rememberScrollState()
                        Row(Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(horizontal = 16.dp)) {
                            val startHour = facility.openingTime.split(":").first().toInt()
                            val endHour = facility.closingTime.split(":").first().toInt()
                            val step = field.minSlotDuration
                            for (currentMinutes in (startHour * 60) until (endHour * 60) step step) {
                                val h = currentMinutes / 60
                                val m = currentMinutes % 60
                                val slotStart = LocalTime(h, m)
                                val existingBooking = bookings.find { booking ->
                                    if (booking.fieldId != field.id) return@find false
                                    val bs = try { LocalTime.parse(booking.startDate) } catch(e:Exception) { LocalTime(0,0) }
                                    val be = try { LocalTime.parse(booking.endDate) } catch(e:Exception) { LocalTime(0,0) }
                                    slotStart >= bs && slotStart < be
                                }
                                val isTechnical = existingBooking?.status == "TECHNICAL"
                                val isClient = existingBooking != null && !isTechnical
                                val isSelectedToBlock = selectedSlotsToBlock.contains(field.id to slotStart)
                                val bgColor = when { isTechnical -> Color.DarkGray; isClient -> Color(0xFF1976D2); isSelectedToBlock -> ErrorRed; else -> Color.White }

                                Column(Modifier.padding(end = 4.dp).width(85.dp)) {
                                    Text("${if(h<10)"0$h" else h}:${if(m<10)"0$m" else m}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = { if (existingBooking != null) bookingToManage = existingBooking else { val key = field.id to slotStart; selectedSlotsToBlock = if (isSelectedToBlock) selectedSlotsToBlock - key else selectedSlotsToBlock + key } },
                                        colors = ButtonDefaults.buttonColors(containerColor = bgColor), shape = RoundedCornerShape(4.dp), modifier = Modifier.height(50.dp).fillMaxWidth(),
                                        border = if (existingBooking == null && !isSelectedToBlock) BorderStroke(1.dp, RacingGreen) else null
                                    ) {
                                        if (isTechnical) Icon(Icons.Default.Build, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        else if (isClient) Text(existingBooking!!.clientName.take(6)+"..", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        else if (isSelectedToBlock) Icon(Icons.Default.Lock, null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Text("Ostatnia aktywność", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            if (recentBookings.isEmpty()) { Text("Brak nowej aktywności.", Modifier.padding(16.dp), color = Color.Gray) }
            else { recentBookings.forEach { booking -> RecentBookingCard(booking); Spacer(Modifier.height(8.dp)) } }
            Spacer(Modifier.height(80.dp))
        }

        if (selectedSlotsToBlock.isNotEmpty()) {
            Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), color = Color.White, shadowElevation = 16.dp) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Wybrano slotów: ${selectedSlotsToBlock.size}")
                    Button(onClick = { scope.launch { val requests = mergeSlotsToRequests(selectedSlotsToBlock, facility.fields, selectedDate.toString()); requests.forEach { req -> api.blockSlot(currentUser.userId, req) }; message = "Terminy zablokowane!"; selectedSlotsToBlock = emptySet(); refreshTrigger++ } }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("ZABLOKUJ TERMINY") }
                }
            }
        }
    }

    if (bookingToManage != null) {
        val isTech = bookingToManage!!.status == "TECHNICAL"
        AlertDialog(
            onDismissRequest = { bookingToManage = null },
            title = { Text(if (isTech) "Przerwa techniczna" else "Rezerwacja Klienta") },
            text = { Column { if (!isTech) { Text("Klient: ${bookingToManage!!.clientName}", fontWeight = FontWeight.Bold); Text("Email: ${bookingToManage!!.clientEmail}"); Text("Cena: ${bookingToManage!!.price} PLN") } else { Text("Blokada serwisowa.") }
                Spacer(Modifier.height(8.dp)); Text("Godziny: ${bookingToManage!!.startDate} - ${bookingToManage!!.endDate}") } },
            confirmButton = { Button(onClick = { scope.launch { api.cancelByOwner(currentUser.userId, bookingToManage!!.id); refreshTrigger++; bookingToManage = null } }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text(if (isTech) "Odblokuj" else "Anuluj Rezerwację") } },
            dismissButton = { TextButton(onClick = { bookingToManage = null }) { Text("Zamknij") } }
        )
    }
}