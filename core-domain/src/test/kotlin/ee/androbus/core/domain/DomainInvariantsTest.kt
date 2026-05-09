package ee.androbus.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DomainInvariantsTest {
    @Test
    fun `blank StopGroupId rejected`() {
        assertFailsWith<IllegalArgumentException> {
            StopGroupId(" ")
        }
    }

    @Test
    fun `blank StopPointId rejected`() {
        assertFailsWith<IllegalArgumentException> {
            StopPointId(" ")
        }
    }

    @Test
    fun `blank RouteLineId rejected`() {
        assertFailsWith<IllegalArgumentException> {
            RouteLineId(" ")
        }
    }

    @Test
    fun `blank RoutePatternId rejected`() {
        assertFailsWith<IllegalArgumentException> {
            RoutePatternId(" ")
        }
    }

    @Test
    fun `blank StopGroup display name rejected`() {
        assertFailsWith<IllegalArgumentException> {
            StopGroup(
                id = StopGroupId("sg-1"),
                displayName = " ",
                cityId = CityId("city-1"),
            )
        }
    }

    @Test
    fun `blank StopPoint display name rejected`() {
        assertFailsWith<IllegalArgumentException> {
            StopPoint(
                id = StopPointId("sp-1"),
                stopGroupId = StopGroupId("sg-1"),
                displayName = " ",
                location = GeoPoint(latitude = 59.35, longitude = 26.36),
                cityId = CityId("city-1"),
            )
        }
    }

    @Test
    fun `blank RouteLine display name rejected`() {
        assertFailsWith<IllegalArgumentException> {
            RouteLine(
                id = RouteLineId("line-1"),
                displayName = " ",
                cityId = CityId("city-1"),
            )
        }
    }

    @Test
    fun `blank RoutePattern display name rejected`() {
        assertFailsWith<IllegalArgumentException> {
            RoutePattern(
                id = RoutePatternId("pattern-1"),
                routeLineId = RouteLineId("line-1"),
                displayName = " ",
                cityId = CityId("city-1"),
                stops = listOf(
                    PatternStop(sequence = 1, stopPointId = StopPointId("sp-1")),
                    PatternStop(sequence = 2, stopPointId = StopPointId("sp-2")),
                ),
            )
        }
    }

    @Test
    fun `invalid GeoPoint latitude rejected`() {
        assertFailsWith<IllegalArgumentException> {
            GeoPoint(latitude = 90.0001, longitude = 25.0)
        }
    }

    @Test
    fun `invalid GeoPoint longitude rejected`() {
        assertFailsWith<IllegalArgumentException> {
            GeoPoint(latitude = 59.0, longitude = 180.0001)
        }
    }

    @Test
    fun `PatternStop sequence less than or equal to zero rejected`() {
        assertFailsWith<IllegalArgumentException> {
            PatternStop(sequence = 0, stopPointId = StopPointId("sp-1"))
        }
    }

    @Test
    fun `RoutePattern rejects fewer than two pattern stops`() {
        assertFailsWith<IllegalArgumentException> {
            RoutePattern(
                id = RoutePatternId("pattern-1"),
                routeLineId = RouteLineId("line-1"),
                displayName = "Main Pattern",
                cityId = CityId("city-1"),
                stops = listOf(
                    PatternStop(sequence = 1, stopPointId = StopPointId("sp-1")),
                ),
            )
        }
    }

    @Test
    fun `RoutePattern rejects non-increasing stop sequence`() {
        assertFailsWith<IllegalArgumentException> {
            RoutePattern(
                id = RoutePatternId("pattern-1"),
                routeLineId = RouteLineId("line-1"),
                displayName = "Main Pattern",
                cityId = CityId("city-1"),
                stops = listOf(
                    PatternStop(sequence = 1, stopPointId = StopPointId("sp-1")),
                    PatternStop(sequence = 1, stopPointId = StopPointId("sp-2")),
                ),
            )
        }
    }

    @Test
    fun `RoutePattern preserves ordered stops`() {
        val pattern = RoutePattern(
            id = RoutePatternId("pattern-1"),
            routeLineId = RouteLineId("line-1"),
            displayName = "Main Pattern",
            cityId = CityId("city-1"),
            stops = listOf(
                PatternStop(sequence = 10, stopPointId = StopPointId("sp-a")),
                PatternStop(sequence = 20, stopPointId = StopPointId("sp-b")),
                PatternStop(sequence = 30, stopPointId = StopPointId("sp-c")),
            ),
        )

        assertEquals(
            listOf(StopPointId("sp-a"), StopPointId("sp-b"), StopPointId("sp-c")),
            pattern.orderedStopPointIds(),
        )
    }

    @Test
    fun `RoutePattern allows duplicate StopPointIds for loop compatibility`() {
        val repeated = StopPointId("sp-loop")
        val pattern = RoutePattern(
            id = RoutePatternId("pattern-loop"),
            routeLineId = RouteLineId("line-loop"),
            displayName = "Loop Pattern",
            cityId = CityId("city-1"),
            stops = listOf(
                PatternStop(sequence = 1, stopPointId = StopPointId("sp-a")),
                PatternStop(sequence = 2, stopPointId = repeated),
                PatternStop(sequence = 3, stopPointId = StopPointId("sp-b")),
                PatternStop(sequence = 4, stopPointId = repeated),
            ),
        )

        assertEquals(
            listOf(
                StopPointId("sp-a"),
                repeated,
                StopPointId("sp-b"),
                repeated,
            ),
            pattern.orderedStopPointIds(),
        )
    }

    @Test
    fun `Trip rejects blank headsign when provided`() {
        assertFailsWith<IllegalArgumentException> {
            Trip(
                id = TripId("trip-1"),
                routePatternId = RoutePatternId("pattern-1"),
                service = ServiceRef(ServiceId("service-1")),
                headsign = " ",
            )
        }
    }

    @Test
    fun `DataConfidence contains STATIC FORECAST REALTIME`() {
        assertEquals(
            setOf(DataConfidence.STATIC, DataConfidence.FORECAST, DataConfidence.REALTIME),
            DataConfidence.entries.toSet(),
        )
    }
}
