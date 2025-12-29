package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcURL = "jdbc:postgresql://localhost:5432/sport_booking" // Adres bazy w Dockerze
        val user = "admin"
        val password = "admin"

        val database = Database.connect(jdbcURL, driverClassName, user, password)

        // Magia Exposed: Automatycznie tworzy tabele, jeśli ich nie ma!
        transaction(database) {
            SchemaUtils.create(Users, Facilities, Fields, Bookings)
        }
    }

    // Pomocnicza funkcja do zapytań asynchronicznych (żeby nie blokować serwera)
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}