package org.pracainzynierska.sportbooking


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Route.bookingRoutes() {
    val repo = BookingRepository()

    route("/api/bookings") {

        // POST: Tworzenie nowej rezerwacji
        post {
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            if (userIdHeader == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji")
                return@post
            }

            try {
                // 1. Odbieramy Request z napisami (String)
                val request = call.receive<CreateBookingRequest>()

                // 2. Serwer przelicza String -> Timestamp (Tu działa Java, więc jest stabilnie)
                // Zakładamy format "2024-06-01T14:00" (bez "Z" na końcu, zwykły ISO local)
                val startTs = LocalDateTime.parse(request.startIso).toInstant(ZoneOffset.UTC).toEpochMilli()
                val endTs = LocalDateTime.parse(request.endIso).toInstant(ZoneOffset.UTC).toEpochMilli()

                // 3. Sprawdzamy dostępność (używamy repozytorium jak dawniej)
                val isAvailable = repo.isFieldAvailable(request.fieldId, startTs, endTs)

                if (isAvailable) {
                    val bookingId = repo.createBooking(userIdHeader, request.fieldId, startTs, endTs)
                    call.respond(HttpStatusCode.Created, mapOf("id" to bookingId, "message" to "Zarezerwowano"))
                } else {
                    call.respond(HttpStatusCode.Conflict, "Termin jest zajęty")
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Błąd danych: ${e.message}")
            }
        }
        // 👇 DODAJ TO (GET): Pobieranie rezerwacji zalogowanego użytkownika
        get {
            // 1. Sprawdzamy, kto pyta (nagłówek)
            val userIdHeader = call.request.header("X-User-Id")
            val userId = userIdHeader?.toIntOrNull()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak ID użytkownika")
                return@get
            }

            // 2. Pobieramy z bazy
            val bookings = repo.getBookingsForUser(userId)

            // 3. Odsyłamy listę JSON
            call.respond(bookings)
        }
    }

}