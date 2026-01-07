package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.selectAll
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery // Importuj swoją funkcję dbQuery

class FacilityRepository {

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