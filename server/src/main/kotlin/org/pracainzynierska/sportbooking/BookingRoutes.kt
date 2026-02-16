package org.pracainzynierska.sportbooking


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode


fun Route.bookingRoutes() {
    val repo = BookingRepository()

    route("/api/bookings") {

        // ANULOWANIE (DELETE /api/bookings/{id})
        delete("/{id}") {
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            val bookingId = call.parameters["id"]?.toIntOrNull()

            if (userIdHeader == null || bookingId == null) {
                println("DEBUG DELETE: userId=$userIdHeader, bookingId=$bookingId")
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

                // 3. PARSOWANIE DATY
                // zamieniamy "2024-07-01T14:00" -> 1719835200000 (ms)
                // używamy ZoneOffset.UTC dla ujednolicenia czasu w bazie
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
                    val bookingId = repo.createBooking(userIdHeader, request.fieldId, startTs, endTs, request.manualClientName)
                    call.respond(HttpStatusCode.Created, mapOf("id" to bookingId.toString(), "message" to "Zarezerwowano"))
                } else {
                    call.respond(HttpStatusCode.Conflict, "Termin jest zajęty")
                }

            } catch (e: Exception) {
                // To wyłapie np. zły format daty wpisany przez użytkownika
                call.respond(HttpStatusCode.BadRequest, "Błąd danych: ${e.message}")
            }

        }


        // Pobieranie rezerwacji zalogowanego użytkownika
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

        get("/taken") {
            val facilityId = call.request.queryParameters["facilityId"]?.toIntOrNull()
            val date = call.request.queryParameters["date"] // Oczekujemy "YYYY-MM-DD"

            if (facilityId == null || date == null) {
                call.respond(HttpStatusCode.BadRequest, "Brak facilityId lub daty")
                return@get
            }

            try {
                val slots = repo.getTakenSlots(facilityId, date)
                call.respond(slots)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Błąd: ${e.message}")
            }
        }

    }
    route("/api/owner") {
        // Statystyki
        get("/stats/{facilityId}") {
            val userId = call.request.header("X-User-Id")?.toIntOrNull()
            val facilityId = call.parameters["facilityId"]?.toIntOrNull()
            if (userId == null || facilityId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            // Tu przydałoby się sprawdzić czy userId to faktycznie właściciel (w repo), na przyszlosc
            val stats = repo.getFacilityStats(facilityId)
            call.respond(stats)
        }

        // Rezerwacje na dany dzień (Timeline)
        get("/bookings/{facilityId}") {
            val userId = call.request.header("X-User-Id")?.toIntOrNull()
            val facilityId = call.parameters["facilityId"]?.toIntOrNull()
            val date = call.request.queryParameters["date"] // YYYY-MM-DD

            if (userId == null || facilityId == null || date == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val bookings = repo.getFacilityBookings(facilityId, date)
            call.respond(bookings)
        }

        // Anulowanie przez właściciela
        delete("/booking/{id}") {
            val userId = call.request.header("X-User-Id")?.toIntOrNull()
            val bookingId = call.parameters["id"]?.toIntOrNull()
            if (userId == null || bookingId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val success = repo.cancelByOwner(userId, bookingId)
            if (success) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.Forbidden, "Brak dostępu")
        }

        // POST: Blokowanie terminu
        post("/block") {
            val userId = call.request.header("X-User-Id")?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            try {
                val req = call.receive<CreateBookingRequest>()

                // Konwersja dat (tak jak przy zwykłej rezerwacji)
                val startTs = LocalDateTime.parse(req.startIso).toInstant(ZoneOffset.UTC).toEpochMilli()
                val endTs = LocalDateTime.parse(req.endIso).toInstant(ZoneOffset.UTC).toEpochMilli()

                // Sprawdzamy dostępność
                if (repo.isFieldAvailable(req.fieldId, startTs, endTs)) {
                    repo.blockTerm(userId, req.fieldId, startTs, endTs)
                    call.respond(HttpStatusCode.Created, "Zablokowano")
                } else {
                    call.respond(HttpStatusCode.Conflict, "Termin zajęty")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Error")
            }
        }

        // Ostatnie rezerwacje (Feed)
        get("/recent/{facilityId}") {
            val facilityId = call.parameters["facilityId"]?.toIntOrNull()
            if (facilityId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val recent = repo.getRecentBookings(facilityId)
            call.respond(recent)
        }
    }



}