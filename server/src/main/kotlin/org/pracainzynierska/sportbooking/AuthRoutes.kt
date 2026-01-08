package org.pracainzynierska.sportbooking

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
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
            // repo.login lub validateUser powinno zwracać obiekt User (z danymi z bazy)
            val user = repo.getUserByEmail(request.email)

            if (user != null && BCrypt.checkpw(request.password, user.passwordHash)) {
                // Generujemy token (u Ciebie to pewnie prosty UUID)
                val token = java.util.UUID.randomUUID().toString()

                // 👇 TU JEST ZMIANA: Dodajemy user.role do odpowiedzi
                call.respond(
                    AuthResponse(
                        token = token,
                        userId = user.id,
                        name = user.name, // lub email, zależy co tam masz
                        role = user.role.toString() // Zamieniamy Enum na String
                    )
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Błędne dane logowania")
            }
        }
    }
}