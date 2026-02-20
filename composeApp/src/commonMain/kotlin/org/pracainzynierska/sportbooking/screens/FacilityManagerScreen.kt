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

@Composable
fun FacilityManagerScreen(
    facility: FacilityDto,
    api: SportApi,
    currentUser: AuthResponse,
    onBack: () -> Unit
) {
    // Statystyki
    var stats by remember { mutableStateOf<FacilityStatsDto?>(null) }

    // Czas i Ładowanie
    var selectedDate by remember { mutableStateOf(LocalDate(2026, 2, 16)) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // Dane z bazy
    var recentBookings by remember { mutableStateOf<List<OwnerBookingDto>>(emptyList()) }
    var bookings by remember { mutableStateOf<List<OwnerBookingDto>>(emptyList()) }

    // Zakładki Boisk
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger, selectedDate) {
        isLoading = true
        bookings = emptyList()

        try {
            if (stats == null || refreshTrigger > 0) stats = api.getFacilityStats(currentUser.userId, facility.id)
            bookings = api.getOwnerBookings(currentUser.userId, facility.id, selectedDate.toString())
            recentBookings = api.getRecentBookings(currentUser.userId, facility.id)
        } catch (e: Exception) {
            println(e)
        } finally {
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // --- NAGŁÓWEK ---
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("Panel Kalendarza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // --- STATYSTYKI ---
            if (stats != null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatsCard("Przychód (Msc)", "${stats!!.monthlyRevenue} PLN", Icons.Default.AttachMoney, RacingGreen, Modifier.weight(1f))
                    StatsCard("Rezerwacje", "${stats!!.totalBookings}", Icons.Default.DateRange, Color(0xFF1976D2), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            } else {
                Text("Ładowanie statystyk...", Modifier.padding(16.dp), color = Color.Gray)
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            // --- JEŚLI NIE MA BOISK, KOŃCZYMY RYSOWANIE ---
            if (facility.fields.isEmpty()) {
                Text("Brak boisk w obiekcie. Dodaj je w zakładce 'Obiekty'.", Modifier.padding(16.dp), color = Color.Gray)
                return@Column
            }

            // --- POBIERAMY OBECNIE WYBRANE BOISKO ---
            // Zabezpieczenie na wypadek usunięcia boiska w trakcie bycia na ekranie
            val selectedField = facility.fields.getOrNull(selectedTabIndex) ?: facility.fields.first()
            val safeTabIndex = facility.fields.indexOf(selectedField)

            // --- WYBÓR DATY (Odczytuje maxDaysAdvance dla konkrentego boiska!) ---
            val daysList = remember(selectedField.maxDaysAdvance) {
                (0 until selectedField.maxDaysAdvance).map { LocalDate(2026, 2, 16).plus(DatePeriod(days = it)) }
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
                            Box(contentAlignment = Alignment.Center) { Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = numberColor) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(getPolishMonthAbbr(date.month), style = MaterialTheme.typography.labelSmall, color = contentColor)
                    }
                }
            }

            // --- ZAKŁADKI BOISK (Nowość UX) ---
            ScrollableTabRow(
                selectedTabIndex = safeTabIndex,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = RacingGreen
            ) {
                facility.fields.forEachIndexed { index, field ->
                    Tab(
                        selected = safeTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(field.name, fontWeight = if(safeTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- WIDOK KALENDARZA DLA WYBRANEGO BOISKA ---
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RacingGreen)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TU ZBUDUJEMY NOWĄ OŚ CZASU", fontWeight = FontWeight.Bold, color = RacingGreen)
                        Text("Dla boiska: ${selectedField.name}", color = Color.Gray)
                        Text("Dla daty: $selectedDate", color = Color.Gray)
                    }
                }
            }

            // --- OSTATNIA AKTYWNOŚĆ ---
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            Text("Ostatnia aktywność", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            if (recentBookings.isEmpty()) {
                Text("Brak nowej aktywności.", Modifier.padding(horizontal = 16.dp), color = Color.Gray)
            } else {
                recentBookings.forEach { booking ->
                    RecentBookingCard(booking)
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}