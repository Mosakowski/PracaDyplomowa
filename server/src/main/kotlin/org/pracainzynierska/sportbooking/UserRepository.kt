package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.*
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery
import org.pracainzynierska.sportbooking.Users
import org.pracainzynierska.sportbooking.UserRole

class UserRepository {

    // 1. Tworzenie nowego użytkownika (Rejestracja)
    suspend fun createUser(request: RegisterRequest): Int? = dbQuery {
        // Najpierw sprawdź, czy email już istnieje
        val existingUser = Users.select { Users.email eq request.email }.singleOrNull()
        if (existingUser != null) {
            return@dbQuery null // Email zajęty -> zwracamy null
        }

        // Dodaj do bazy (INSERT)
        Users.insert {
            it[email] = request.email
            it[name] = request.name
            // WAŻNE: Haszujemy hasło przed zapisem!
            it[password] = Security.hashPassword(request.password)

            // Mapowanie boolean (isOwner) na Enum z Twojego PDF-a
            it[role] = if (request.isOwner) UserRole.FIELD_OWNER else UserRole.CLIENT
        } get Users.id // Zwróć ID nowo utworzonego usera
    }

    // 2. Pobieranie użytkownika po emailu (Logowanie)
    suspend fun findUserByEmail(email: String) = dbQuery {
        Users.select { Users.email eq email }
            .map { row ->
                // Zwracamy obiekt z hasłem (hashem), żeby potem je sprawdzić
                UserRow(
                    id = row[Users.id],
                    email = row[Users.email],
                    passwordHash = row[Users.password],
                    name = row[Users.name],
                    role = row[Users.role]
                )
            }
            .singleOrNull()
    }

    suspend fun getUserByEmail(email: String): User? = dbQuery {
        // SELECT * FROM users WHERE email = ...
        Users.select { Users.email eq email }
            .map {
                User(
                    id = it[Users.id],
                    email = it[Users.email],
                    name = it[Users.name],
                    passwordHash = it[Users.password],

                    // 👇 Tu mapujemy kolumnę userType (z bazy) na pole role (w klasie User)
                    role = it[Users.role]
                )
            }
            .singleOrNull()
    }

}

// Pomocnicza klasa wewnętrzna (tylko dla backendu), żeby wygodnie przekazywać dane z bazy
data class UserRow(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val name: String,
    val role: UserRole
)

// To jest model wewnętrzny serwera - odwzorowanie wiersza z bazy
data class User(
    val id: Int,
    val email: String,
    val name: String,
    val passwordHash: String,
    val role: UserRole // 👈 Twoje UserRole z Schema.kt
)