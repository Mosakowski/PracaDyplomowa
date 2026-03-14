package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.FieldDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.SessionManager

data class OwnerFacilityDetailsUiState(
    val facility: FacilityDto,
    val groupedFields: Map<String, List<FieldDto>>,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showEditFacilityDialog: Boolean = false,
    val showDeleteFacilityDialog: Boolean = false,
    val showAddFieldDialog: Boolean = false,
    val fieldToEdit: FieldDto? = null,
    val fieldToDelete: FieldDto? = null
)

class OwnerFacilityDetailsViewModel(
    private val api: SportApi,
    private val sessionManager: SessionManager,
    initialFacility: FacilityDto
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OwnerFacilityDetailsUiState(
            facility = initialFacility,
            groupedFields = initialFacility.fields.groupBy { it.type }
        )
    )
    val uiState: StateFlow<OwnerFacilityDetailsUiState> = _uiState.asStateFlow()

    fun refresh() {
        val user = sessionManager.currentUser.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val myFacilities = api.getMyFacilities(user.userId)
                val updated = myFacilities.find { it.id == _uiState.value.facility.id }
                if (updated != null) {
                    _uiState.update { it.copy(
                        facility = updated,
                        groupedFields = updated.fields.groupBy { it.type },
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd odświeżania danych") }
            }
        }
    }

    fun setShowEditFacilityDialog(show: Boolean) = _uiState.update { it.copy(showEditFacilityDialog = show) }
    fun setShowDeleteFacilityDialog(show: Boolean) = _uiState.update { it.copy(showDeleteFacilityDialog = show) }
    fun setShowAddFieldDialog(show: Boolean) = _uiState.update { it.copy(showAddFieldDialog = show) }
    fun setFieldToEdit(field: FieldDto?) = _uiState.update { it.copy(fieldToEdit = field) }
    fun setFieldToDelete(field: FieldDto?) = _uiState.update { it.copy(fieldToDelete = field) }

    suspend fun deleteFacility(): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        return api.deleteFacility(user.userId, _uiState.value.facility.id)
    }

    suspend fun deleteField(fieldId: Int): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        return api.deleteField(user.userId, fieldId)
    }
}
