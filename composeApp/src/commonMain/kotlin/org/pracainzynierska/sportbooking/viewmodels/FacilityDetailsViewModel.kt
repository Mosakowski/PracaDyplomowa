package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import org.pracainzynierska.sportbooking.FacilityDto

data class FacilityDetailsUiState(
    val facility: FacilityDto,
    val groupedFields: Map<String, List<org.pracainzynierska.sportbooking.FieldDto>>
)

class FacilityDetailsViewModel(
    val facility: FacilityDto
) : ViewModel() {
    val uiState = FacilityDetailsUiState(
        facility = facility,
        groupedFields = facility.fields.groupBy { it.type }
    )
}
