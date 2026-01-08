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

        // ANULOWANIE (DELETE /api/bookings/{id})
        delete("/{id}") {
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            val bookingId = call.parameters["id"]?.toIntOrNull()

            if (userIdHeader == null || bookingId == null) {
                println("DEBUG DELETE: userId=$userIdHeader, bookingId=$bookingId") // Zobaczysz to w konsoli serwera
                call.respond(HttpStatusCode.BadRequest, "Błąd: userId=$userIdHeader, bookingId=$bookingId")
                return@delete
            }

            println("🚨 DEBUG DELETE: Próba usunięcia rezerwacji ID=$bookingId przez Usera ID=$userIdHeader")

            val success = repo.cancelBooking(userIdHeader, bookingId)

            println("🚨 DEBUG WYNIK: Czy usunięto? $success")

            if (success) {
                call.respond(HttpStatusCode.OK, "Rezerwacja anulowana")
            } else {
                call.respond(HttpStatusCode.NotFound, "Nie znaleziono rezerwacji lub brak uprawnień")
            }
        }

        // POST: Tworzenie nowej rezerwacji
        post {
            // 1. Autoryzacja
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            if (userIdHeader == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji")
                return@post
            }

            try {
                // 2. Odbieramy Request (który ma pola typu String)
                val request = call.receive<CreateBookingRequest>()

                // 3. PARSOWANIE DATY (Tu działa Java - jest stabilnie)
                // Zamieniamy "2024-07-01T14:00" -> 1719835200000 (Milisekundy)
                // Używamy ZoneOffset.UTC dla ujednolicenia czasu w bazie.
                val startTs = LocalDateTime.parse(request.startIso).toInstant(ZoneOffset.UTC).toEpochMilli()
                val endTs = LocalDateTime.parse(request.endIso).toInstant(ZoneOffset.UTC).toEpochMilli()

                // 0.1. Blokada "Powrotu do Przeszłości"
                // Pobieramy aktualny czas serwera (w milisekundach UTC)
                val now = System.currentTimeMillis()

                if (startTs < now) {
                    call.respond(HttpStatusCode.BadRequest, "Nie można rezerwować w przeszłości! Marty McFly nie lubi tego.")
                    return@post
                }

                // 0.2. Blokada "Zakrzywienia Czasoprzestrzeni"
                // Sprawdzamy, czy koniec nie jest przed początkiem (np. Start 14:00, Koniec 13:00)
                if (endTs <= startTs) {
                    call.respond(HttpStatusCode.BadRequest, "Rezerwacja musi trwać co najmniej chwilę (Koniec > Start).")
                    return@post
                }

                // 4. Sprawdzamy dostępność (logika anty-kolizyjna)
                // Przekazujemy już przeliczone liczby (Long)
                val isAvailable = repo.isFieldAvailable(request.fieldId, startTs, endTs)

                if (isAvailable) {
                    // 5. Zapisujemy w bazie
                    val bookingId = repo.createBooking(userIdHeader, request.fieldId, startTs, endTs)
                    call.respond(HttpStatusCode.Created, mapOf("id" to bookingId.toString(), "message" to "Zarezerwowano"))
                } else {
                    call.respond(HttpStatusCode.Conflict, "Termin jest zajęty")
                }

            } catch (e: Exception) {
                // To wyłapie np. zły format daty wpisany przez użytkownika
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