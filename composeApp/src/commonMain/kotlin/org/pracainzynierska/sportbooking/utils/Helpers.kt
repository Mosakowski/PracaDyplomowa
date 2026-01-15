package org.pracainzynierska.sportbooking.utils

import kotlinx.datetime.LocalTime
import org.pracainzynierska.sportbooking.CreateBookingRequest
import org.pracainzynierska.sportbooking.FieldDto

// 1 Oblicza sumę cen wybranych slotów
fun calculateTotalPrice(selected: Set<Pair<Int, LocalTime>>, fields: List<FieldDto>): Double {
    var total = 0.0
    selected.forEach { (fieldId, _) ->
        val field = fields.find { it.id == fieldId }
        field?.let { total += it.price }
    }
    return total
}

// 2 scala sąsiednie sloty w jedną rezerwację
fun mergeSlotsToRequests(
    selected: Set<Pair<Int, LocalTime>>,
    fields: List<FieldDto>,
    dateStr: String
): List<CreateBookingRequest> {
    val byField = selected.groupBy { it.first }
    val requests = mutableListOf<CreateBookingRequest>()

    byField.forEach { (fieldId, slots) ->
        val field = fields.find { it.id == fieldId } ?: return@forEach
        val duration = field.minSlotDuration

        val times = slots.map { it.second }.sorted()

        if (times.isEmpty()) return@forEach

        var blockStart = times[0]
        var blockEnd = blockStart.addMinutes(duration)

        for (i in 1 until times.size) {
            val nextTime = times[i]

            if (nextTime == blockEnd) {
                blockEnd = nextTime.addMinutes(duration)
            } else {
                requests.add(CreateBookingRequest(fieldId, "${dateStr}T${blockStart}", "${dateStr}T${blockEnd}"))
                blockStart = nextTime
                blockEnd = blockStart.addMinutes(duration)
            }
        }
        requests.add(CreateBookingRequest(fieldId, "${dateStr}T${blockStart}", "${dateStr}T${blockEnd}"))
    }
    return requests
}

// Pomocnicza do dodawania minut
fun LocalTime.addMinutes(minutes: Int): LocalTime {
    val totalMinutes = this.hour * 60 + this.minute + minutes
    if (totalMinutes >= 24 * 60) return LocalTime(23, 59)
    return LocalTime(totalMinutes / 60, totalMinutes % 60)
}