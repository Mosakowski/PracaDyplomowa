package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.selectAll
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery // Importuj swoją funkcję dbQuery
import org.jetbrains.exposed.sql.insert

class FacilityRepository {

    // ... wewnątrz klasy FacilityRepository ...

    // Zmieniliśmy nazwę argumentu na 'ownerId' żeby nie mylił się z kolumną
    suspend fun add(ownerId: Int, request: AddFacilityRequest): Int = dbQuery {
        Facilities.insert {
            // Teraz jest jasne: do kolumny Facilities.userId wstawiamy wartość ownerId
            it[Facilities.userId] = ownerId
            it[Facilities.name] = request.name
            it[Facilities.location] = request.location
            it[Facilities.description] = request.description ?: "" // Zabezpieczenie na null
        }[Facilities.id]
    }

    suspend fun addField(request: AddFieldRequest): Int = dbQuery {
        Fields.insert {
            it[facilityId] = request.facilityId
            it[name] = request.name
            // Konwersja String -> Enum (UWAGA: musi pasować idealnie do nazw w Enumie!)
            it[fieldType] = FieldType.valueOf(request.fieldType)
            // Konwersja Double -> BigDecimal
            it[pricePerSlot] = request.price.toBigDecimal()
        }[Fields.id]
    }

    // Funkcja pobierająca wszystkie obiekty
    suspend fun getAll(): List<FacilityDto> = dbQuery {
        // 1. Pobierz wszystkie obiekty
        val facilities = Facilities.selectAll().map { row ->
            FacilityDto(
                id = row[Facilities.id],
                name = row[Facilities.name],
                location = row[Facilities.location],
                description = row[Facilities.description],
                fields = emptyList() // Na razie pusta lista, zaraz uzupełnimy
            )
        }

        // 2. Pobierz wszystkie boiska i pogrupuj je po facility_id
        val fieldsMap = Fields.selectAll().map { row ->
            val facilityId = row[Fields.facilityId]
            val fieldDto = FieldDto(
                id = row[Fields.id],
                name = row[Fields.name],
                // 👇 Tu używamy .name
                type = row[Fields.fieldType].name
            )
            facilityId to fieldDto
        }.groupBy({ it.first }, { it.second }) // Grupujemy

        // 3. Połącz dane: Wstawiamy boiska do odpowiednich obiektów
        facilities.map { facility ->
            facility.copy(fields = fieldsMap[facility.id] ?: emptyList())
        }


    }
}