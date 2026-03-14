package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.SessionManager

data class AdminUiState(
    val facilities: List<FacilityDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val api: SportApi,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val facilities = api.getFacilities()
                _uiState.update { it.copy(facilities = facilities, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd pobierania danych") }
            }
        }
    }

    fun logout() {
        sessionManager.currentUser.value = null
    }
}
