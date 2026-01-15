package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class AdminRepository {

    // 1. Pobierz Statystyki (Dashboard)
    fun getSystemStats(): AdminStatsDto {
        return transaction {
            val usersCount = Users.selectAll().count()
            val facilitiesCount = Facilities.selectAll().count()
            val bookingsCount = Bookings.selectAll().count()

            // Sumowanie przychodów
            val totalRevenue = Bookings.selectAll().map { it[Bookings.price] }.sumOf { it.toDouble() }

            AdminStatsDto(
                totalUsers = usersCount,
                totalFacilities = facilitiesCount,
                totalBookings = bookingsCount,
                totalRevenue = totalRevenue
            )
        }
    }

    fun getAllUsers(): List<UserDto> {
        return transaction {
            Users.selectAll().map { resultRow ->
                UserDto(
                    id = resultRow[Users.id],
                    email = resultRow[Users.email],
                    name = resultRow[Users.name],
                    role = resultRow[Users.role].name,
                    token = null
                )
            }
        }
    }

    // 3. Usuń użytkownika)
    fun deleteUser(targetUserId: Int): Boolean {
        return transaction {
            // usuwamy rezerwacje, które ten użytkownik ZROBIŁ (jako klient)
            Bookings.deleteWhere { userId eq targetUserId }

            // KROK 2: Usuwamy obiekty, których ten użytkownik jest WŁAŚCICIELEM
            Facilities.deleteWhere { userId eq targetUserId }

            // KROK 3: Na końcu usuwamy samego użytkownika
            val deletedCount = Users.deleteWhere { id eq targetUserId }

            deletedCount > 0
        }
    }

    fun getAllFacilities(): List<FacilityAdminDto> {
        return transaction {
            (Facilities innerJoin Users) // Łączymy tabele, żeby znać nazwę właściciela
                .selectAll()
                .map {
                    FacilityAdminDto(
                        id = it[Facilities.id],
                        name = it[Facilities.name],
                        ownerName = it[Users.name],
                        location = it[Facilities.location]
                    )
                }
        }
    }

    // 5. Usuń obiekt
    fun deleteFacility(facilityId: Int): Boolean {
        return transaction {
            // Najpierw usuwamy rezerwacje na boiskach tego obiektu

            // 1 Znajdź ID wszystkich boisk w tym obiekcie
            val fieldIds = Fields.select { Fields.facilityId eq facilityId }.map { it[Fields.id] }

            // 2 Usuń rezerwacje na tych boiskach
            if (fieldIds.isNotEmpty()) {
                Bookings.deleteWhere { fieldId inList fieldIds }
            }

            // 3. Usuń boiska
            Fields.deleteWhere { Fields.facilityId eq facilityId }

            // 4 Usuń sam obiekt
            val deleted = Facilities.deleteWhere { Facilities.id eq facilityId }
            deleted > 0
        }
    }
}