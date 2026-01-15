package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.AdminStatsDto
import org.pracainzynierska.sportbooking.FacilityAdminDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.UserDto
import org.pracainzynierska.sportbooking.theme.ErrorRed
import org.pracainzynierska.sportbooking.theme.RacingGreen

@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit
) {
    val api = remember { SportApi() }
    val scope = rememberCoroutineScope()

    // --- STANY DANYCH ---
    var stats by remember { mutableStateOf<AdminStatsDto?>(null) }
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var facilities by remember { mutableStateOf<List<FacilityAdminDto>>(emptyList()) }
    var triggerRefresh by remember { mutableStateOf(0) }

    // --- STANY ROZWIJANIA BELEK ---
    var isOwnersExpanded by remember { mutableStateOf(true) }
    var isClientsExpanded by remember { mutableStateOf(false) }
    var isFacilitiesExpanded by remember { mutableStateOf(false) }

    // Filtrowanie użytkowników
    val owners = users.filter { it.role == "FIELD_OWNER" || it.role == "OWNER" }
    val clients = users.filter { it.role == "CLIENT" }

    // --- ŁADOWANIE DANYCH ---
    LaunchedEffect(triggerRefresh) {
        try {
            stats = api.getAdminStats()
            users = api.getAllUsers()
            facilities = api.getAllFacilitiesAdmin()
        } catch (e: Exception) {
            println("Błąd admina: ${e.message}")
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

        // 1. HEADER
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.Black).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ADMIN PANEL", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, "Wyjdź", tint = Color.White)
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {

            // 2. DASHBOARD (Statystyki)
            if (stats != null) {
                item {
                    Text("Statystyki Systemu", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                StatsCardLocal("Użytkownicy", stats!!.totalUsers.toString(), Icons.Default.People, Color(0xFF1976D2), Modifier.fillMaxWidth())
                            }
                            Box(Modifier.weight(1f)) {
                                StatsCardLocal("Obiekty", stats!!.totalFacilities.toString(), Icons.Default.Stadium, RacingGreen, Modifier.fillMaxWidth())
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                StatsCardLocal("Rezerwacje", stats!!.totalBookings.toString(), Icons.Default.BookOnline, Color(0xFFFFA000), Modifier.fillMaxWidth())
                            }
                            Box(Modifier.weight(1f)) {
                                StatsCardLocal("Przychód", "${stats!!.totalRevenue.toInt()} zł", Icons.Default.AttachMoney, Color(0xFF388E3C), Modifier.fillMaxWidth())
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // 3. WŁAŚCICIELE
            item {
                SectionHeader("Właściciele Obiektów (${owners.size})", isOwnersExpanded, { isOwnersExpanded = !isOwnersExpanded }, Icons.Default.Business)
            }
            if (isOwnersExpanded) {
                items(owners) { user ->
                    UserAdminCard(user, { scope.launch { if (api.deleteUser(user.id)) triggerRefresh++ } })
                }
                if (owners.isEmpty()) item { Text("Brak właścicieli", Modifier.padding(16.dp), color = Color.Gray) }
            }

            // 4. KLIENCI
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Klienci Indywidualni (${clients.size})", isClientsExpanded, { isClientsExpanded = !isClientsExpanded }, Icons.Default.Person)
            }
            if (isClientsExpanded) {
                items(clients) { user ->
                    UserAdminCard(user, { scope.launch { if (api.deleteUser(user.id)) triggerRefresh++ } })
                }
                if (clients.isEmpty()) item { Text("Brak klientów", Modifier.padding(16.dp), color = Color.Gray) }
            }

            // 5 ZARZĄDZANIE OBIEKTAMI
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Wszystkie Obiekty (${facilities.size})", isFacilitiesExpanded, { isFacilitiesExpanded = !isFacilitiesExpanded }, Icons.Default.Stadium)
            }
            if (isFacilitiesExpanded) {
                items(facilities) { facility ->
                    FacilityAdminCard(facility, onDelete = {
                        scope.launch { if (api.deleteFacilityAdmin(facility.id)) triggerRefresh++ }
                    })
                }
                if (facilities.isEmpty()) item { Text("Brak obiektów", Modifier.padding(16.dp), color = Color.Gray) }
            }
        }
    }
}

// --- KOMPONENTY ---

@Composable
fun SectionHeader(title: String, isExpanded: Boolean, onToggle: () -> Unit, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = RacingGreen)
                Spacer(Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
        }
    }
}

@Composable
fun UserAdminCard(user: UserDto, onDelete: () -> Unit) {
    val (roleName, roleColor, roleBg) = when(user.role) {
        "FIELD_OWNER", "OWNER" -> Triple("Właściciel", Color(0xFF1565C0), Color(0xFFBBDEFB))
        "ADMIN" -> Triple("Administrator", Color(0xFFC62828), Color(0xFFFFCDD2))
        else -> Triple("Użytkownik", Color(0xFF2E7D32), Color(0xFFC8E6C9))
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Surface(color = roleBg, shape = MaterialTheme.shapes.small) {
                        Text(roleName, Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelSmall, color = roleColor, fontWeight = FontWeight.Bold)
                    }
                }
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("ID: ${user.id}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Usuń", tint = ErrorRed) }
        }
    }
}

// KARTA OBIEKTU
@Composable
fun FacilityAdminCard(facility: FacilityAdminDto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(facility.name, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Surface(color = Color(0xFFE0F2F1), shape = MaterialTheme.shapes.small) {
                        Text("ID: ${facility.id}", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelSmall, color = RacingGreen)
                    }
                }
                Text("Właściciel: ${facility.ownerName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Lokalizacja: ${facility.location}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Usuń", tint = ErrorRed) }
        }
    }
}

@Composable
fun StatsCardLocal(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
        }
    }
}