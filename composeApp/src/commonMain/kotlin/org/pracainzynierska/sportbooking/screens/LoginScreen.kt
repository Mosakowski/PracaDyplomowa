package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.AuthResponse
import org.pracainzynierska.sportbooking.LoginRequest
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.theme.RacingGreen

import org.pracainzynierska.sportbooking.viewmodels.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (AuthResponse) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loginSuccess) {
        uiState.loginSuccess?.let {
            onLoginSuccess(it)
            viewModel.resetLoginSuccess()
        }
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, null, modifier = Modifier.size(64.dp), tint = RacingGreen)
        Spacer(Modifier.height(16.dp))

        Text("Witaj ponownie", style = MaterialTheme.typography.headlineMedium, color = RacingGreen, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.email, onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Adres email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.password, onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Hasło") },
            leadingIcon = { Icon(Icons.Default.Key, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Zaloguj się")
        }

        uiState.errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Nie masz konta? Załóż je tutaj")
        }
    }
}