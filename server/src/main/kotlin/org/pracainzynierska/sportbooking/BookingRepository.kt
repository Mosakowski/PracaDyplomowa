package org.pracainzynierska.sportbooking

// --- IMPORTY (Java Time + Exposed) ---
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*

class BookingRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"))

    // 1. Sprawdzanie czy termin jest wolny
    suspend fun isFieldAvailable(fieldId: Int, start: Long, end: Long): Boolean = dbQuery {
        val startInstant = Instant.ofEpochMilli(start)
        val endInstant = Instant.ofEpochMilli(end)

        val clashCount = Bookings.selectAll().where {
            (Bookings.fieldId eq fieldId) and
                    (Bookings.status neq BookingStatus.CANCELLED) and
                    (Bookings.start less endInstant) and
                    (Bookings.end greater startInstant)
        }.count()

        return@dbQuery clashCount == 0L
    }

    // 2. Zapisywanie rezerwacji (POPRAWIONE)
    suspend fun createBooking(userId: Int, fieldId: Int, start: Long, end: Long, manualClientName: String? = null): Int = dbQuery {
        val startInstant = Instant.ofEpochMilli(start)
        val endInstant = Instant.ofEpochMilli(end)

        val fieldRow = Fields.selectAll().where { Fields.id eq fieldId }.single()
        val pricePerSlot = fieldRow[Fields.pricePerSlot]
        val slotDurationMin = fieldRow[Fields.minSlotDuration]

        val durationMillis = end - start
        val durationMinutes = durationMillis / 1000 / 60
        val slotsCount = (durationMinutes.toDouble() / slotDurationMin).coerceAtLeast(1.0)
        val totalPrice = pricePerSlot.multiply(slotsCount.toBigDecimal())

        Bookings.insert {
            it[Bookings.userId] = userId
            it[Bookings.fieldId] = fieldId
            it[Bookings.start] = startInstant
            it[Bookings.end] = endInstant
            it[Bookings.status] = BookingStatus.WAITING
            it[Bookings.price] = totalPrice
            it[Bookings.manualClientName] = manualClientName
        }[Bookings.id]
    }

    // 3. Pobieranie historii (JOIN z FACILITIES)
    suspend fun getBookingsForUser(userId: Int): List<BookingDto> = dbQuery {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("UTC"))

        (Bookings innerJoin Fields innerJoin Facilities)
            .selectAll().where { Bookings.userId eq userId }
            .orderBy(Bookings.start, SortOrder.DESC)
            .map { row ->
                val startInstant = row[Bookings.start]
                val endInstant = row[Bookings.end]

                BookingDto(
                    id = row[Bookings.id],
                    userId = row[Bookings.userId],
                    fieldId = row[Bookings.fieldId],
                    fieldName = row[Fields.name],

                    // Nowe dane
                    facilityName = row[Facilities.name],
                    facilityLocation = row[Facilities.location],

                    startTimestamp = startInstant.toEpochMilli(),
                    endTimestamp = endInstant.toEpochMilli(),
                    status = row[Bookings.status].name,
                    price = row[Bookings.price].toDouble(),
                    startDate = formatter.format(startInstant),
                    endDate = formatter.format(endInstant)
                )
            }
    }

    // 4. Anulowanie
    suspend fun cancelBooking(userId: Int, bookingId: Int): Boolean = dbQuery {
        val updatedRows = Bookings.update({ (Bookings.id eq bookingId) and (Bookings.userId eq userId) }) {
            it[status] = BookingStatus.CANCELLED
        }
        updatedRows > 0
    }

    // 5. Zajęte sloty
    suspend fun getTakenSlots(facilityId: Int, dateString: String): List<BookingDto> = dbQuery {
        val date = LocalDate.parse(dateString)
        val startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()

        val facilityFieldIds = Fields.slice(Fields.id)
            .selectAll().where { Fields.facilityId eq facilityId }
            .map { it[Fields.id] }

        if (facilityFieldIds.isEmpty()) return@dbQuery emptyList()

        Bookings.selectAll().where {
            (Bookings.fieldId inList facilityFieldIds) and
                    (Bookings.status neq BookingStatus.CANCELLED) and
                    (Bookings.start greaterEq startOfDay) and
                    (Bookings.end less endOfDay)
        }.map { row ->
            val startInstant = row[Bookings.start]
            val endInstant = row[Bookings.end]

            BookingDto(
                id = row[Bookings.id],
                userId = row[Bookings.userId],
                fieldId = row[Bookings.fieldId],
                fieldName = "Zajęte",
                facilityName = "",
                facilityLocation = "",

                startTimestamp = startInstant.toEpochMilli(),
                endTimestamp = endInstant.toEpochMilli(),
                status = row[Bookings.status].name,
                price = row[Bookings.price].toDouble(),
                startDate = startInstant.toString(),
                endDate = endInstant.toString()
            )
        }
    }

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("UTC"))
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("UTC"))

    // 6. Panel Właściciela - Timeline
    suspend fun getFacilityBookings(facilityId: Int, dateStr: String): List<OwnerBookingDto> = dbQuery {
        val date = LocalDate.parse(dateStr)
        val startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()

        (Bookings innerJoin Fields innerJoin Users)
            .selectAll().where {
                (Fields.facilityId eq facilityId) and
                        (Bookings.status neq BookingStatus.CANCELLED) and
                        (Bookings.start greaterEq startOfDay) and
                        (Bookings.end less endOfDay)
            }
            .orderBy(Bookings.start)
            .map { row -> mapToOwnerDto(row) }
    }

    // 7. Panel Właściciela - Feed
    suspend fun getRecentBookings(facilityId: Int, limit: Int = 5): List<OwnerBookingDto> = dbQuery {
        val fieldIds = Fields.slice(Fields.id).selectAll().where { Fields.facilityId eq facilityId }.map { it[Fields.id] }
        if (fieldIds.isEmpty()) return@dbQuery emptyList()

        (Bookings innerJoin Fields innerJoin Users)
            .selectAll().where {
                (Bookings.fieldId inList fieldIds) and
                        (Bookings.status neq BookingStatus.CANCELLED)
            }
            .orderBy(Bookings.createdAt, SortOrder.DESC)
            .limit(limit)
            .map { row -> mapToOwnerDto(row) }
    }

    // 8. Panel Właściciela - Statystyki
    suspend fun getFacilityStats(facilityId: Int): FacilityStatsDto = dbQuery {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val fieldIds = Fields.slice(Fields.id).selectAll().where { Fields.facilityId eq facilityId }.map { it[Fields.id] }

        if (fieldIds.isEmpty()) return@dbQuery FacilityStatsDto(0.0, 0, "Brak danych")

        val bookingsInMonth = Bookings.selectAll().where {
            (Bookings.fieldId inList fieldIds) and
                    (Bookings.status neq BookingStatus.CANCELLED) and
                    (Bookings.start greaterEq startOfMonth)
        }.toList()

        val revenue = bookingsInMonth.sumOf { it[Bookings.price] }.toDouble()
        val count = bookingsInMonth.size

        val popularFieldId = bookingsInMonth.groupingBy { it[Bookings.fieldId] }
            .eachCount()
            .maxByOrNull { it.value }?.key

        val popularFieldName = if (popularFieldId != null) {
            Fields.selectAll().where { Fields.id eq popularFieldId }.single()[Fields.name]
        } else "Brak danych"

        FacilityStatsDto(revenue, count, popularFieldName)
    }

    // 9. Anulowanie Właściciel
    suspend fun cancelByOwner(ownerId: Int, bookingId: Int): Boolean = dbQuery {
        val bookingRow = (Bookings innerJoin Fields innerJoin Facilities)
            .selectAll().where {
                (Bookings.id eq bookingId) and
                        (Facilities.userId eq ownerId)
            }
            .singleOrNull()

        if (bookingRow == null) return@dbQuery false

        Bookings.update({ Bookings.id eq bookingId }) {
            it[status] = BookingStatus.CANCELLED
        } > 0
    }

    // 10. Blokowanie (POPRAWIONE)
    suspend fun blockTerm(userId: Int, fieldId: Int, start: Long, end: Long): Int = dbQuery {
        val startInstant = Instant.ofEpochMilli(start)
        val endInstant = Instant.ofEpochMilli(end)

        Bookings.insert {
            it[Bookings.userId] = userId
            it[Bookings.fieldId] = fieldId
            it[Bookings.start] = startInstant
            it[Bookings.end] = endInstant
            it[Bookings.status] = BookingStatus.TECHNICAL
            it[Bookings.price] = 0.0.toBigDecimal()
        }[Bookings.id]
    }

    private fun mapToOwnerDto(row: ResultRow): OwnerBookingDto {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"))

        return OwnerBookingDto(
            id = row[Bookings.id],
            fieldId = row[Bookings.fieldId],
            fieldName = row[Fields.name],
            startTimestamp = row[Bookings.start].toEpochMilli(),
            endTimestamp = row[Bookings.end].toEpochMilli(),
            status = row[Bookings.status].name,
            price = row[Bookings.price].toDouble(),
            startDate = timeFormatter.format(row[Bookings.start]), // "14:00"
            endDate = timeFormatter.format(row[Bookings.end]),     // "15:30"
            clientName = row[Bookings.manualClientName] ?: row[Users.name],
            clientEmail = row[Users.email],
            bookingTime = dateTimeFormatter.format(row[Bookings.createdAt]),
            rawDate = dateFormatter.format(row[Bookings.start]) // "2026-01-13"
        )
    }
}