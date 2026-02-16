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

        get("/owner/{id}") {
            // 1. Wyciągamy {id} z adresu URL (np. /api/facilities/owner/5 -> id to 5)
            val ownerIdStr = call.parameters["id"]
            val ownerId = ownerIdStr?.toIntOrNull()

            // 2. Zabezpieczenie: jeśli ktoś wpisał bzdury (np. /owner/abc), odrzucamy z błędem
            if (ownerId == null) {
                call.respond(HttpStatusCode.BadRequest, "Niepoprawne ID właściciela")
                return@get
            }

            // 3. Pytamy repozytorium o obiekty tylko dla tego jednego ID
            val facilities = repo.getByOwnerId(ownerId)

            // 4. Zwracamy listę do aplikacji mobilnej/webowej
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
        put("/{id}") {
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            val facilityId = call.parameters["id"]?.toIntOrNull()

            if (userIdHeader == null || facilityId == null) {
                call.respond(HttpStatusCode.BadRequest, "Brak ID lub autoryzacji")
                return@put
            }

            // Używamy tego samego modelu co przy dodawaniu (AddFacilityRequest)
            val request = call.receive<AddFacilityRequest>()

            val updated = repo.update(facilityId, userIdHeader, request)

            if (updated) {
                call.respond(HttpStatusCode.OK, "Zaktualizowano")
            } else {
                call.respond(HttpStatusCode.NotFound, "Nie znaleziono obiektu lub brak uprawnień")
            }
        }

        // USUWANIE (DELETE /{id})
        delete("/{id}") {
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            val facilityId = call.parameters["id"]?.toIntOrNull()

            if (userIdHeader == null || facilityId == null) {
                call.respond(HttpStatusCode.BadRequest, "Brak ID lub autoryzacji")
                return@delete
            }

            val deleted = repo.delete(facilityId, userIdHeader)

            if (deleted) {
                call.respond(HttpStatusCode.OK, "Usunięto")
            } else {
                call.respond(HttpStatusCode.NotFound, "Nie znaleziono obiektu lub brak uprawnień")
            }
        }


    }

    route("/api/fields") {
        post {
            // 1. Sprawdzamy autoryzację
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            if (userIdHeader == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji")
                return@post
            }

            try {
                val request = call.receive<AddFieldRequest>()

                // 2. PRZEKAZUJEMY userIdHeader DO REPOZYTORIUM
                val newId = repo.addField(userIdHeader, request)

                call.respond(HttpStatusCode.Created, mapOf("id" to newId.toString(), "message" to "Boisko dodane!"))

            } catch (e: IllegalAccessException) {
                //  3. JEŚLI REPO RZUCIŁO BŁĄD, ŻE TO NIE TWÓJ OBIEKT
                call.respond(HttpStatusCode.Forbidden, "Nie masz prawa dodawać boisk do tego obiektu!")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Niepoprawny typ boiska!")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Błąd serwera: ${e.message}")
            }
        }
        // EDYCJA KONKRETNEGO BOISKA
        put("/{id}") {
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            val fieldId = call.parameters["id"]?.toIntOrNull()

            if (userIdHeader == null || fieldId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji lub ID")
                return@put
            }

            try {
                val request = call.receive<AddFieldRequest>()
                val updated = repo.updateField(userIdHeader, fieldId, request)

                if (updated) {
                    call.respond(HttpStatusCode.OK, "Boisko zaktualizowane")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "Nie znaleziono boiska lub brak uprawnień")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Błąd danych: ${e.message}")
            }
        }

        // USUWANIE KONKRETNEGO BOISKA
        delete("/{id}") {
            val userIdHeader = call.request.header("X-User-Id")?.toIntOrNull()
            val fieldId = call.parameters["id"]?.toIntOrNull()

            if (userIdHeader == null || fieldId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Brak autoryzacji lub ID")
                return@delete
            }

            val deleted = repo.deleteField(userIdHeader, fieldId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, "Boisko usunięte")
            } else {
                call.respond(HttpStatusCode.Forbidden, "Nie znaleziono boiska lub brak uprawnień")
            }
        }
    }
}