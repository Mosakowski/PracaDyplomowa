package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.pracainzynierska.sportbooking.BookingDto
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.SessionManager
import org.pracainzynierska.sportbooking.SportApi
import org.pracainzynierska.sportbooking.utils.mergeSlotsToRequests

data class SchedulerUiState(
    val selectedDate: LocalDate = LocalDate(2026, 2, 16),
    val takenSlots: List<BookingDto> = emptyList(),
    val selectedSlots: Set<Pair<Int, LocalTime>> = emptySet(),
    val isLoading: Boolean = false,
    val selectedTabIndex: Int = 0,
    val showPaymentDialog: Boolean = false,
    val message: String? = null
)

class SchedulerViewModel(
    private val api: SportApi,
    private val sessionManager: SessionManager,
    private val facility: FacilityDto
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulerUiState())
    val uiState: StateFlow<SchedulerUiState> = _uiState.asStateFlow()

    init {
        loadTakenSlots()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedSlots = emptySet()) }
        loadTakenSlots()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index, selectedSlots = emptySet()) }
    }

    fun onSlotToggled(fieldId: Int, time: LocalTime) {
        _uiState.update { state ->
            val slot = fieldId to time
            val newSlots = if (state.selectedSlots.contains(slot)) {
                state.selectedSlots - slot
            } else {
                state.selectedSlots + slot
            }
            state.copy(selectedSlots = newSlots)
        }
    }

    fun setShowPaymentDialog(show: Boolean) {
        _uiState.update { it.copy(showPaymentDialog = show) }
    }

    fun loadTakenSlots() {
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val slots = api.getTakenSlots(facility.id, date.toString())
                _uiState.update { it.copy(takenSlots = slots, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                println("Błąd: $e")
            }
        }
    }

    fun confirmPayment() {
        val user = sessionManager.currentUser.value ?: return
        val state = _uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(showPaymentDialog = false, isLoading = true) }
            try {
                val requests = mergeSlotsToRequests(state.selectedSlots, facility.fields, state.selectedDate.toString())
                var successCount = 0
                var errorMsg: String? = null
                
                requests.forEach { req ->
                    try {
                        if (api.createBooking(user.userId, req)) successCount++
                    } catch (e: Exception) {
                        errorMsg = e.message
                    }
                }

                if (successCount > 0) {
                    _uiState.update { it.copy(
                        message = "Płatność przyjęta! Zarezerwowano pomyślnie.",
                        selectedSlots = emptySet(),
                        isLoading = false
                    ) }
                    loadTakenSlots()
                } else {
                    _uiState.update { it.copy(message = "Błąd: $errorMsg", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Błąd krytyczny: ${e.message}", isLoading = false) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
