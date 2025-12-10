package org.pracainzynierska.sportbooking

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(CORS){
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        // Na produkcji tutaj wpiszesz konkretną domenę.
        // Na etapie dev pozwalamy na wszystko ("*"):
        anyHost()
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        }
        )
    }

    routing {
        facilityRoutes()
        authRoutes()
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")

        }
    }
}