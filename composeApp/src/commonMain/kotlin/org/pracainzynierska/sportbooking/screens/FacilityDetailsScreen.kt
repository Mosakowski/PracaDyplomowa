package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.theme.RacingGreenLight

@Composable
fun FacilityDetailsScreen(
    facility: FacilityDto,
    currentUser: AuthResponse?,
    onNavigateToScheduler: () -> Unit,
    onBack: () -> Unit
    // USUNIĘTO: onNavigateToManager - klient tego nie potrzebuje!
) {
    val groupedFields = remember(facility.fields) { facility.fields.groupBy { it.type } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // --- 1. NAGŁÓWEK ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(RacingGreen, RacingGreenLight))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Sports, null, modifier = Modifier.size(100.dp).alpha(0.3f), tint = Color.White)
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                }

                Column(Modifier.padding(16.dp)) {
                    Text(facility.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(facility.location, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("Otwarte: ${facility.openingTime} - ${facility.closingTime}", style = MaterialTheme.typography.labelMedium)
                    facility.description?.let { if (it.isNotEmpty()) Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp)) }
                }
            }
        }

        // --- 2. OFERTA SPORTOWA (Tylko widok pogrupowany) ---
        Text("Oferta sportowa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        if (facility.fields.isEmpty()) {
            Text("Ten obiekt nie ma jeszcze dodanych boisk.", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
        } else {
            groupedFields.forEach { (type, fieldsList) ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when(type) {
                                "PILKA_NOZNA" -> Icons.Default.SportsSoccer
                                "KOSZYKOWKA" -> Icons.Default.SportsBasketball
                                "KORT_TENISOWY" -> Icons.Default.SportsTennis
                                else -> Icons.Default.Sports
                            }
                            Icon(icon, null, tint = RacingGreen)
                            Spacer(Modifier.width(16.dp))
                            Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)
                        }
                        Surface(color = RacingGreenLight, shape = RoundedCornerShape(16.dp)) {
                            Text("${fieldsList.size} szt.", color = Color.White, modifier = Modifier.padding(12.dp, 4.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 3. PRZYCISK REZERWACJI ---
        if (facility.fields.isNotEmpty()) {
            Button(
                onClick = onNavigateToScheduler,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RacingGreen)
            ) { Text("Sprawdź dostępność i zarezerwuj") }
        }
        Spacer(Modifier.height(24.dp))
    }
}