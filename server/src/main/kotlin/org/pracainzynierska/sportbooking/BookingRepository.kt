package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.*
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery
import java.time.Instant

class BookingRepository {

    // 1. Pobieranie historii (bez zmian - to działało)
    suspend fun getBookingsForUser(userId: Int): List<BookingDto> = dbQuery {
        (Bookings innerJoin Fields)
            .select { Bookings.userId eq userId }
            .orderBy(Bookings.start, SortOrder.DESC)
            .map { row ->
                BookingDto(
                    id = row[Bookings.id],
                    fieldId = row[Bookings.fieldId],
                    fieldName = row[Fields.name],
                    startTimestamp = row[Bookings.start].toEpochMilli(),
                    endTimestamp = row[Bookings.end].toEpochMilli(),
                    status = row[Bookings.status].name,
                    price = row[Bookings.price].toDouble()
                )
            }
    }

    // 2. NOWA METODA: Sprawdzanie dostępności (używana w Routingu)
    suspend fun isFieldAvailable(fieldId: Int, start: Long, end: Long): Boolean = dbQuery {
        val startInstant = Instant.ofEpochMilli(start)
        val endInstant = Instant.ofEpochMilli(end)

        // Sprawdzamy czy istnieje jakakolwiek rezerwacja nakładająca się na ten termin
        Bookings.select {
            (Bookings.fieldId eq fieldId) and
                    (Bookings.status neq BookingStatus.CANCELLED) and
                    (Bookings.start less endInstant) and    // Początek starej < Koniec nowej
                    (Bookings.end greater startInstant)     // Koniec starej > Początek nowej
        }.count() == 0L // Zwraca true, jeśli licznik wynosi 0 (czyli jest wolne)
    }

    // 3. ZAKTUALIZOWANA METODA: Tworzenie rezerwacji (przyjmuje gotowe Timestampy)
    suspend fun createBooking(userId: Int, fieldId: Int, start: Long, end: Long): Int = dbQuery {
        val startInstant = Instant.ofEpochMilli(start)
        val endInstant = Instant.ofEpochMilli(end)

        // Pobierz cenę boiska
        val pricePerSlot = Fields.select { Fields.id eq fieldId }
            .single()[Fields.pricePerSlot]

        // Zapisz rezerwację
        Bookings.insert {
            it[this.userId] = userId
            it[this.fieldId] = fieldId
            it[this.start] = startInstant
            it[this.end] = endInstant
            it[this.status] = BookingStatus.WAITING // Od razu potwierdzamy
            it[this.price] = pricePerSlot
        }[Bookings.id]
    }
}