package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SportApi

data class FacilitiesUiState(
    val facilities: List<FacilityDto> = emptyList(),
    val filteredFacilities: List<FacilityDto> = emptyList(),
    val searchQuery: String = "",
    val selectedSport: String? = null,
    val isLoading: Boolean = false,
    val allSports: List<String> = emptyList()
)

class FacilitiesViewModel(
    private val api: SportApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(FacilitiesUiState())
    val uiState: StateFlow<FacilitiesUiState> = _uiState.asStateFlow()

    init {
        fetchFacilities()
    }

    fun fetchFacilities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val f = api.getFacilities()
                _uiState.update { state ->
                    val sports = f.flatMap { it.fields.map { field -> field.type } }.distinct().sorted()
                    state.copy(
                        facilities = f,
                        allSports = sports,
                        isLoading = false
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                // Handle error
                println(e)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onSportSelected(sport: String?) {
        _uiState.update { it.copy(selectedSport = if (it.selectedSport == sport) null else sport) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.facilities.filter { facility ->
            val matchesText = if (state.searchQuery.isBlank()) true else {
                facility.name.contains(state.searchQuery, ignoreCase = true) ||
                        facility.location.contains(state.searchQuery, ignoreCase = true)
            }
            val matchesSport = if (state.selectedSport == null) true else {
                facility.fields.any { it.type == state.selectedSport }
            }
            matchesText && matchesSport
        }
        _uiState.update { it.copy(filteredFacilities = filtered) }
    }
}
