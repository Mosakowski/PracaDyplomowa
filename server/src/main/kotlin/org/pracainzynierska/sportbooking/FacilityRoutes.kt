package org.pracainzynierska.sportbooking

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.facilityRoutes() {
    val repo = FacilityRepository()

    route("/api/facilities") {
        get{
            val facilities = repo.getAll()
            call.respond(facilities)
        }

        post {
            // 1. Sprawdzamy, kto dodaje (musi być zalogowany)
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            if (userIdHeader == null) {
                call.respond(HttpStatusCode.Unauthorized, "Musisz być zalogowany")
                return@post
            }

            // 2. Odczytujemy JSON-a z prośbą
            val request = call.receive<AddFacilityRequest>()

            // 3. Zapisujemy w bazie
            val newId = repo.add(userIdHeader, request)

            // 4. Odpowiadamy sukcesem
            call.respond(HttpStatusCode.Created, mapOf("id" to newId.toString(), "message" to "Obiekt dodany!"))
        }
    }

    route("/api/fields") {
        post {
            // 1. Sprawdzamy autoryzację
            val userIdHeader = call.request.header("X-User-Id")
            if (userIdHeader == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji")
                return@post
            }

            try {
                // 2. Odbieramy dane
                val request = call.receive<AddFieldRequest>()

                // 3. Zapisujemy
                val newId = repo.addField(request)

                call.respond(HttpStatusCode.Created, mapOf("id" to newId.toString(), "message" to "Boisko dodane!"))
            } catch (e: IllegalArgumentException) {
                // To wyłapie sytuację, gdy wyślesz zły typ (np. "PING_PONG" którego nie ma w Enumie)
                call.respond(HttpStatusCode.BadRequest, "Niepoprawny typ boiska! Dostępne: PILKA_NOZNA, KORT_TENISOWY, KOSZYKOWKA, INNE")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Błąd serwera: ${e.message}")
            }
        }
    }
}