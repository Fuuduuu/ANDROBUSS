package ee.androbus.core.gtfs

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.ServiceCalendarResolver
import ee.androbus.core.domain.ServiceId
import ee.androbus.core.domain.StopPointId
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RakvereProfileSmokeTest {
    private val parser = GtfsFeedParser()
    private val mapper = GtfsDomainMapper()
    private val cityId = CityId("rakvere")
    private val feedId = FeedId("rakvere-profile-smoke")

    @Test
    fun `parser parses rakvere profile smoke without exception`() {
        val parsed = parsedProfileFeed()

        assertTrue(parsed.stops.isNotEmpty())
        assertTrue(parsed.trips.isNotEmpty())
        assertTrue(parsed.stopTimes.isNotEmpty())
        assertTrue(parsed.calendars.isNotEmpty())
        assertTrue(parsed.calendarDates.isNotEmpty())
    }

    @Test
    fun `quoted service_id with commas is preserved across trips calendars and calendar_dates`() {
        val parsed = parsedProfileFeed()

        assertTrue(parsed.trips.any { it.serviceId == "Mock_1,2,3-Mo" })
        assertTrue(parsed.calendars.any { it.serviceId == "Mock_1,2,3-Mo" })
        assertTrue(parsed.calendarDates.any { it.serviceId == "Mock_1,2,3-Mo" })
    }

    @Test
    fun `unknown extra columns are tolerated`() {
        val parsed = parsedProfileFeed()

        assertEquals(4, parsed.stops.size)
        assertEquals(2, parsed.routes.size)
        assertEquals(3, parsed.trips.size)
        assertEquals(7, parsed.stopTimes.size)
    }

    @Test
    fun `block_id column present in fixture does not alter current gtfs trip model behavior`() {
        val parsed = parsedProfileFeed()

        val trip = parsed.trips.first { it.tripId == "TRIP_1" }
        assertEquals("ROUTE_1", trip.routeId)
        assertEquals("Mock_1,2,3-Mo", trip.serviceId)
        assertEquals("Polikliinik", trip.tripHeadsign)
    }

    @Test
    fun `missing optional files are tolerated`() {
        val fixtureDir = resourcePath("gtfs/rakvere-profile-smoke")

        assertTrue(Files.notExists(fixtureDir.resolve("frequencies.txt")))
        assertTrue(Files.notExists(fixtureDir.resolve("transfers.txt")))
        assertTrue(Files.notExists(fixtureDir.resolve("attributions.txt")))

        val parsed = parser.parseDirectory(fixtureDir)
        assertTrue(parsed.calendarDates.isNotEmpty())
    }

    @Test
    fun `domain mapper keeps StopPointId values from stop_id only`() {
        val mapped = mappedProfileFeed()
        val stopIds = mapped.stopPoints.map { it.id }.toSet()

        assertTrue(stopIds.contains(StopPointId("STOP_A")))
        assertTrue(stopIds.contains(StopPointId("STOP_B")))
        assertTrue(stopIds.contains(StopPointId("STOP_C")))
        assertTrue(stopIds.contains(StopPointId("STOP_D")))
        assertFalse(stopIds.contains(StopPointId("Jaam")))
        assertFalse(stopIds.contains(StopPointId("Polikliinik")))
    }

    @Test
    fun `loop pattern duplicate stop is preserved for TRIP_3`() {
        val mapped = mappedProfileFeed()
        val loopPattern = mapped.routePatterns.first { it.id == RoutePatternId("pattern:TRIP_3") }

        assertEquals(
            listOf(
                StopPointId("STOP_A"),
                StopPointId("STOP_B"),
                StopPointId("STOP_A"),
            ),
            loopPattern.orderedStopPointIds(),
        )
    }

    @Test
    fun `parser reads all four stops and stop_area filtering is not parser responsibility`() {
        val mapped = mappedProfileFeed()

        assertEquals(4, mapped.stopPoints.size)
    }

    @Test
    fun `calendar_dates exception_type 2 removes weekday service on 2026-01-01`() {
        val mapped = mappedProfileFeed()
        val resolver = ServiceCalendarResolver(
            calendars = mapped.serviceCalendars,
            exceptions = mapped.serviceCalendarExceptions,
        )

        assertFalse(
            resolver.isServiceActive(
                serviceId = ServiceId("Mock_1,2,3-Mo"),
                date = LocalDate.of(2026, 1, 1),
            ),
        )
    }

    @Test
    fun `calendar_dates exception_type 1 adds sunday service on 2026-01-01`() {
        val mapped = mappedProfileFeed()
        val resolver = ServiceCalendarResolver(
            calendars = mapped.serviceCalendars,
            exceptions = mapped.serviceCalendarExceptions,
        )

        assertTrue(
            resolver.isServiceActive(
                serviceId = ServiceId("Mock_1,2,3-Su"),
                date = LocalDate.of(2026, 1, 1),
            ),
        )
    }

    private fun parsedProfileFeed() = parser.parseDirectory(resourcePath("gtfs/rakvere-profile-smoke"))

    private fun mappedProfileFeed() = mapper.map(
        parsedFeed = parsedProfileFeed(),
        cityId = cityId,
        feedId = feedId,
    )

    private fun resourcePath(relativePath: String): Path {
        val resource = checkNotNull(javaClass.classLoader.getResource(relativePath)) {
            "Missing test resource: $relativePath"
        }
        return Path.of(resource.toURI())
    }
}
