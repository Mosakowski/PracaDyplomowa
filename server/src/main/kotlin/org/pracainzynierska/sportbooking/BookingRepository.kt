package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.*
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BookingRepository {

    // 1. Sprawdzanie czy termin jest wolny (Logika slotów/przedziałów)
    suspend fun isFieldAvailable(fieldId: Int, start: Long, end: Long): Boolean = dbQuery {
        val startInstant = Instant.ofEpochMilli(start)
        val endInstant = Instant.ofEpochMilli(end)

        // SQL: Policz rezerwacje, które NIE są anulowane i NAKŁADAJĄ SIĘ na nasz termin
        val clashCount = Bookings.select {
            (Bookings.fieldId eq fieldId) and
                    (Bookings.status neq BookingStatus.CANCELLED) and
                    // Matematyka przedziałów: (StartIstniejacy < KoniecNowy) AND (KoniecIstniejacy > StartNowy)
                    (Bookings.start less endInstant) and
                    (Bookings.end greater startInstant)
        }.count()

        // Jeśli licznik wynosi 0, to znaczy że jest wolne (zwracamy true)
        return@dbQuery clashCount == 0L
    }

    // 2. Zapisywanie rezerwacji (Prosty INSERT)
    suspend fun createBooking(userId: Int, fieldId: Int, start: Long, end: Long): Int = dbQuery {
        val startInstant = Instant.ofEpochMilli(start)
        val endInstant = Instant.ofEpochMilli(end)

        // Pobieramy cenę
        val pricePerSlot = Fields.select { Fields.id eq fieldId }
            .single()[Fields.pricePerSlot]

        // Wstawiamy rekord
        Bookings.insert {
            it[this.userId] = userId
            it[this.fieldId] = fieldId
            it[this.start] = startInstant
            it[this.end] = endInstant
            it[this.status] = BookingStatus.WAITING // lub CONFIRMED
            it[this.price] = pricePerSlot
        }[Bookings.id]
    }

    // 3. Pobieranie historii (żebyś miał kompletny plik)
    suspend fun getBookingsForUser(userId: Int): List<BookingDto> = dbQuery {
        // Formatter do daty (np. 2024-09-01 12:00)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.of("UTC")) // Używamy UTC, żeby było spójnie

        (Bookings innerJoin Fields)
            .select { Bookings.userId eq userId }
            .orderBy(Bookings.start, SortOrder.DESC)
            .map { row ->
                // Konwersja na Instant (Java Time)
                val startInstant = row[Bookings.start]
                val endInstant = row[Bookings.end]

                BookingDto(
                    id = row[Bookings.id],
                    fieldId = row[Bookings.fieldId],
                    fieldName = row[Fields.name],
                    startTimestamp = startInstant.toEpochMilli(),
                    endTimestamp = endInstant.toEpochMilli(),
                    status = row[Bookings.status].name,
                    price = row[Bookings.price].toDouble(),

                    // 👇 TU DZIEJE SIĘ MAGIA: Serwer zamienia czas na ładny tekst
                    startDate = formatter.format(startInstant),
                    endDate = formatter.format(endInstant)
                )
            }
    }

    // ANULOWANIE REZERWACJI
    // Zwraca true, jeśli udało się anulować (czyli rezerwacja istniała i należała do tego usera)
    suspend fun cancelBooking(userId: Int, bookingId: Int): Boolean = dbQuery {
        val updatedRows = Bookings.update({ (Bookings.id eq bookingId) and (Bookings.userId eq userId) }) {
            it[status] = BookingStatus.CANCELLED
        }
        // Jeśli zaktualizowano 1 wiersz, to znaczy że sukces.
        // Jeśli 0, to znaczy że rezerwacja nie istnieje albo należy do kogoś innego.
        updatedRows > 0
    }
}