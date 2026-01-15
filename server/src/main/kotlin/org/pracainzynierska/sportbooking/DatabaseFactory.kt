package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcURL = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/sportbooking" // Adres bazy w Dockerze
        val user = System.getenv("JDBC_DATABASE_USERNAME") ?: "postgres"
        val password = System.getenv("JDBC_DATABASE_PASSWORD") ?: "admin" // hasło lokalne

        val database = Database.connect(jdbcURL, driverClassName, user, password)

        // Exposed: Automatycznie tworzy tabele, jeśli ich nie ma
        transaction(database) {
            SchemaUtils.create(Users, Facilities, Fields, Bookings)
        }
    }

    // Pomocnicza funkcja do zapytań asynchronicznych (żeby nie blokować serwera)
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}