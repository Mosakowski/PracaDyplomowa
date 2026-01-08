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

class SportApi {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Pamiętaj: localhost działa na Webie. Na Androidzie będziesz musiał to zmienić na 10.0.2.2!
    private val baseUrl = "http://localhost:8080/api"

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
        return response.status == HttpStatusCode.Created
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
}

