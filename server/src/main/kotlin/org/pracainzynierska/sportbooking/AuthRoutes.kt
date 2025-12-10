package org.pracainzynierska.sportbooking

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.authRoutes() {
    val repo = UserRepository()

    route("/api/auth") {

        // POST /api/auth/register
        post("/register") {
            // 1. Odbierz dane (JSON -> RegisterRequest)
            val request = call.receive<RegisterRequest>()

            // 2. Spróbuj utworzyć usera
            val newId = repo.createUser(request)

            if (newId != null) {
                call.respond(HttpStatusCode.Created, mapOf("message" to "User created", "id" to newId))
            } else {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email already exists"))
            }
        }

        // POST /api/auth/login
        post("/login") {
            val request = call.receive<LoginRequest>()

            // 1. Znajdź usera w bazie
            val user = repo.findUserByEmail(request.email)

            // 2. Sprawdź hasło (czy pasuje do hasha)
            if (user != null && Security.checkPassword(request.password, user.passwordHash)) {
                // Generujemy prosty token (w przyszłości zamienimy na JWT)
                val fakeToken = UUID.randomUUID().toString()

                call.respond(AuthResponse(
                    token = fakeToken,
                    userId = user.id,
                    name = user.name,
                    role = user.role.toString()
                ))
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }
    }
}