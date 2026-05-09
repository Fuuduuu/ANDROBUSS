package ee.androbus.core.domain

import java.time.DayOfWeek
import java.time.LocalDate

data class ServiceCalendar(
    val serviceId: ServiceId,
    val activeDays: Set<DayOfWeek>,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(activeDays.isNotEmpty()) { "ServiceCalendar activeDays cannot be empty." }
        require(!startDate.isAfter(endDate)) { "ServiceCalendar startDate must be on or before endDate." }
    }
}
