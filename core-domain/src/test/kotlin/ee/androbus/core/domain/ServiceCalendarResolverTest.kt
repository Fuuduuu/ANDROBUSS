package ee.androbus.core.domain

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class ServiceCalendarResolverTest {
    private val serviceA = ServiceId("service-a")
    private val serviceB = ServiceId("service-b")
    private val start = LocalDate.of(2026, 5, 1)
    private val end = LocalDate.of(2026, 5, 31)

    @Test
    fun `ServiceCalendar rejects empty activeDays`() {
        assertFailsWith<IllegalArgumentException> {
            ServiceCalendar(
                serviceId = serviceA,
                activeDays = emptySet(),
                startDate = start,
                endDate = end,
            )
        }
    }

    @Test
    fun `ServiceCalendar rejects startDate after endDate`() {
        assertFailsWith<IllegalArgumentException> {
            ServiceCalendar(
                serviceId = serviceA,
                activeDays = setOf(DayOfWeek.MONDAY),
                startDate = end,
                endDate = start,
            )
        }
    }

    @Test
    fun `weekday active inside date range returns true`() {
        val resolver = resolver(calendars = listOf(mondayCalendar()))
        val monday = LocalDate.of(2026, 5, 4)

        assertTrue(resolver.isServiceActive(serviceA, monday))
    }

    @Test
    fun `inactive weekday inside date range returns false`() {
        val resolver = resolver(calendars = listOf(mondayCalendar()))
        val tuesday = LocalDate.of(2026, 5, 5)

        assertFalse(resolver.isServiceActive(serviceA, tuesday))
    }

    @Test
    fun `date before startDate returns false`() {
        val resolver = resolver(calendars = listOf(mondayCalendar()))

        assertFalse(resolver.isServiceActive(serviceA, LocalDate.of(2026, 4, 27)))
    }

    @Test
    fun `date after endDate returns false`() {
        val resolver = resolver(calendars = listOf(mondayCalendar()))

        assertFalse(resolver.isServiceActive(serviceA, LocalDate.of(2026, 6, 1)))
    }

    @Test
    fun `REMOVE_SERVICE exception overrides active base calendar`() {
        val targetDate = LocalDate.of(2026, 5, 4)
        val resolver = resolver(
            calendars = listOf(mondayCalendar()),
            exceptions = listOf(
                ServiceCalendarException(
                    serviceId = serviceA,
                    date = targetDate,
                    exceptionType = ServiceExceptionType.REMOVE_SERVICE,
                ),
            ),
        )

        assertFalse(resolver.isServiceActive(serviceA, targetDate))
    }

    @Test
    fun `ADD_SERVICE exception overrides inactive weekday`() {
        val targetDate = LocalDate.of(2026, 5, 5)
        val resolver = resolver(
            calendars = listOf(mondayCalendar()),
            exceptions = listOf(
                ServiceCalendarException(
                    serviceId = serviceA,
                    date = targetDate,
                    exceptionType = ServiceExceptionType.ADD_SERVICE,
                ),
            ),
        )

        assertTrue(resolver.isServiceActive(serviceA, targetDate))
    }

    @Test
    fun `ADD_SERVICE works with no base calendar`() {
        val targetDate = LocalDate.of(2026, 5, 5)
        val resolver = resolver(
            exceptions = listOf(
                ServiceCalendarException(
                    serviceId = serviceA,
                    date = targetDate,
                    exceptionType = ServiceExceptionType.ADD_SERVICE,
                ),
            ),
        )

        assertTrue(resolver.isServiceActive(serviceA, targetDate))
    }

    @Test
    fun `no base calendar and no exception returns false`() {
        val resolver = resolver()

        assertFalse(resolver.isServiceActive(serviceA, LocalDate.of(2026, 5, 5)))
    }

    @Test
    fun `REMOVE_SERVICE with no base calendar returns false`() {
        val targetDate = LocalDate.of(2026, 5, 5)
        val resolver = resolver(
            exceptions = listOf(
                ServiceCalendarException(
                    serviceId = serviceA,
                    date = targetDate,
                    exceptionType = ServiceExceptionType.REMOVE_SERVICE,
                ),
            ),
        )

        assertFalse(resolver.isServiceActive(serviceA, targetDate))
    }

    @Test
    fun `exception applies only to exact serviceId and date`() {
        val targetDate = LocalDate.of(2026, 5, 5)
        val resolver = resolver(
            exceptions = listOf(
                ServiceCalendarException(
                    serviceId = serviceA,
                    date = targetDate,
                    exceptionType = ServiceExceptionType.ADD_SERVICE,
                ),
            ),
        )

        assertTrue(resolver.isServiceActive(serviceA, targetDate))
        assertFalse(resolver.isServiceActive(serviceB, targetDate))
        assertFalse(resolver.isServiceActive(serviceA, targetDate.plusDays(1)))
    }

    @Test
    fun `duplicate exceptions for same serviceId and date are rejected`() {
        val targetDate = LocalDate.of(2026, 5, 5)

        assertFailsWith<IllegalArgumentException> {
            resolver(
                exceptions = listOf(
                    ServiceCalendarException(
                        serviceId = serviceA,
                        date = targetDate,
                        exceptionType = ServiceExceptionType.ADD_SERVICE,
                    ),
                    ServiceCalendarException(
                        serviceId = serviceA,
                        date = targetDate,
                        exceptionType = ServiceExceptionType.REMOVE_SERVICE,
                    ),
                ),
            )
        }
    }

    @Test
    fun `resolver behavior is deterministic from explicit LocalDate input`() {
        val resolver = resolver(calendars = listOf(mondayCalendar()))
        val monday = LocalDate.of(2026, 5, 11)
        val tuesday = LocalDate.of(2026, 5, 12)

        assertTrue(resolver.isServiceActive(serviceA, monday))
        assertFalse(resolver.isServiceActive(serviceA, tuesday))
        assertTrue(resolver.isServiceActive(serviceA, monday))
    }

    @Test
    fun `duplicate base calendars for same serviceId are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            resolver(
                calendars = listOf(
                    mondayCalendar(),
                    mondayCalendar(),
                ),
            )
        }
    }

    private fun mondayCalendar(serviceId: ServiceId = serviceA): ServiceCalendar =
        ServiceCalendar(
            serviceId = serviceId,
            activeDays = setOf(DayOfWeek.MONDAY),
            startDate = start,
            endDate = end,
        )

    private fun resolver(
        calendars: List<ServiceCalendar> = emptyList(),
        exceptions: List<ServiceCalendarException> = emptyList(),
    ): ServiceCalendarResolver = ServiceCalendarResolver(calendars = calendars, exceptions = exceptions)
}
