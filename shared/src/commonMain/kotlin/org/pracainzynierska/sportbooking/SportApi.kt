package org.pracainzynierska.sportbooking

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.* // Ważne: Do ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.pracainzynierska.sportbooking.BookingDto
import io.ktor.client.statement.bodyAsText

class SportApi {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }


    //zmienione
    private val baseUrl = "https://rezerwacjaboisk.onrender.com/api"
    //private val baseUrl = "http://localhost:8080/api"

    // 1. Pobieranie boisk
    suspend fun getFacilities(): List<FacilityDto> {
        return client.get("$baseUrl/facilities").body()
    }

    // 2. Rejestracja (NOWE)
    // Funkcja przyjmuje obiekt RegisterRequest (ten z Models.kt)
    // Zwraca true, jeśli się udało, lub rzuca błędem
    suspend fun register(request: RegisterRequest): Boolean {
        val response = client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json) // Mówimy serwerowi: "Wysyłam JSON"
            setBody(request) // Pakujemy dane do koperty
        }
        return response.status == HttpStatusCode.Created
    }


    // 3. Logowanie (NOWE)
    // Zwraca AuthResponse (token + dane usera)
    suspend fun login(request: LoginRequest): AuthResponse {
        val response = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status == HttpStatusCode.OK) {
            // Tylko jeśli jest 200 OK, próbujemy czytać obiekt AuthResponse
            return response.body()
        } else {
            // W przeciwnym razie czytamy treść błędu (np. "Invalid credentials")
            val errorBody = response.bodyAsText()
            throw Exception("Błąd logowania (${response.status}): $errorBody")
        }
    }

    suspend fun createBooking(userId: Int, request: CreateBookingRequest): Boolean {
        val response = client.post("$baseUrl/bookings") {
            contentType(ContentType.Application.Json)
            // WAŻNE: Dodajemy nagłówek, którego wymaga Twój Backend (BookingRoutes.kt)
            header("X-User-Id", userId)
            setBody(request)
        }
        if (response.status == HttpStatusCode.Created) {
            return true
        } else {
            // Jeśli serwer zwrócił błąd (400, 409 itp.), czytamy jego treść
            val serverMessage = response.bodyAsText()
            // I rzucamy wyjątkiem, który złapie Twój App.kt
            throw Exception(serverMessage)
        }
    }

    suspend fun getMyBookings(userId: Int): List<BookingDto> {
        return client.get("$baseUrl/bookings") {
            header("X-User-Id", userId)
        }.body()
    }

    suspend fun addFacility(userId: Int, request: AddFacilityRequest): Boolean {
        val response = client.post("$baseUrl/facilities") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", userId)
            setBody(request)
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun addField(userId: Int, request: AddFieldRequest): Boolean {
        val response = client.post("$baseUrl/fields") { // Nowy endpoint /api/fields
            contentType(ContentType.Application.Json)
            header("X-User-Id", userId)
            setBody(request)
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun cancelBooking(userId: Int, bookingId: Int): Boolean {
        val response = client.delete("$baseUrl/bookings/$bookingId") {
            header("X-User-Id", userId)
        }
        return response.status == io.ktor.http.HttpStatusCode.OK
    }

    // Edycja
    suspend fun updateFacility(userId: Int, facilityId: Int, request: AddFacilityRequest): Boolean {
        val response = client.put("$baseUrl/facilities/$facilityId") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", userId)
            setBody(request)
        }
        return response.status == HttpStatusCode.OK
    }

    // Usuwanie
    suspend fun deleteFacility(userId: Int, facilityId: Int): Boolean {
        val response = client.delete("$baseUrl/facilities/$facilityId") {
            header("X-User-Id", userId)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun getTakenSlots(facilityId: Int, date: String): List<BookingDto> {
        // date musi być w formacie "YYYY-MM-DD"
        return client.get("$baseUrl/bookings/taken") {
            parameter("facilityId", facilityId)
            parameter("date", date)
        }.body()
    }

    // Edycja boiska (PUT /api/fields/{id})
    suspend fun updateField(userId: Int, fieldId: Int, request: AddFieldRequest): Boolean {
        val response = client.put("$baseUrl/fields/$fieldId") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", userId)
            setBody(request)
        }
        return response.status == HttpStatusCode.OK
    }

    // Usuwanie boiska (DELETE /api/fields/{id})
    suspend fun deleteField(userId: Int, fieldId: Int): Boolean {
        val response = client.delete("$baseUrl/fields/$fieldId") {
            header("X-User-Id", userId)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun getFacilityStats(userId: Int, facilityId: Int): FacilityStatsDto {
        return client.get("$baseUrl/owner/stats/$facilityId") {
            header("X-User-Id", userId)
        }.body()
    }

    suspend fun getOwnerBookings(userId: Int, facilityId: Int, date: String): List<OwnerBookingDto> {
        return client.get("$baseUrl/owner/bookings/$facilityId") {
            header("X-User-Id", userId)
            parameter("date", date)
        }.body()
    }

    suspend fun cancelByOwner(userId: Int, bookingId: Int): Boolean {
        val response = client.delete("$baseUrl/owner/booking/$bookingId") {
            header("X-User-Id", userId)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun blockSlot(userId: Int, request: CreateBookingRequest): Boolean {
        val response = client.post("$baseUrl/owner/block") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", userId)
            setBody(request)
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun getRecentBookings(userId: Int, facilityId: Int): List<OwnerBookingDto> {
        return client.get("$baseUrl/owner/recent/$facilityId") {
            header("X-User-Id", userId)
        }.body()
    }

    suspend fun getAdminStats(): AdminStatsDto {
        return client.get("$baseUrl/admin/stats").body()
    }

    suspend fun getAllUsers(): List<UserDto> {
        return client.get("$baseUrl/admin/users").body()
    }

    suspend fun deleteUser(userId: Int): Boolean {
        val response = client.delete("$baseUrl/admin/users/$userId")
        return response.status == HttpStatusCode.OK
    }

    suspend fun getAllFacilitiesAdmin(): List<FacilityAdminDto> {
        return client.get("$baseUrl/admin/facilities").body()
    }

    suspend fun deleteFacilityAdmin(id: Int): Boolean {
        val response = client.delete("$baseUrl/admin/facilities/$id")
        return response.status == HttpStatusCode.OK
    }
}

