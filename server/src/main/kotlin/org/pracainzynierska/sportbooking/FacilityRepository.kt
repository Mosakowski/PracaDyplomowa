package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.*
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

// 👇 NOWE IMPORTY (Potrzebne do pracy z tekstowym JSONem z bazy)
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FacilityRepository {

    suspend fun add(ownerId: Int, request: AddFacilityRequest): Int = dbQuery {
        Facilities.insert {
            it[userId] = ownerId
            it[name] = request.name
            it[location] = request.location
            it[description] = request.description ?: ""
            // ❌ USUNIĘTE: openingTime, closingTime, maxDaysAdvance (teraz są w Fields)
        }[Facilities.id]
    }

    suspend fun addField(ownerId: Int, request: AddFieldRequest): Int = dbQuery {
        // A. NAJPIERW SPRAWDZAMY CZY USER JEST WŁAŚCICIELEM OBIEKTU
        val isOwner = Facilities.selectAll()
            .where { (Facilities.id eq request.facilityId) and (Facilities.userId eq ownerId) }.count() > 0

        if (!isOwner) {
            throw IllegalAccessException("Nie jesteś właścicielem tego obiektu!")
        }

        // B. JEŚLI JEST WŁAŚCICIELEM, TO DODAJEMY BOISKO
        Fields.insert {
            it[facilityId] = request.facilityId
            it[name] = request.name
            it[fieldType] = FieldType.valueOf(request.fieldType)
            it[pricePerSlot] = request.price.toBigDecimal()
            it[minSlotDuration] = request.minSlotDuration

            // 👇 NOWE POLA ZAAWANSOWANE (Zgodnie z planem)
            it[description] = request.description
            it[status] = FieldStatus.valueOf(request.status)
            it[photoUrl] = request.photoUrl
            it[maxDaysAdvance] = request.maxDaysAdvance
            it[cancellationHours] = request.cancellationHours

            // 🚨 MAGIA JSONA: Zamieniamy Mapę z requestu na zwykły string
            it[weeklySchedule] = Json.encodeToString(request.weeklySchedule)
        }[Fields.id]
    }

    suspend fun getAll(): List<FacilityDto> = dbQuery {
        val facilities = Facilities.selectAll().map { row ->
            FacilityDto(
                id = row[Facilities.id],
                name = row[Facilities.name],
                location = row[Facilities.location],
                description = row[Facilities.description],
                fields = emptyList(),
                ownerId = row[Facilities.userId]
            )
        }

        val fieldsMap = Fields.selectAll().map { row ->
            val facilityId = row[Fields.facilityId]

            // 🚨 ODPUSZCZANIE MAGII: Bierzemy tekst z bazy i zamieniamy z powrotem na Mapę
            val scheduleJson = row[Fields.weeklySchedule]
            val scheduleMap = if (!scheduleJson.isNullOrBlank()) {
                Json.decodeFromString<Map<DayOfWeekIso, DaySchedule>>(scheduleJson)
            } else {
                null
            }

            val fieldDto = FieldDto(
                id = row[Fields.id],
                name = row[Fields.name],
                type = row[Fields.fieldType].name,
                price = row[Fields.pricePerSlot].toDouble(),
                minSlotDuration = row[Fields.minSlotDuration],

                // 👇 NOWE POLA:
                description = row[Fields.description],
                status = row[Fields.status].name,
                photoUrl = row[Fields.photoUrl],
                maxDaysAdvance = row[Fields.maxDaysAdvance],
                cancellationHours = row[Fields.cancellationHours],
                weeklySchedule = scheduleMap
            )
            facilityId to fieldDto
        }.groupBy({ it.first }, { it.second })

        facilities.map { facility ->
            facility.copy(fields = fieldsMap[facility.id] ?: emptyList())
        }
    }

    suspend fun getByOwnerId(ownerId: Int): List<FacilityDto> = dbQuery {
        val facilities = Facilities.selectAll().where { Facilities.userId eq ownerId }.map { row ->
            FacilityDto(
                id = row[Facilities.id],
                name = row[Facilities.name],
                location = row[Facilities.location],
                description = row[Facilities.description],
                fields = emptyList(),
                ownerId = row[Facilities.userId]
            )
        }

        if (facilities.isEmpty()) return@dbQuery emptyList()
        val facilityIds = facilities.map { it.id }

        val fieldsMap = Fields.selectAll().where { Fields.facilityId inList facilityIds }.map { row ->
            val facilityId = row[Fields.facilityId]

            // 🚨 Znów dekodujemy JSON
            val scheduleJson = row[Fields.weeklySchedule]
            val scheduleMap = if (!scheduleJson.isNullOrBlank()) {
                Json.decodeFromString<Map<DayOfWeekIso, DaySchedule>>(scheduleJson)
            } else {
                null
            }

            val fieldDto = FieldDto(
                id = row[Fields.id],
                name = row[Fields.name],
                type = row[Fields.fieldType].name,
                price = row[Fields.pricePerSlot].toDouble(),
                minSlotDuration = row[Fields.minSlotDuration],

                description = row[Fields.description],
                status = row[Fields.status].name,
                photoUrl = row[Fields.photoUrl],
                maxDaysAdvance = row[Fields.maxDaysAdvance],
                cancellationHours = row[Fields.cancellationHours],
                weeklySchedule = scheduleMap
            )
            facilityId to fieldDto
        }.groupBy({ it.first }, { it.second })

        facilities.map { facility ->
            facility.copy(fields = fieldsMap[facility.id] ?: emptyList())
        }
    }

    suspend fun update(facilityId: Int, ownerId: Int, request: AddFacilityRequest): Boolean = dbQuery {
        val updatedRows = Facilities.update({ (Facilities.id eq facilityId) and (Facilities.userId eq ownerId) }) {
            it[name] = request.name
            it[location] = request.location
            it[description] = request.description ?: ""
            // ❌ Usunięto godziny
        }
        updatedRows > 0
    }

    suspend fun delete(facilityId: Int, ownerId: Int): Boolean = dbQuery {
        val isOwner = Facilities.selectAll().where { (Facilities.id eq facilityId) and (Facilities.userId eq ownerId) }.count() > 0
        if (!isOwner) return@dbQuery false

        val fieldIds = Fields.slice(Fields.id)
            .selectAll().where { Fields.facilityId eq facilityId }
            .map { it[Fields.id] }

        if (fieldIds.isNotEmpty()) {
            Bookings.deleteWhere { Bookings.fieldId inList fieldIds }
        }

        Fields.deleteWhere { Fields.facilityId eq facilityId }
        Facilities.deleteWhere { Facilities.id eq facilityId } > 0
    }

    suspend fun updateField(ownerId: Int, fieldId: Int, request: AddFieldRequest): Boolean = dbQuery {
        val fieldInUserFacility = Fields.innerJoin(Facilities)
            .selectAll().where { (Fields.id eq fieldId) and (Facilities.userId eq ownerId) }
            .count() > 0

        if (!fieldInUserFacility) return@dbQuery false

        Fields.update({ Fields.id eq fieldId }) {
            it[name] = request.name
            it[fieldType] = FieldType.valueOf(request.fieldType)
            it[pricePerSlot] = request.price.toBigDecimal()
            it[minSlotDuration] = request.minSlotDuration

            // 👇 Aktualizacja nowych zaawansowanych danych
            it[description] = request.description
            it[status] = FieldStatus.valueOf(request.status)
            it[photoUrl] = request.photoUrl
            it[maxDaysAdvance] = request.maxDaysAdvance
            it[cancellationHours] = request.cancellationHours
            it[weeklySchedule] = Json.encodeToString(request.weeklySchedule)
        } > 0
    }

    suspend fun deleteField(ownerId: Int, fieldId: Int): Boolean = dbQuery {
        val fieldInUserFacility = Fields.innerJoin(Facilities)
            .selectAll().where { (Fields.id eq fieldId) and (Facilities.userId eq ownerId) }
            .count() > 0

        if (!fieldInUserFacility) return@dbQuery false

        Bookings.deleteWhere { Bookings.fieldId eq fieldId }
        Fields.deleteWhere { Fields.id eq fieldId } > 0
    }
}