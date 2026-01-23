package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.RegisterRequest
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.theme.RacingGreen

@Composable
fun RegisterScreen(
    api: SportApi,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isOwner by remember { mutableStateOf(false) }

    // 👇 NOWA ZMIENNA: Stan błędu dla emaila
    var emailError by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), // Dodałem padding dla estetyki
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(64.dp), tint = RacingGreen)
        Spacer(Modifier.height(16.dp))
        Text("Załóż konto", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = RacingGreen)
        Text("Wypełnij dane, aby dołączyć.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Imię i Nazwisko") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        //  ZMODYFIKOWANE POLE EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false // Resetujemy błąd, gdy użytkownik zaczyna poprawiać
                errorMessage = null
            },
            label = { Text("Adres Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = emailError, // Zapala czerwoną ramkę
            supportingText = {
                if (emailError) {
                    Text("Niepoprawny format adresu email", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Hasło") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it },
            label = { Text("Powtórz hasło") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword,
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = isOwner,
                onCheckedChange = { isOwner = it },
                colors = CheckboxDefaults.colors(checkedColor = RacingGreen)
            )
            Text("Chcę zarządzać obiektem (Konto Właściciela)")
        }

        Spacer(Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (isProcessing) return@Button

                // 1. Sprawdź puste pola
                if (email.isBlank() || password.isBlank() || name.isBlank()) {
                    errorMessage = "Wypełnij wszystkie pola."
                    return@Button
                }

                // 2.  SPRAWDZENIE EMAILA
                if (!isValidEmail(email)) {
                    emailError = true
                    errorMessage = "Podaj poprawny adres email."
                    return@Button
                }

                // 3. Sprawdź hasła
                if (password != confirmPassword) {
                    errorMessage = "Hasła nie są identyczne!"
                    return@Button
                }

                isProcessing = true
                errorMessage = null

                scope.launch {
                    try {
                        val request = RegisterRequest(name = name, email = email, password = password, isOwner = isOwner)
                        val success = api.register(request)

                        if (success) {
                            onRegisterSuccess()
                        } else {
                            errorMessage = "Rejestracja nieudana. Email może być zajęty."
                            isProcessing = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "Błąd: ${e.message}"
                        isProcessing = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RacingGreen),
            enabled = !isProcessing
        ) {
            if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("ZAREJESTRUJ SIĘ")
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Masz już konto? Zaloguj się", color = RacingGreen)
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    return email.matches(emailRegex.toRegex())
}