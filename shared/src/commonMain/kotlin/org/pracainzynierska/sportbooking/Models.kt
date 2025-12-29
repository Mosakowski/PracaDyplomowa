package org.pracainzynierska.sportbooking

import kotlinx.serialization.Serializable

@Serializable
data class FacilityDto(
    val id: Int,
    val name: String,
    val location: String,
    val description: String?
)

// 1. Co wysyła użytkownik, gdy chce się zarejestrować?
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    // Domyślnie rejestrujemy jako CLIENT, ale w przyszłości można to rozbudować
    val isOwner: Boolean = false
)

// 2. Co wysyła, gdy chce się zalogować?
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// 3. Co odsyła serwer po udanym logowaniu?
@Serializable
data class AuthResponse(
    val token: String, // "Przepustka" do dalszych zapytań
    val userId: Int,
    val name: String,
    val role: String   // Np. "CLIENT" lub "FIELD_OWNER"
)

// ... (istniejące DTO)

@Serializable
data class BookingDto(
    val id: Int,
    val fieldId: Int,
    val fieldName: String, // Przydatne do wyświetlania na liście "Moje rezerwacje"
    val startTimestamp: Long, // Data w milisekundach
    val endTimestamp: Long,
    val status: String,
    val price: Double
)

@Serializable
data class CreateBookingRequest(
    val fieldId: Int,
    val startTimestamp: Long,
    val endTimestamp: Long
)