package ee.androbus.core.domain

import java.time.LocalDate

class ServiceCalendarResolver(
    calendars: List<ServiceCalendar>,
    exceptions: List<ServiceCalendarException>,
) {
    private val calendarsByServiceId: Map<ServiceId, ServiceCalendar>
    private val exceptionsByServiceAndDate: Map<ServiceDateKey, ServiceCalendarException>

    init {
        val duplicateCalendarServiceIds = calendars
            .groupBy { it.serviceId }
            .filterValues { it.size > 1 }
            .keys
        require(duplicateCalendarServiceIds.isEmpty()) {
            "Duplicate calendars are not allowed for the same serviceId."
        }

        val duplicateExceptionKeys = exceptions
            .groupBy { ServiceDateKey(it.serviceId, it.date) }
            .filterValues { it.size > 1 }
            .keys
        require(duplicateExceptionKeys.isEmpty()) {
            "Duplicate exceptions are not allowed for the same serviceId and date."
        }

        calendarsByServiceId = calendars.associateBy { it.serviceId }
        exceptionsByServiceAndDate = exceptions.associateBy { ServiceDateKey(it.serviceId, it.date) }
    }

    fun isServiceActive(serviceId: ServiceId, date: LocalDate): Boolean {
        val exception = exceptionsByServiceAndDate[ServiceDateKey(serviceId, date)]
        if (exception != null) {
            return exception.exceptionType == ServiceExceptionType.ADD_SERVICE
        }

        val calendar = calendarsByServiceId[serviceId] ?: return false
        if (date.isBefore(calendar.startDate) || date.isAfter(calendar.endDate)) {
            return false
        }

        return date.dayOfWeek in calendar.activeDays
    }
}

private data class ServiceDateKey(
    val serviceId: ServiceId,
    val date: LocalDate,
)
