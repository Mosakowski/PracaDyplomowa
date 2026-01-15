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
import io.ktor.server.http.content.*
import java.io.File

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
        allowHeader("X-User-Id")

        // Na produkcji tutaj wpisze konkretną domenę.
        // Na etapie dev pozwalam na wszystko ("*"):
        anyHost()
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
        )
    }

    routing {
        facilityRoutes()
        authRoutes()
        bookingRoutes()
        adminRoutes()
        // SERWOWANIE STRONY WWW (Frontend Wasm)
        staticFiles("/", File("/app/static")) {
            default("index.html")
            enableAutoHeadResponse()
        }
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")

        }
    }
}