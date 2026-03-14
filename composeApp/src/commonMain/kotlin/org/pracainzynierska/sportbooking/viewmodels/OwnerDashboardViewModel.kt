package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.SessionManager

data class OwnerDashboardUiState(
    val myFacilities: List<FacilityDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OwnerDashboardViewModel(
    private val api: SportApi,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerDashboardUiState())
    val uiState: StateFlow<OwnerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadMyFacilities()
    }

    fun loadMyFacilities() {
        val user = sessionManager.currentUser.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Assuming api.getOwnerFacilities(userId) exists, 
                // but based on previous code it was using api.getFacilities().filter { it.ownerId == userId }
                val all = api.getFacilities()
                val mine = all.filter { it.ownerId == user.userId }
                _uiState.update { it.copy(myFacilities = mine, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd pobierania twoich obiektów") }
            }
        }
    }

    fun logout() {
        sessionManager.currentUser.value = null
    }
}
