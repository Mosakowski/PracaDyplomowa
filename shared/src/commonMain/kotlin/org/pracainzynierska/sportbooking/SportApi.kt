package org.pracainzynierska.sportbooking

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Klasa odpowiedzialna za komunikację z serwerem
class SportApi {

    // 1. Tworzymy "wirtualną przeglądarkę" (HttpClient)
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Ignoruj pola, których nie znamy (bezpieczeństwo)
            })
        }
    }

    // 2. Adres Twojego serwera.
    // UWAGA: Na Androidzie 'localhost' oznacza telefon, nie komputer.
    // Do emulatora używa się 10.0.2.2, ale na Webie 'localhost' jest OK.
    private val baseUrl = "http://localhost:8080/api"

    // 3. Funkcja pobierająca listę obiektów
    // 'suspend' oznacza, że funkcja nie zablokuje ekranu podczas czekania na odpowiedź
    // Zwraca listę naszych DTO, które zdefiniowaliśmy wcześniej
    suspend fun getFacilities(): List<FacilityDto> {
        // Wyślij zapytanie GET, pobierz odpowiedź (body) i zamień JSON na obiekty Kotlin
        return client.get("$baseUrl/facilities").body()
    }
}