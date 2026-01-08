package org.pracainzynierska.sportbooking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.pracainzynierska.sportbooking.AddFacilityRequest
import kotlinx.datetime.*

// Importy modeli z modułu Shared
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.RegisterRequest
import org.pracainzynierska.sportbooking.LoginRequest
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.CreateBookingRequest
import org.pracainzynierska.sportbooking.BookingDto
import kotlin.time.ExperimentalTime

enum class Screen {
    LOGIN, LIST, MY_BOOKINGS
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
        var currentUser by remember { mutableStateOf<AuthResponse?>(null) }
        val api = remember { SportApi() }

        // Główny kontener
        Column(Modifier.fillMaxSize()) {

            // --- NAGŁÓWEK I NAWIGACJA ---
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SportBooking", style = MaterialTheme.typography.headlineSmall)
                    if (currentUser != null) {
                        Text("Witaj, ${currentUser?.name}!", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Przyciski nawigacji (widoczne tylko po zalogowaniu)
                if (currentUser != null) {
                    Row {
                        TextButton(onClick = { currentScreen = Screen.LIST }) { Text("Obiekty") }
                        TextButton(onClick = { currentScreen = Screen.MY_BOOKINGS }) { Text("Moje Rezerwacje") }
                        TextButton(onClick = {
                            currentUser = null
                            currentScreen = Screen.LOGIN
                        }) { Text("Wyloguj") }
                    }
                }
            }

            // --- ZAWARTOŚĆ EKRANU ---
            // To musi być POZA blokiem Row, ale wewnątrz Column
            when (currentScreen) {
                Screen.LOGIN -> {
                    LoginScreen(
                        onLoginSuccess = { user ->
                            currentUser = user
                            currentScreen = Screen.LIST
                        },
                        api = api
                    )
                }
                Screen.LIST -> {
                    FacilitiesScreen(api, currentUser)
                }
                Screen.MY_BOOKINGS -> {
                    if (currentUser != null) {
                        MyBookingsScreen(api, currentUser!!.userId)
                    } else {
                        // Zabezpieczenie: jak nie ma usera, wracamy do logowania
                        currentScreen = Screen.LOGIN
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
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isRegisterMode) "Załóż konto" else "Zaloguj się", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Hasło") }, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                try {
                    errorMessage = null
                    if (isRegisterMode) {
                        val success = api.register(RegisterRequest(email, password, "Nowy User"))
                        if (success) {
                            errorMessage = "Konto utworzone! Zaloguj się."
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
        }) {
            Text(if (isRegisterMode) "Zarejestruj" else "Zaloguj")
        }

        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp)) }
        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(if (isRegisterMode) "Masz konto? Zaloguj się" else "Nie masz konta? Zarejestruj się")
        }
    }
}

// --- EKRAN 2: LISTA OBIEKTÓW ---
@Composable
fun FacilitiesScreen(api: SportApi, currentUser: AuthResponse?) {
    // Zmieniamy to na pamięć, którą można "wymusić" do odświeżenia (klucz refreshTrigger)
    var refreshTrigger by remember { mutableStateOf(0) }
    var facilities by remember { mutableStateOf<List<FacilityDto>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Pobieramy dane za każdym razem, gdy zmieni się refreshTrigger
    LaunchedEffect(refreshTrigger) {
        try { facilities = api.getFacilities() } catch (e: Exception) { println(e) }
    }

    Box(Modifier.fillMaxSize()) { // Używamy Box, żeby móc pozycjonować elementy (np. FAB)
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dostępne obiekty", style = MaterialTheme.typography.titleLarge)

                // Przycisk dodawania - widoczny dla zalogowanych
                if (currentUser != null && currentUser?.role == "FIELD_OWNER") {
                    Button(onClick = { showAddDialog = true }) {
                        Text("+ Dodaj")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (facilities.isEmpty()) {
                Text("Brak obiektów. Dodaj pierwszy!")
            }

            LazyColumn {
                items(facilities.size) { index ->
                    FacilityCard(
                        facility = facilities[index],
                        api = api,
                        currentUser = currentUser,
                        onRefresh = { refreshTrigger++ } // 👈 PRZEKAZUJEMY ODŚWIEŻANIE DALEJ
                    )
                }
            }
        }

        // Dialog dodawania
        if (showAddDialog && currentUser != null) {
            AddFacilityDialog(
                userId = currentUser.userId,
                api = api,
                onDismiss = { showAddDialog = false },
                onSuccess = {
                    showAddDialog = false
                    refreshTrigger++ // To wymusi ponowne pobranie listy (LaunchedEffect)
                }
            )
        }
    }
}

@Composable
fun FacilityCard(
    facility: FacilityDto,
    api: SportApi,
    currentUser: AuthResponse?,
    onRefresh: () -> Unit // 👈 NOWY ARGUMENT
) {
    var showBookingDialog by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) } // 👈 NOWY STAN

    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(facility.name, style = MaterialTheme.typography.titleMedium)
            Text("Lokalizacja: ${facility.location}")

            if (facility.fields.isNotEmpty()) {
                Text("Dostępne: ${facility.fields.joinToString { it.name }}", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Brak boisk - dodaj jakieś!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            // Panel przycisków
            // Panel przycisków
            if (currentUser != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    // 1. Przycisk Rezerwacji (Dla KAŻDEGO zalogowanego)
                    Button(onClick = { showBookingDialog = true }) {
                        Text("Rezerwuj")
                    }

                    // 2. Przycisk Dodawania Boiska (TYLKO dla Właściciela)
                    // Upewnij się, że ten fragment występuje TYLKO RAZ w kodzie!
                    if (currentUser.role == "FIELD_OWNER") {
                        OutlinedButton(onClick = { showAddFieldDialog = true }) {
                            Text("+ Boisko")
                        }
                    }
                }
            } else {
                Text("Zaloguj się, aby zarezerwować", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    // Dialog Rezerwacji
    if (showBookingDialog && currentUser != null) {
        ReservationDialog(facility, currentUser.userId, api) { showBookingDialog = false }
    }

    // Dialog Dodawania Boiska (NOWY)
    if (showAddFieldDialog && currentUser != null) {
        AddFieldDialog(
            facilityId = facility.id,
            userId = currentUser.userId,
            api = api,
            onDismiss = { showAddFieldDialog = false },
            onSuccess = {
                showAddFieldDialog = false
                onRefresh() // 👈 Odśwież listę obiektów, żeby zobaczyć nowe boisko
            }
        )
    }
}

// --- DIALOG REZERWACJI (Z WYBOREM BOISKA I FIXEM NA DATY) ---
@OptIn(ExperimentalTime::class)
@Composable
fun ReservationDialog(
    facility: FacilityDto,
    userId: Int,
    api: SportApi,
    onDismiss: () -> Unit
) {
    var dateText by remember { mutableStateOf("2024-07-01") }
    var timeStart by remember { mutableStateOf("14:00") }
    var timeEnd by remember { mutableStateOf("15:00") }

    // Domyślnie zaznacz pierwsze boisko
    var selectedFieldId by remember { mutableStateOf(facility.fields.firstOrNull()?.id) }

    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rezerwacja: ${facility.name}") },
        text = {
            Column {
                Text("Wybierz boisko:", style = MaterialTheme.typography.titleSmall)
                if (facility.fields.isEmpty()) {
                    Text("Brak zdefiniowanych boisk!", color = MaterialTheme.colorScheme.error)
                } else {
                    facility.fields.forEach { field ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (selectedFieldId == field.id),
                                onClick = { selectedFieldId = field.id }
                            )
                            Text("${field.name} (${field.type})")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Data (RRRR-MM-DD)") })
                OutlinedTextField(value = timeStart, onValueChange = { timeStart = it }, label = { Text("Start (GG:MM)") })
                OutlinedTextField(value = timeEnd, onValueChange = { timeEnd = it }, label = { Text("Koniec (GG:MM)") })

                message?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedFieldId != null,
                onClick = {
                    scope.launch {
                        try {
                            // CZYSTY TEKST - Żadnych bibliotek dat!
                            // Sklejamy to co wpisałeś w jeden napis.
                            // Format musi pasować do tego, co Backend parsuje (LocalDateTime):
                            // "RRRR-MM-DD" + "T" + "GG:MM"
                            // Przykład: "2024-06-01T14:00"
                            val startString = "${dateText}T${timeStart}"
                            val endString = "${dateText}T${timeEnd}"

                            // Wysyłamy napisy
                            val request = CreateBookingRequest(selectedFieldId!!, startString, endString)

                            val success = api.createBooking(userId, request)
                            if (success) {
                                message = "Zarezerwowano pomyślnie!"
                                kotlinx.coroutines.delay(1000)
                                onDismiss()
                            } else {
                                message = "Błąd: Termin zajęty!"
                            }
                        } catch (e: Exception) {
                            message = "Błąd: ${e.message}"
                            println(e)
                        }
                    }
                }
            ) { Text("Zatwierdź") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

// --- EKRAN 3: MOJE REZERWACJE (NOWOŚĆ) ---
@Composable
fun MyBookingsScreen(api: SportApi, userId: Int) {
    var bookings by remember { mutableStateOf<List<BookingDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try { bookings = api.getMyBookings(userId) } catch (e: Exception) { println(e) }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Twoje Rezerwacje", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Text("Nie masz jeszcze żadnych rezerwacji.")
        } else {
            LazyColumn {
                items(bookings.size) { index ->
                    BookingCard(bookings[index])
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: BookingDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(booking.fieldName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Status: ${booking.status}", style = MaterialTheme.typography.bodySmall)
            Text("Start (Ts): ${booking.startTimestamp}")
            Text("Cena: ${booking.price} PLN", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun AddFacilityDialog(
    userId: Int,
    api: SportApi,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit // Żeby odświeżyć listę po dodaniu
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj nowy obiekt") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa obiektu") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokalizacja") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis (opcjonalnie)") })

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        val request = AddFacilityRequest(name, location, description)
                        val success = api.addFacility(userId, request)
                        if (success) {
                            onSuccess() // Odśwież listę
                            onDismiss() // Zamknij okno
                        } else {
                            errorMessage = "Błąd podczas dodawania."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Błąd: ${e.message}"
                    }
                }
            }) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@Composable
fun AddFieldDialog(
    facilityId: Int,
    userId: Int,
    api: SportApi,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    // Domyślny typ
    var selectedType by remember { mutableStateOf("PILKA_NOZNA") }

    // Lista musi zgadzać się z Enumem w Backendzie!
    val types = listOf("PILKA_NOZNA", "KORT_TENISOWY", "KOSZYKOWKA", "INNE")

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj boisko do obiektu") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa (np. Kort 1)") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena za godzinę (PLN)") })

                Spacer(Modifier.height(16.dp))
                Text("Typ boiska:", style = MaterialTheme.typography.titleSmall)

                // Radio Buttons dla Typów
                types.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedType == type),
                            onClick = { selectedType = type }
                        )
                        Text(type)
                    }
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    try {
                        val price = priceStr.toDoubleOrNull()
                        if (price == null) {
                            errorMessage = "Podaj poprawną cenę!"
                            return@launch
                        }

                        val request = AddFieldRequest(facilityId, name, selectedType, price)
                        val success = api.addField(userId, request)

                        if (success) {
                            onSuccess()
                            onDismiss()
                        } else {
                            errorMessage = "Błąd zapisu."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Błąd: ${e.message}"
                    }
                }
            }) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}