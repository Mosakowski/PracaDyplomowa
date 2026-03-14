package org.pracainzynierska.sportbooking.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import org.pracainzynierska.sportbooking.BookingDto
import org.pracainzynierska.sportbooking.FacilityDto
import org.pracainzynierska.sportbooking.components.BlikPaymentDialog
import org.pracainzynierska.sportbooking.theme.RacingGreen
import org.pracainzynierska.sportbooking.theme.RacingGreenLight
import org.pracainzynierska.sportbooking.utils.calculateTotalPrice

import org.pracainzynierska.sportbooking.viewmodels.SchedulerViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen(
    facility: FacilityDto,
    onBack: () -> Unit,
    viewModel: SchedulerViewModel = koinViewModel { parametersOf(facility) }
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- HELPERY DO PRZETWARZANIA DATY I CZASU ---
    fun getPolishDayAbbr(day: DayOfWeek): String = when(day) {
        DayOfWeek.MONDAY -> "PON."
        DayOfWeek.TUESDAY -> "WT."
        DayOfWeek.WEDNESDAY -> " ŚR."
        DayOfWeek.THURSDAY -> "CZW."
        DayOfWeek.FRIDAY -> "PT."
        DayOfWeek.SATURDAY -> "SOB."
        DayOfWeek.SUNDAY -> "ND."
        else -> day.name.take(3)
    }

    fun getPolishMonthAbbr(month: Month): String = when(month) {
        Month.JANUARY -> "Sty"
        Month.FEBRUARY -> "Lut"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Kwi"
        Month.MAY -> "Maj"
        Month.JUNE -> "Cze"
        Month.JULY -> "Lip"
        Month.AUGUST -> "Sie"
        Month.SEPTEMBER -> "Wrz"
        Month.OCTOBER -> "Paź"
        Month.NOVEMBER -> "Lis"
        Month.DECEMBER -> "Gru"
        else -> month.name.take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(facility.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                
                if (facility.fields.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Obiekt nie posiada żadnych boisk.", color = Color.Gray)
                    }
                    return@Column
                }

                // --- ZAKŁADKI BOISK ---
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = RacingGreen
                ) {
                    facility.fields.forEachIndexed { index, field ->
                        Tab(
                            selected = uiState.selectedTabIndex == index,
                            onClick = { viewModel.onTabSelected(index) },
                            text = { 
                                Text(
                                    field.name, 
                                    fontWeight = if(uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // --- WYBÓR DATY ---
                val currentField = facility.fields.getOrNull(uiState.selectedTabIndex) ?: facility.fields.first()
                val today = LocalDate(2026, 2, 16) // Mocked today
                val daysList = remember(currentField.maxDaysAdvance) {
                    (0 until currentField.maxDaysAdvance).map { today.plus(DatePeriod(days = it)) }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(daysList) { date ->
                        val isSelected = (date == uiState.selectedDate)
                        val contentColor = if (isSelected) RacingGreen else Color.Gray
                        val circleColor = if (isSelected) RacingGreen else Color.Transparent
                        val numberColor = if (isSelected) Color.White else Color.Black
                        val borderColor = if (isSelected) RacingGreen else Color.LightGray

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.onDateSelected(date) }.padding(4.dp)
                        ) {
                            Text(getPolishDayAbbr(date.dayOfWeek), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = contentColor)
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = circleColor,
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = numberColor)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(getPolishMonthAbbr(date.month), style = MaterialTheme.typography.labelSmall, color = contentColor)
                        }
                    }
                }

                // --- LISTA SLOTÓW CZASOWYCH ---
                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = RacingGreen)
                        }
                    } else {
                        val startHour = 8
                        val endHour = 22
                        val slotsCount = (endHour - startHour) * 2
                        
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            repeat(slotsCount) { index ->
                                val hour = startHour + index / 2
                                val minute = (index % 2) * 30
                                val time = LocalTime(hour, minute)
                                
                                val isTaken = uiState.takenSlots.any { 
                                    it.fieldId == currentField.id && it.startDate.contains(time.toString().substring(0, 5))
                                }
                                val isSelected = uiState.selectedSlots.contains(currentField.id to time)

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isTaken -> Color.LightGray.copy(alpha = 0.5f)
                                            isSelected -> RacingGreenLight.copy(alpha = 0.5f)
                                            else -> Color.White
                                        }
                                    ),
                                    onClick = { if (!isTaken) viewModel.onSlotToggled(currentField.id, time) },
                                    border = if (isSelected) BorderStroke(2.dp, RacingGreen) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                ) {
                                    Row(Modifier.fillMaxHeight().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(time.toString().substring(0, 5), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Spacer(Modifier.width(16.dp))
                                        Text(
                                            text = if (isTaken) "Zajęte" else if (isSelected) "Wybrano" else "Dostępne",
                                            color = if (isTaken) Color.Red else if (isSelected) RacingGreen else Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // --- PASEK PODSUMOWANIA ---
                if (uiState.selectedSlots.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = Color.White
                    ) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val totalPrice = calculateTotalPrice(uiState.selectedSlots, facility.fields)
                            Column {
                                Text("Wybrano: ${uiState.selectedSlots.size} slotów", style = MaterialTheme.typography.bodySmall)
                                Text("${totalPrice} PLN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RacingGreen)
                            }
                            Button(
                                onClick = { viewModel.setShowPaymentDialog(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = RacingGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ZAREZERWUJ")
                            }
                        }
                    }
                }
            }

            // --- DIALOG PŁATNOŚCI ---
            if (uiState.showPaymentDialog) {
                val priceToPay = calculateTotalPrice(uiState.selectedSlots, facility.fields)
                BlikPaymentDialog(
                    totalPrice = priceToPay,
                    onDismiss = { viewModel.setShowPaymentDialog(false) },
                    onConfirmPayment = { viewModel.confirmPayment() }
                )
            }

            // --- KOMUNIKATY (SNACKBAR) ---
            uiState.message?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp, start = 16.dp, end = 16.dp),
                    action = { TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") } }
                ) {
                    Text(it)
                }
            }
        }
    }
}