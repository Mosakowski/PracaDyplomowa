package org.pracainzynierska.sportbooking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.pracainzynierska.sportbooking.BookingDto
import org.pracainzynierska.sportbooking.SessionManager
import org.pracainzynierska.sportbooking.SportApi

data class MyBookingsUiState(
    val bookings: List<BookingDto> = emptyList(),
    val isLoading: Boolean = false,
    val bookingToCancel: BookingDto? = null,
    val errorMessage: String? = null
)

class MyBookingsViewModel(
    private val api: SportApi,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val user = sessionManager.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(errorMessage = "Użytkownik nie jest zalogowany") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val fetched = api.getMyBookings(user.userId)
                _uiState.update { state ->
                    state.copy(
                        bookings = fetched.sortedByDescending { it.id },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Nie udało się pobrać rezerwacji") }
                println(e)
            }
        }
    }

    fun showCancelConfirmation(booking: BookingDto) {
        _uiState.update { it.copy(bookingToCancel = booking) }
    }

    fun hideCancelConfirmation() {
        _uiState.update { it.copy(bookingToCancel = null) }
    }

    fun cancelBooking() {
        val user = sessionManager.currentUser.value
        val booking = _uiState.value.bookingToCancel
        
        if (user == null || booking == null) return

        viewModelScope.launch {
            try {
                if (api.cancelBooking(user.userId, booking.id)) {
                    refresh()
                }
                _uiState.update { it.copy(bookingToCancel = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Nie udało się odwołać rezerwacji") }
            }
        }
    }
}
