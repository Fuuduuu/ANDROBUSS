package ee.androbus.core.domain

import java.time.LocalDate

enum class ServiceExceptionType {
    ADD_SERVICE,
    REMOVE_SERVICE,
}

data class ServiceCalendarException(
    val serviceId: ServiceId,
    val date: LocalDate,
    val exceptionType: ServiceExceptionType,
)
