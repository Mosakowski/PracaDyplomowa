package org.pracainzynierska.sportbooking

import kotlinx.serialization.Serializable

@Serializable
data class FacilityDto(
    val id: Int,
    val name: String,
    val location: String,
    val description: String?
)