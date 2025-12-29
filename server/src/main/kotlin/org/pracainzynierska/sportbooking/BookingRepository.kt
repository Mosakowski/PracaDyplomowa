package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery
import java.time.Instant

class BookingRepository {

    // Pobierz rezerwacje konkretnego użytkownika (do historii)
    suspend fun getBookingsForUser(userId: Int): List<BookingDto> = dbQuery {
        // Łączymy tabele (JOIN), żeby pobrać też nazwę boiska
        (Bookings innerJoin Fields)
            .select { Bookings.userId eq userId }
            .orderBy(Bookings.start, SortOrder.DESC) // Najnowsze na górze
            .map { row ->
                BookingDto(
                    id = row[Bookings.id],
                    fieldId = row[Bookings.fieldId],
                    fieldName = row[Fields.name], // Mamy to dzięki innerJoin
                    startTimestamp = row[Bookings.start].toEpochMilli(),
                    endTimestamp = row[Bookings.end].toEpochMilli(),
                    status = row[Bookings.status].name,
                    price = row[Bookings.price].toDouble()
                )
            }
    }

    // TWORZENIE REZERWACJI (z wykrywaniem kolizji)
    suspend fun createBooking(userId: Int, request: CreateBookingRequest): Int? = dbQuery {
        val startInstant = Instant.ofEpochMilli(request.startTimestamp)
        val endInstant = Instant.ofEpochMilli(request.endTimestamp)

        // 1. SPRAWDZENIE KOLIZJI
        // Szukamy rezerwacji na TYM SAMYM boisku, która nakłada się czasowo.
        // Logika: (StartA < EndB) AND (EndA > StartB)
        val collision = Bookings.select {
            (Bookings.fieldId eq request.fieldId) and
                    (Bookings.status neq BookingStatus.CANCELLED) and // Ignorujemy anulowane
                    (Bookings.start less endInstant) and
                    (Bookings.end greater startInstant)
        }.count() > 0

        if (collision) {
            return@dbQuery null // Jest kolizja, nie rezerwujemy!
        }

        // 2. Pobierz cenę boiska (żeby zapisać ją w rezerwacji)
        val pricePerSlot = Fields.select { Fields.id eq request.fieldId }
            .single()[Fields.pricePerSlot]

        // 3. Zapisz rezerwację
        Bookings.insert {
            it[this.userId] = userId
            it[this.fieldId] = request.fieldId
            it[start] = startInstant
            it[end] = endInstant
            it[status] = BookingStatus.WAITING
            it[price] = pricePerSlot // Uproszczenie: cena za cały slot = cena jednostkowa
        } get Bookings.id
    }
}