package org.pracainzynierska.sportbooking

// --- IMPORTY UI (Interfejs) ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import podstawowych ikon (Home, Person, etc.)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

// --- IMPORTY LOGIKI (Backend/Shared) ---
import org.pracainzynierska.sportbooking.AddFacilityRequest
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.RegisterRequest
import org.pracainzynierska.sportbooking.LoginRequest
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.CreateBookingRequest
import org.pracainzynierska.sportbooking.BookingDto
import org.pracainzynierska.sportbooking.AddFieldRequest

// --- SEKCJA 1: KONFIGURACJA WYGLĄDU (MOTYW) ---
// Tutaj możesz łatwo zmieniać kolory w przyszłości.

// Kolor przewodni: Racing Green (Głęboka, angielska zieleń)
val RacingGreen = Color(0xFF004225)
// Kolor akcentu: Jaśniejsza zieleń (dla elementów drugoplanowych)
val RacingGreenLight = Color(0xFF2E6B48)
// Kolor tła: Bardzo jasny szary (żeby nie raziło czystą bielą)
val AppBackground = Color(0xFFF9F9F9)
// Kolor błedu: Klasyczna czerwień
val ErrorRed = Color(0xFFB00020)

// Definicja jasnego motywu
private val AppColorScheme = lightColorScheme(
    primary = RacingGreen,
    onPrimary = Color.White, // Tekst na zielonym tle
    secondary = RacingGreenLight,
    onSecondary = Color.White,
    background = AppBackground,
    surface = Color.White, // Tło kart i dialogów
    error = ErrorRed
)

enum class Screen {
    LOGIN, LIST, MY_BOOKINGS
}

// --- GŁÓWNA APLIKACJA ---
@Composable
@Preview
fun App() {
    // Nakładamy nasz motyw na całą aplikację
    MaterialTheme(colorScheme = AppColorScheme) {
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var currentUser by remember { mutableStateOf<AuthResponse?>(null) }
        val api = remember { SportApi() }

        // Główny kontener (Tło całej aplikacji)
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {

                // --- GÓRNY PASEK (HEADER) ---
                // Wygląda teraz jak belka aplikacji
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp // Cień pod paskiem
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lewa strona: Logo/Nazwa
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = RacingGreen)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SportBooking",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RacingGreen
                                )
                                if (currentUser != null) {
                                    Text(
                                        text = "Witaj, ${currentUser?.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Prawa strona: Nawigacja (Ikony zamiast tekstu)
                        if (currentUser != null) {
                            Row {
                                // Przycisk: Obiekty
                                IconButton(onClick = { currentScreen = Screen.LIST }) {
                                    Icon(Icons.Default.Home, contentDescription = "Obiekty", tint = RacingGreen)
                                }
                                // Przycisk: Moje Rezerwacje
                                IconButton(onClick = { currentScreen = Screen.MY_BOOKINGS }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Rezerwacje", tint = RacingGreen)
                                }
                                // Przycisk: Wyloguj
                                IconButton(onClick = {
                                    currentUser = null
                                    currentScreen = Screen.LOGIN
                                }) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = "Wyloguj", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }

                // --- ZAWARTOŚĆ EKRANU ---
                Box(Modifier.padding(16.dp)) {
                    when (currentScreen) {
                        Screen.LOGIN -> LoginScreen(
                            onLoginSuccess = { user ->
                                currentUser = user
                                currentScreen = Screen.LIST
                            },
                            api = api
                        )
                        Screen.LIST -> FacilitiesScreen(api, currentUser)
                        Screen.MY_BOOKINGS -> {
                            if (currentUser != null) {
                                MyBookingsScreen(api, currentUser!!.userId)
                            } else {
                                currentScreen = Screen.LOGIN
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- EKRAN 1: LOGOWANIE ---
@Composable
fun LoginScreen(onLoginSuccess: (AuthResponse) -> Unit, api: SportApi) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRegisterMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ikona kłódki nad formularzem
        Icon(
            imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = RacingGreen
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isRegisterMode) "Dołącz do gry" else "Witaj ponownie",
            style = MaterialTheme.typography.headlineMedium,
            color = RacingGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(32.dp))

        // Pola tekstowe
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adres email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            leadingIcon = { Icon(Icons.Default.Key, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        // Główny przycisk
        Button(
            onClick = {
                scope.launch {
                    try {
                        errorMessage = null
                        if (isRegisterMode) {
                            val success = api.register(RegisterRequest(email, password, "Użytkownik"))
                            if (success) {
                                errorMessage = "Konto gotowe! Zaloguj się teraz."
                                isRegisterMode = false
                            }
                        } else {
                            val user = api.login(LoginRequest(email, password))
                            onLoginSuccess(user)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Błąd: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp) // Zaokrąglone rogi
        ) {
            Text(if (isRegisterMode) "Zarejestruj się" else "Zaloguj się", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
        }

        // Komunikaty błędów
        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        // Przełącznik trybu
        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(if (isRegisterMode) "Masz już konto? Zaloguj się" else "Nie masz konta? Załóż je tutaj")
        }
    }
}

// --- EKRAN 2: LISTA OBIEKTÓW ---
@Composable
fun FacilitiesScreen(api: SportApi, currentUser: AuthResponse?) {
    var refreshTrigger by remember { mutableStateOf(0) }
    var facilities by remember { mutableStateOf<List<FacilityDto>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        try { facilities = api.getFacilities() } catch (e: Exception) { println(e) }
    }

    Box(Modifier.fillMaxSize()) {
        Column {
            // Nagłówek sekcji
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dostępne obiekty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                if (currentUser != null && currentUser?.role == "FIELD_OWNER") {
                    FilledTonalButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Dodaj Obiekt")
                    }
                }
            }

            if (facilities.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Brak obiektów w bazie. Pusto tu...", color = Color.Gray)
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp) // Odstępy między kartami
            ) {
                items(facilities.size) { index ->
                    FacilityCard(
                        facility = facilities[index],
                        api = api,
                        currentUser = currentUser,
                        onRefresh = { refreshTrigger++ }
                    )
                }
            }
        }

        if (showAddDialog && currentUser != null) {
            AddFacilityDialog(
                userId = currentUser.userId,
                api = api,
                onDismiss = { showAddDialog = false },
                onSuccess = {
                    showAddDialog = false
                    refreshTrigger++
                }
            )
        }
    }
}

// --- KARTA OBIEKTU (WYGLĄD) ---
@Composable
fun FacilityCard(
    facility: FacilityDto,
    api: SportApi,
    currentUser: AuthResponse?,
    onRefresh: () -> Unit
) {
    var showBookingDialog by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Nagłówek karty z ikoną
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = RacingGreen,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(facility.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(facility.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            if (facility.fields.isNotEmpty()) {
                Text(
                    "Dostępne boiska: ${facility.fields.joinToString { it.name }}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("Ten obiekt nie ma jeszcze boisk.", style = MaterialTheme.typography.bodySmall, color = ErrorRed)
            }

            Spacer(Modifier.height(16.dp))

            // Przyciski akcji
            if (currentUser != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showBookingDialog = true }) {
                        Text("Rezerwuj termin")
                    }

                    if (currentUser.role == "FIELD_OWNER") {
                        OutlinedButton(onClick = { showAddFieldDialog = true }) {
                            Text("+ Boisko")
                        }
                    }
                }
            }
        }
    }

    if (showBookingDialog && currentUser != null) {
        ReservationDialog(facility, currentUser.userId, api) { showBookingDialog = false }
    }

    if (showAddFieldDialog && currentUser != null) {
        AddFieldDialog(
            facilityId = facility.id,
            userId = currentUser.userId,
            api = api,
            onDismiss = { showAddFieldDialog = false },
            onSuccess = {
                showAddFieldDialog = false
                onRefresh()
            }
        )
    }
}

// --- EKRAN 3: MOJE REZERWACJE ---
@Composable
fun MyBookingsScreen(api: SportApi, userId: Int) {
    var bookings by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            try { bookings = api.getMyBookings(userId) } catch (e: Exception) { println(e) }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column {
        Text("Twoja historia", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp), // 👈 Rozmiar przeniesiony do Modifiera
                        tint = Color.Gray
                    )
                    Text("Brak rezerwacji", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(bookings.size) { index ->
                    BookingCard(
                        booking = bookings[index],
                        onCancel = {
                            scope.launch {
                                val success = api.cancelBooking(userId, bookings[index].id)
                                if (success) refresh()
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- KARTA REZERWACJI ---
@Composable
fun BookingCard(booking: BookingDto, onCancel: () -> Unit) {
    val isCancelled = booking.status == "CANCELLED"
    val isConfirmed = booking.status == "CONFIRMED"

    // Ustawiamy kolor karty zależnie od statusu
    val cardColor = if (isCancelled) Color(0xFFEEEEEE) else Color.White
    val textColor = if (isCancelled) Color.Gray else Color.Black

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if(isCancelled) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sports, null, tint = if(isCancelled) Color.Gray else RacingGreen)
                    Spacer(Modifier.width(8.dp))
                    Text(booking.fieldName, style = MaterialTheme.typography.titleMedium, color = textColor, fontWeight = FontWeight.Bold)
                }

                // Etykieta statusu
                Surface(
                    color = when {
                        isConfirmed -> Color(0xFFE8F5E9) // Jasny zielony
                        isCancelled -> Color(0xFFFFEBEE) // Jasny czerwony
                        else -> Color.LightGray
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if(isCancelled) "ANULOWANO" else if(isConfirmed) "POTWIERDZONO" else booking.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isConfirmed -> Color(0xFF2E7D32)
                            isCancelled -> ErrorRed
                            else -> Color.Black
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            // Sekcja danych
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Początek", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(booking.startDate, style = MaterialTheme.typography.bodyMedium, color = textColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Koniec", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(booking.endDate, style = MaterialTheme.typography.bodyMedium, color = textColor)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stopka: Cena i Przycisk
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${booking.price} PLN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if(isCancelled) Color.Gray else RacingGreen
                )

                if (!isCancelled) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("Odwołaj")
                    }
                }
            }
        }
    }
}

// --- DIALOGI (Standardowe, ale z motywem) ---

@OptIn(ExperimentalTime::class)
@Composable
fun ReservationDialog(facility: FacilityDto, userId: Int, api: SportApi, onDismiss: () -> Unit) {
    var dateText by remember { mutableStateOf("2024-07-01") }
    var timeStart by remember { mutableStateOf("14:00") }
    var timeEnd by remember { mutableStateOf("15:00") }
    var selectedFieldId by remember { mutableStateOf(facility.fields.firstOrNull()?.id) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rezerwacja: ${facility.name}") },
        text = {
            Column {
                Text("Wybierz boisko:", style = MaterialTheme.typography.labelLarge, color = RacingGreen)
                if (facility.fields.isEmpty()) {
                    Text("Brak boisk!", color = ErrorRed)
                } else {
                    facility.fields.forEach { field ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (selectedFieldId == field.id),
                                onClick = { selectedFieldId = field.id },
                                colors = RadioButtonDefaults.colors(selectedColor = RacingGreen)
                            )
                            Text("${field.name} (${field.type})")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Data (RRRR-MM-DD)") })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = timeStart, onValueChange = { timeStart = it }, label = { Text("Od") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = timeEnd, onValueChange = { timeEnd = it }, label = { Text("Do") }, modifier = Modifier.weight(1f))
                }
                message?.let { Text(it, color = ErrorRed, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedFieldId != null,
                onClick = {
                    scope.launch {
                        try {
                            // Tu jest Twoja logika
                            val request = CreateBookingRequest(selectedFieldId!!, "${dateText}T${timeStart}", "${dateText}T${timeEnd}")
                            val success = api.createBooking(userId, request)
                            if (success) {
                                message = "Sukces!"
                                kotlinx.coroutines.delay(1000)
                                onDismiss()
                            }
                        } catch (e: Exception) {
                            message = "Błąd: ${e.message}"
                        }
                    }
                }
            ) { Text("Potwierdź rezerwację") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Wróć") } }
    )
}

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
                        val success = api.addFacility(userId, AddFacilityRequest(name, location, description))
                        if (success) { onSuccess(); onDismiss() } else errorMessage = "Błąd"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun AddFieldDialog(facilityId: Int, userId: Int, api: SportApi, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PILKA_NOZNA") }
    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowe Boisko") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa (np. Kort A)") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena (PLN/h)") })
                Spacer(Modifier.height(16.dp))
                Text("Rodzaj nawierzchni:", style = MaterialTheme.typography.labelLarge)
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (selectedType == type), onClick = { selectedType = type })
                        Text(type.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
                    }
                }
                errorMessage?.let { Text(it, color = ErrorRed) }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        val price = priceStr.toDoubleOrNull() ?: return@launch
                        val success = api.addField(userId, AddFieldRequest(facilityId, name, selectedType, price))
                        if (success) { onSuccess(); onDismiss() } else errorMessage = "Błąd zapisu"
                    } catch (e: Exception) { errorMessage = e.message }
                }
            }) { Text("Dodaj boisko") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}