package org.pracainzynierska.sportbooking

import org.jetbrains.exposed.sql.selectAll
import org.pracainzynierska.sportbooking.DatabaseFactory.dbQuery // Importuj swoją funkcję dbQuery

class FacilityRepository {

    // Funkcja pobierająca wszystkie obiekty
    suspend fun getAllFacilities(): List<FacilityDto> = dbQuery {
        Facilities.selectAll().map { row ->
            FacilityDto(
                id = row[Facilities.id],
                name = row[Facilities.name],
                location = row[Facilities.location],
                description = row[Facilities.description]
            )
        }
    }
}