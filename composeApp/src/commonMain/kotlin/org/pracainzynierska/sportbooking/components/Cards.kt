package org.pracainzynierska.sportbooking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.pracainzynierska.sportbooking.BookingDto
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.OwnerBookingDto
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

// --- 1. KARTA OBIEKTU (Dla listy głównej) ---
@Composable
fun FacilityCard(
    facility: FacilityDto,
    onNavigate: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        onClick = onNavigate
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Place, null, tint = RacingGreen, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(facility.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(facility.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNavigate,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)
            ) {
                Text("Zobacz szczegóły")
            }
        }
    }
}

// --- 2. KARTA REZERWACJI (Dla Moje Rezerwacje) ---
@Composable
fun BookingCard(booking: BookingDto, onCancel: () -> Unit) {
    val isCancelled = booking.status == "CANCELLED"

    // Parsowanie daty
    val (day, month, timeRange, fullDate) = try {
        // Fix na format daty (spacja -> T)
        val cleanStart = booking.startDate.replace(" ", "T")
        val cleanEnd = booking.endDate.replace(" ", "T")

        val start = LocalDateTime.parse(cleanStart)
        val end = LocalDateTime.parse(cleanEnd)

        fun timeStr(t: LocalDateTime) = "${if(t.hour<10)"0" else ""}${t.hour}:${if(t.minute<10)"0" else ""}${t.minute}"

        val plMonths = listOf("STY", "LUT", "MAR", "KWI", "MAJ", "CZE", "LIP", "SIE", "WRZ", "PAŹ", "LIS", "GRU")
        val m = plMonths[start.month.number - 1]

        // 👇 TUTAJ BYŁ BŁĄD: .day zamienione na .dayOfMonth
        val d = start.dayOfMonth.toString()
        val range = "${timeStr(start)} - ${timeStr(end)}"
        // 👇 TUTAJ TEŻ: .day zamienione na .dayOfMonth
        val full = "${start.dayOfMonth}.${start.month.number}.${start.year}"

        Quadruple(d, m, range, full)
    } catch (e: Exception) {
        println("Błąd parsowania: ${e.message}")
        Quadruple("?", "???", "??:??", "Błąd")
    }

    val statusColor = if (isCancelled) Color.LightGray else RacingGreen

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
    ) {
        Row(Modifier.fillMaxSize()) {
            // Lewy pasek z datą
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(statusColor)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isCancelled) {
                    Icon(Icons.Default.Cancel, null, tint = Color.White)
                    Text("ANUL.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Text(month, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                    Text(day, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(fullDate, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }

            // Treść główna
            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(booking.facilityName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if(isCancelled) Color.Gray else Color.Black)
                        Text(booking.fieldName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    Surface(color = if (isCancelled) Color.LightGray.copy(alpha=0.2f) else RacingGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text("${booking.price} zł", modifier = Modifier.padding(8.dp, 4.dp), fontWeight = FontWeight.Bold, color = if(isCancelled) Color.Gray else RacingGreen)
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = RacingGreen)
                    Spacer(Modifier.width(4.dp))
                    Text(timeRange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(booking.facilityLocation, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                }
            }

            // Przycisk anulowania
            if (!isCancelled) {
                Column(Modifier.fillMaxHeight().padding(end = 4.dp), verticalArrangement = Arrangement.Center) {
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Cancel, "Odwołaj", tint = ErrorRed.copy(alpha = 0.6f)) }
                }
            }
        }
    }
}

// --- 3. KARTA OSTATNIEJ REZERWACJI (Panel Właściciela) ---
// --- 3. KARTA OSTATNIEJ REZERWACJI (Panel Właściciela) ---
@Composable
fun RecentBookingCard(booking: OwnerBookingDto) {
    val dateString = remember(booking.rawDate) {
        try {
            // Backend wysyła "2026-01-13", dodajemy godzinę żeby parser zadziałał
            val date = LocalDateTime.parse("${booking.rawDate}T00:00")

            val plMonths = listOf("STY", "LUT", "MAR", "KWI", "MAJ", "CZE", "LIP", "SIE", "WRZ", "PAŹ", "LIS", "GRU")
            val monthStr = plMonths[date.month.number - 1]

            // 👇 ZMIANA: .day -> .dayOfMonth
            "${date.dayOfMonth} $monthStr"
        } catch (e: Exception) {
            booking.rawDate // w razie błędu pokaż surową datę
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                if (booking.status == "TECHNICAL") Icon(Icons.Default.Build, null, tint = Color.Gray)
                else Icon(Icons.Default.Person, null, tint = RacingGreen)
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(if (booking.status == "TECHNICAL") "Blokada techniczna" else booking.clientName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(booking.fieldName, style = MaterialTheme.typography.bodySmall, color = Color.Black)

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateString, style = MaterialTheme.typography.labelMedium, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = RacingGreen)
                    Spacer(Modifier.width(4.dp))
                    Text("${booking.startDate} - ${booking.endDate}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Text("Utworzono: ${booking.bookingTime}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            if (booking.price > 0) Text("${booking.price} zł", fontWeight = FontWeight.Bold, color = RacingGreen)
            else Text("BLK", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// --- 4. KARTA STATYSTYK (Panel Właściciela) ---
@Composable
fun StatsCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

// Helper
data class Quadruple<A,B,C,D>(val first: A, val second: B, val third: C, val fourth: D)