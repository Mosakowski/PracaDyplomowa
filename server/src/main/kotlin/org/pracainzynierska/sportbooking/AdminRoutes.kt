package org.pracainzynierska.sportbooking

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
    val repo = AdminRepository()

    route("/api/admin") {

        // Dashboard
        get("/stats") {
            // na przyszlosc, Tu można dodać sprawdzenie czy user to admin
            val stats = repo.getSystemStats()
            call.respond(stats)
        }

        // Lista userów
        get("/users") {
            val users = repo.getAllUsers()
            call.respond(users)
        }

        // Usuwanie usera
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            // Ochrona przed usunięciem samego siebie
            if (id == 1) {
                call.respond(HttpStatusCode.Forbidden, "Nie możesz usunąć Admina!")
                return@delete
            }

            if (repo.deleteUser(id)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Obiekty
        get("/facilities") {
            val list = repo.getAllFacilities()
            call.respond(list)
        }

        delete("/facilities/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            if (repo.deleteFacility(id)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

    }
}