package org.pracainzynierska.sportbooking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card // Zmieniono na material3
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

import sportsfieldbooking.composeapp.generated.resources.Res
import sportsfieldbooking.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        // 1. Stan: Tutaj przechowujemy listę boisk. Na początku jest pusta.
        var facilities by remember { mutableStateOf<List<FacilityDto>>(emptyList()) }

        // 2. Klient API (ten z Shared)
        val api = remember { SportApi() }

        // 3. Efekt uboczny (LaunchedEffect): Wykonaj to TYLKO RAZ przy starcie
        LaunchedEffect(Unit) {
            try {
                // Pobierz dane z backendu
                facilities = api.getFacilities()
            } catch (e: Exception) {
                println("Błąd pobierania: ${e.message}")
            }
        }

        // 4. Widok
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // ZMIANA: Zamiast 'h4' używamy 'headlineMedium' (standard Material 3)
            Text(
                text = "Dostępne Obiekty Sportowe",
                style = MaterialTheme.typography.headlineMedium
            )

            // 5. Wyświetlanie listy
            facilities.forEach { facility ->
                // Tutaj wywołujemy funkcję zdefiniowaną na dole pliku
                FacilityCard(facility)
            }
        }
    }
}

@Composable
fun FacilityCard(facility: FacilityDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        // W Material 3 cienie (elevation) ustawia się trochę inaczej:
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // ZMIANA: Zamiast 'h6' używamy 'titleLarge'
            Text(
                text = facility.name,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Lokalizacja: ${facility.location}",
                style = MaterialTheme.typography.bodyMedium
            )

            facility.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}