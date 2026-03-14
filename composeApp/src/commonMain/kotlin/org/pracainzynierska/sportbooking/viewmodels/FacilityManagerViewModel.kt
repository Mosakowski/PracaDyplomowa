package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.pracainzynierska.sportbooking.*
import org.pracainzynierska.sportbooking.SessionManager

data class FacilityManagerUiState(
    val stats: FacilityStatsDto? = null,
    val recentBookings: List<OwnerBookingDto> = emptyList(),
    val bookings: List<OwnerBookingDto> = emptyList(),
    val selectedDate: LocalDate = LocalDate(2026, 2, 16),
    val selectedTabIndex: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FacilityManagerViewModel(
    private val api: SportApi,
    private val sessionManager: SessionManager,
    private val facility: FacilityDto
) : ViewModel() {

    private val _uiState = MutableStateFlow(FacilityManagerUiState())
    val uiState: StateFlow<FacilityManagerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val user = sessionManager.currentUser.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = api.getFacilityStats(user.userId, facility.id)
                val recent = api.getRecentBookings(user.userId, facility.id)
                val bookings = api.getOwnerBookings(user.userId, facility.id, _uiState.value.selectedDate.toString())
                
                _uiState.update { it.copy(
                    stats = stats,
                    recentBookings = recent,
                    bookings = bookings,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd pobierania danych managera") }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadBookingsForDate()
    }

    private fun loadBookingsForDate() {
        val user = sessionManager.currentUser.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bookings = api.getOwnerBookings(user.userId, facility.id, _uiState.value.selectedDate.toString())
                _uiState.update { it.copy(bookings = bookings, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd pobierania rezerwacji") }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }
}
