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

import org.pracainzynierska.sportbooking.viewmodels.RegisterViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(64.dp), tint = RacingGreen)
        Spacer(Modifier.height(16.dp))
        Text("Załóż konto", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = RacingGreen)
        Text("Wypełnij dane, aby dołączyć.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.name, onValueChange = { viewModel.onNameChanged(it) },
            label = { Text("Imię i Nazwisko") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Adres Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.emailError,
            supportingText = {
                if (uiState.emailError) {
                    Text("Niepoprawny format adresu email", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password, onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Hasło") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.confirmPassword, onValueChange = { viewModel.onConfirmPasswordChanged(it) },
            label = { Text("Powtórz hasło") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.password.isNotEmpty() && uiState.confirmPassword.isNotEmpty() && uiState.password != uiState.confirmPassword,
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = uiState.isOwner,
                onCheckedChange = { viewModel.onIsOwnerChanged(it) },
                colors = CheckboxDefaults.colors(checkedColor = RacingGreen)
            )
            Text("Chcę zarządzać obiektem (Konto Właściciela)")
        }

        Spacer(Modifier.height(24.dp))

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.register() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RacingGreen),
            enabled = !uiState.isProcessing
        ) {
            if (uiState.isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("ZAREJESTRUJ SIĘ")
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Masz już konto? Zaloguj się", color = RacingGreen)
        }
    }
}