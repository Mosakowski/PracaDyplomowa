package org.pracainzynierska.sportbooking


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookingRoutes() {
    val repo = BookingRepository()

    route("/api/bookings") {

        // POST: Tworzenie nowej rezerwacji
        post {
            // 1. Pobieramy ID użytkownika (Tymczasowa symulacja)
            val userIdHeader = call.request.header("X-User-Id")
            val userId = userIdHeader?.toIntOrNull()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak ID użytkownika")
                return@post
            }

            // 2. Odbieramy dane JSON
            val request = call.receive<CreateBookingRequest>()

            // 3. Próbujemy zarezerwować (zapis do bazy)
            val newId = repo.createBooking(userId, request)

            // 4. Obsługa wyniku
            if (newId != null) {
                // 👇 Zmieniamy ID na tekst (String), żeby mapa miała jeden typ danych <String, String>
                call.respond(HttpStatusCode.Created, mapOf("id" to newId.toString(), "message" to "Zarezerwowano!"))

            } else {
                call.respond(HttpStatusCode.Conflict, "Ten termin jest już zajęty!")
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