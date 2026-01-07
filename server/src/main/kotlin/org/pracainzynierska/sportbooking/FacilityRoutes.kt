package org.pracainzynierska.sportbooking

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.facilityRoutes() {
    val repo = FacilityRepository()

    route("/api/facilities") {
        get{
            val facilities = repo.getAll()
            call.respond(facilities)
        }
    }
}