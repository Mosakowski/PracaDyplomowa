package org.pracainzynierska.sportbooking

import kotlinx.serialization.Serializable

@Serializable
data class FacilityDto(
    val id: Int,
    val name: String,
    val location: String,
    val description: String?,
    val fields: List<FieldDto>,
    val ownerId: Int,
    val openingTime: String,    // np. "08:00"
    val closingTime: String,    // np. "22:00"
    val maxDaysAdvance: Int     // np. 30 (dni do przodu)
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

@Serializable
data class BookingDto(
    val id: Int,
    val userId: Int,
    val fieldId: Int,
    val fieldName: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val status: String,
    val price: Double,
    val startDate: String, // np. "2024-09-01 12:00"
    val endDate: String,    // np. "2024-09-01 13:00",
    val facilityName: String,
    val facilityLocation: String
)
@Serializable
data class CreateBookingRequest(
    val fieldId: Int,
    val startIso: String, // np. "2024-06-01T14:00"
    val endIso: String,    // np. "2024-06-01T15:00"
    val manualClientName: String? = null //dla recznej rezerwacji przez wlasciciela
)

@Serializable
data class FieldDto(
    val id: Int,
    val name: String,
    val type: String,
    val minSlotDuration: Int, // w minutach, np. 60 lub 90
    val price: Double
)

@Serializable
data class AddFacilityRequest(
    val name: String,
    val location: String,
    val description: String?,
    val openingTime: String = "08:00",
    val closingTime: String = "22:00",
    val maxDaysAdvance: Int = 30
)

@Serializable
data class AddFieldRequest(
    val facilityId: Int,
    val name: String,
    val fieldType: String,
    val price: Double,
    val minSlotDuration: Int = 60
)

@Serializable
data class OwnerBookingDto(
    val id: Int,
    val fieldId: Int,
    val fieldName: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val status: String,
    val price: Double,
    val startDate: String,
    val endDate: String,
    val clientName: String,
    val clientEmail: String,
    val bookingTime: String,
    val rawDate: String // np. "2026-01-13"
)

@Serializable
data class FacilityStatsDto(
    val monthlyRevenue: Double,
    val totalBookings: Int,
    val mostPopularField: String
)

@Serializable
data class AdminStatsDto(
    val totalUsers: Long,
    val totalFacilities: Long,
    val totalBookings: Long,
    val totalRevenue: Double
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val name: String,
    val role: String,
    val token: String? = null
)

@Serializable
data class FacilityAdminDto(
    val id: Int,
    val name: String,
    val ownerName: String, // Żeby admin wiedział czyje to
    val location: String
)