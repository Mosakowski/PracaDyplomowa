package org.pracainzynierska.sportbooking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Używamy Material 3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

// Importy Twoich modeli z modułu Shared
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.RegisterRequest
import org.pracainzynierska.sportbooking.LoginRequest
import org.pracainzynierska.sportbooking.AuthResponse

// Enum do prostej nawigacji: albo Logowanie, albo Lista
enum class Screen {
    LOGIN, LIST
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        // 1. Stan aplikacji: Na którym jesteśmy ekranie? Domyślnie LOGOWANIE.
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

        // Stan zalogowanego użytkownika (żeby wyświetlić "Witaj X")
        var currentUser by remember { mutableStateOf<AuthResponse?>(null) }

        // Klient API (wspólny dla wszystkich ekranów)
        val api = remember { SportApi() }

        // Główny kontener
        Column(Modifier.fillMaxSize()) {

            // Pasek górny (Header)
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SportBooking", style = MaterialTheme.typography.headlineSmall)
                // Jeśli użytkownik jest zalogowany, pokaż powitanie
                if (currentUser != null) {
                    Text(
                        "Witaj, ${currentUser?.name}!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 2. Nawigacja: Wybierz widok na podstawie zmiennej 'currentScreen'
            when (currentScreen) {
                Screen.LOGIN -> {
                    // Wyświetl ekran logowania
                    LoginScreen(
                        onLoginSuccess = { user ->
                            currentUser = user      // Zapisz dane usera
                            currentScreen = Screen.LIST // Przełącz na listę
                        },
                        api = api
                    )
                }
                Screen.LIST -> {
                    // Wyświetl listę boisk (Twój stary kod, zamknięty w funkcji)
                    FacilitiesScreen(api)
                }
            }
        }
    }
}

// --- KOMPONENT 1: EKRAN LOGOWANIA ---
@Composable
fun LoginScreen(onLoginSuccess: (AuthResponse) -> Unit, api: SportApi) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRegisterMode by remember { mutableStateOf(false) } // Przełącznik: Logowanie czy Rejestracja?
    val scope = rememberCoroutineScope() // Do obsługi asynchronicznego API

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isRegisterMode) "Załóż nowe konto" else "Zaloguj się",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        // Pola formularza
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adres Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            visualTransformation = PasswordVisualTransformation(), // Kropki zamiast liter
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Główny przycisk
        Button(
            onClick = {
                scope.launch {
                    try {
                        errorMessage = null
                        if (isRegisterMode) {
                            // 1. Próba rejestracji
                            val request = RegisterRequest(email, password, "Nowy Użytkownik")
                            val success = api.register(request)
                            if (success) {
                                errorMessage = "Sukces! Możesz się teraz zalogować."
                                isRegisterMode = false // Wróć do logowania
                            }
                        } else {
                            // 2. Próba logowania
                            val request = LoginRequest(email, password)
                            val user = api.login(request)
                            onLoginSuccess(user) // Wywołaj zmianę ekranu
                        }
                    } catch (e: Exception) {
                        errorMessage = "Błąd: ${e.message}"
                        println("Auth error: $e")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (isRegisterMode) "Zarejestruj się" else "Zaloguj")
        }

        // Komunikat o błędzie (jeśli jest)
        errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(msg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        // Przełącznik trybu (Text Button)
        TextButton(onClick = {
            isRegisterMode = !isRegisterMode
            errorMessage = null
        }) {
            Text(if (isRegisterMode) "Masz już konto? Zaloguj się" else "Nie masz konta? Zarejestruj się")
        }
    }
}

// --- KOMPONENT 2: LISTA BOISK (To co miałeś wcześniej) ---
@Composable
fun FacilitiesScreen(api: SportApi) {
    var facilities by remember { mutableStateOf<List<FacilityDto>>(emptyList()) }

    // Pobierz dane przy wejściu na ten ekran
    LaunchedEffect(Unit) {
        try {
            facilities = api.getFacilities()
        } catch (e: Exception) {
            println("Błąd pobierania listy: ${e.message}")
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Dostępne obiekty", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        facilities.forEach { facility ->
            FacilityCard(facility)
        }
    }
}

// --- KOMPONENT 3: KAFEL BOISKA ---
@Composable
fun FacilityCard(facility: FacilityDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = facility.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Lokalizacja: ${facility.location}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            facility.description?.let { desc ->
                Spacer(Modifier.height(4.dp))
                Text(text = desc, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}