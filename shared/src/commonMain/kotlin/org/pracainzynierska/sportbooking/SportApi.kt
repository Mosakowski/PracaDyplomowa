package org.pracainzynierska.sportbooking

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.* // Ważne: Do ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

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
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}