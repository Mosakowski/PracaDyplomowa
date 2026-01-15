package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.*
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class FacilityRepository {

    suspend fun add(ownerId: Int, request: AddFacilityRequest): Int = dbQuery {
        Facilities.insert {
            it[userId] = ownerId
            it[name] = request.name
            it[location] = request.location
            it[description] = request.description ?: ""
            it[openingTime] = request.openingTime
            it[closingTime] = request.closingTime
            it[maxDaysAdvance] = request.maxDaysAdvance
        }[Facilities.id]
    }

    suspend fun addField(ownerId: Int, request: AddFieldRequest): Int = dbQuery {
        // A. NAJPIERW SPRAWDZAMY CZY USER JEST WŁAŚCICIELEM OBIEKTU
        val isOwner = Facilities.selectAll()
            .where { (Facilities.id eq request.facilityId) and (Facilities.userId eq ownerId) }.count() > 0

        if (!isOwner) {
            // Jeśli nie jest właścicielem, rzucamy błąd (który wyłapiemy w Routes)
            throw IllegalAccessException("Nie jesteś właścicielem tego obiektu!")
        }

        // B. JEŚLI JEST WŁAŚCICIELEM, TO DODAJEMY
        Fields.insert {
            it[facilityId] = request.facilityId
            it[name] = request.name
            it[fieldType] = FieldType.valueOf(request.fieldType)
            it[pricePerSlot] = request.price.toBigDecimal()
            it[minSlotDuration] = request.minSlotDuration
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
                openingTime = row[Facilities.openingTime],
                closingTime = row[Facilities.closingTime],
                maxDaysAdvance = row[Facilities.maxDaysAdvance],
                ownerId = row[Facilities.userId]
            )
        }

        // 2. Pobieramy boiska
        val fieldsMap = Fields.selectAll().map { row ->
            val facilityId = row[Fields.facilityId]
            val fieldDto = FieldDto(
                id = row[Fields.id],
                name = row[Fields.name],
                type = row[Fields.fieldType].name,
                price = row[Fields.pricePerSlot].toDouble(),
                minSlotDuration = row[Fields.minSlotDuration]
            )
            facilityId to fieldDto
        }.groupBy({ it.first }, { it.second })

        //łączymy
        facilities.map { facility ->
            facility.copy(fields = fieldsMap[facility.id] ?: emptyList())
        }
    }
    suspend fun update(facilityId: Int, ownerId: Int, request: AddFacilityRequest): Boolean = dbQuery {
        val updatedRows = Facilities.update({ (Facilities.id eq facilityId) and (Facilities.userId eq ownerId) }) {
            it[name] = request.name
            it[location] = request.location
            it[description] = request.description ?: ""
            it[openingTime] = request.openingTime
            it[closingTime] = request.closingTime
            it[maxDaysAdvance] = request.maxDaysAdvance
        }
        updatedRows > 0
    }

    // USUWANIE OBIEKTU
    suspend fun delete(facilityId: Int, ownerId: Int): Boolean = dbQuery {
        // 1. Sprawdzamy czy obiekt istnieje i czy user jest właścicielem
        val isOwner = Facilities.selectAll().where { (Facilities.id eq facilityId) and (Facilities.userId eq ownerId) }.count() > 0

        if (!isOwner) return@dbQuery false

        // 2. Pobieramy listę ID wszystkich boisk należących do tego obiektu
        val fieldIds = Fields.slice(Fields.id)
            .selectAll().where { Fields.facilityId eq facilityId }
            .map { it[Fields.id] }

        // 3. Usuwamy REZERWACJE powiązane z tymi boiskami
        // Jeśli lista boisk nie jest pusta, usuwamy ich rezerwacje
        if (fieldIds.isNotEmpty()) {
            Bookings.deleteWhere { Bookings.fieldId inList fieldIds }
        }

        // 4. Usuwamy BOISKA powiązane z tym obiektem
        Fields.deleteWhere { Fields.facilityId eq facilityId }

        // 5. Na końcu usuwamy sam OBIEKT
        Facilities.deleteWhere { Facilities.id eq facilityId } > 0
    }

    //  1. EDYCJA BOISKA
    suspend fun updateField(ownerId: Int, fieldId: Int, request: AddFieldRequest): Boolean = dbQuery {
        // Sprawdzamy, czy boisko należy do obiektu, którego właścicielem jest ownerId
        // Łączymy tabelę Fields z Facilities
        val fieldInUserFacility = Fields.innerJoin(Facilities)
            .selectAll().where { (Fields.id eq fieldId) and (Facilities.userId eq ownerId) }
            .count() > 0

        if (!fieldInUserFacility) return@dbQuery false

        Fields.update({ Fields.id eq fieldId }) {
            it[name] = request.name
            it[fieldType] = FieldType.valueOf(request.fieldType)
            it[pricePerSlot] = request.price.toBigDecimal()
            it[minSlotDuration] = request.minSlotDuration
        } > 0
    }

    //  2. USUWANIE BOISKA
    suspend fun deleteField(ownerId: Int, fieldId: Int): Boolean = dbQuery {
        // Sprawdzamy własność
        val fieldInUserFacility = Fields.innerJoin(Facilities)
            .selectAll().where { (Fields.id eq fieldId) and (Facilities.userId eq ownerId) }
            .count() > 0

        if (!fieldInUserFacility) return@dbQuery false

        // A. Najpierw usuwamy rezerwacje tego boiska
        Bookings.deleteWhere { Bookings.fieldId eq fieldId }

        // B. Usuwamy boisko
        Fields.deleteWhere { Fields.id eq fieldId } > 0
    }
}