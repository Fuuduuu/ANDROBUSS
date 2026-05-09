package ee.androbus.core.gtfs

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.ServiceExceptionType
import ee.androbus.core.domain.ServiceId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.domain.TripId
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GtfsDomainMapperTest {
    private val parser = GtfsFeedParser()
    private val mapper = GtfsDomainMapper()
    private val cityId = CityId("rakvere")
    private val feedId = FeedId("rakvere-smoke")

    @Test
    fun `calendar_dates exception type 1 maps to ADD_SERVICE`() {
        val mapped = mapFixture()

        val exception = mapped.serviceCalendarExceptions.first {
            it.serviceId == ServiceId("WKD") && it.date == LocalDate.of(2026, 5, 3)
        }
        assertEquals(ServiceExceptionType.ADD_SERVICE, exception.exceptionType)
    }

    @Test
    fun `calendar_dates exception type 2 maps to REMOVE_SERVICE`() {
        val mapped = mapFixture()

        val exception = mapped.serviceCalendarExceptions.first {
            it.serviceId == ServiceId("WKD") && it.date == LocalDate.of(2026, 5, 5)
        }
        assertEquals(ServiceExceptionType.REMOVE_SERVICE, exception.exceptionType)
    }

    @Test
    fun `invalid exception type fails clearly`() {
        val parsed = parser.parseDirectory(resourcePath("gtfs/rakvere-smoke"))
        val invalid = parsed.copy(
            calendarDates = parsed.calendarDates + GtfsCalendarDate(
                serviceId = "WKD",
                date = LocalDate.of(2026, 5, 29),
                exceptionType = 9,
            ),
        )

        val error = assertFailsWith<GtfsParseException> {
            mapper.map(invalid, cityId, feedId)
        }

        assertTrue(error.message.orEmpty().contains("exception_type"))
    }

    @Test
    fun `same-name stops with different stop_id remain separate StopPoints`() {
        val mapped = mapFixture()

        val sameNamePoints = mapped.stopPoints.filter { it.displayName == "Keskpeatus" }
        assertEquals(2, sameNamePoints.size)
        assertNotEquals(sameNamePoints[0].id, sameNamePoints[1].id)
    }

    @Test
    fun `mapper does not group by stop name`() {
        val mapped = mapFixture()

        val sameNameGroups = mapped.stopGroups.filter { it.displayName == "Keskpeatus" }
        assertEquals(2, sameNameGroups.size)
        assertTrue(sameNameGroups.any { it.id == StopGroupId("group:RKV_A_OUT") })
        assertTrue(sameNameGroups.any { it.id == StopGroupId("group:RKV_A_IN") })
    }

    @Test
    fun `mapper creates RoutePattern ordered by stop sequence`() {
        val mapped = mapFixture()

        val pattern = mapped.routePatterns.first { it.id == RoutePatternId("pattern:T2") }
        assertEquals(
            listOf(
                StopPointId("RKV_C"),
                StopPointId("RKV_B"),
                StopPointId("RKV_A_IN"),
            ),
            pattern.orderedStopPointIds(),
        )
    }

    @Test
    fun `mapper creates Trip with ServiceRef ServiceId`() {
        val mapped = mapFixture()

        val trip = mapped.trips.first { it.id == TripId("T1") }
        assertEquals(ServiceId("WKD"), trip.service.id)
    }

    @Test
    fun `parser and mapper public APIs remain Android-free`() {
        listOf(
            CsvTableReader::class.java,
            GtfsFeedParser::class.java,
            GtfsDomainMapper::class.java,
        ).forEach { clazz ->
            clazz.declaredConstructors.forEach { constructor ->
                constructor.parameterTypes.forEach { parameterType ->
                    assertFalse(
                        parameterType.name.startsWith("android."),
                        "Android type ${parameterType.name} found in ${clazz.name} constructor.",
                    )
                }
            }
            clazz.declaredMethods.forEach { method ->
                assertFalse(
                    method.returnType.name.startsWith("android."),
                    "Android return type ${method.returnType.name} found in ${clazz.name}.${method.name}.",
                )
                method.parameterTypes.forEach { parameterType ->
                    assertFalse(
                        parameterType.name.startsWith("android."),
                        "Android parameter type ${parameterType.name} found in ${clazz.name}.${method.name}.",
                    )
                }
            }
        }
    }

    private fun mapFixture() =
        mapper.map(
            parsedFeed = parser.parseDirectory(resourcePath("gtfs/rakvere-smoke")),
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
