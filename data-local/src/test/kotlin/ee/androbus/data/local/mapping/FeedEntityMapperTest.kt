package ee.androbus.data.local.mapping

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FeedEntityMapperTest {
    private val cityId = CityId("rakvere")
    private val feedId = FeedId("rakvere-smoke")

    @Test
    fun `StopPoint round-trip keeps scoped key and identity`() {
        val stopPoint =
            StopPoint(
                id = StopPointId("RKV_C"),
                stopGroupId = StopGroupId("group:jaam"),
                displayName = "Jaam",
                location = GeoPoint(59.3550, 26.3650),
                cityId = cityId,
                feedId = feedId,
                platformCode = "1",
            )

        val entity = FeedEntityMapper.toStopPointEntity(stopPoint, cityId = cityId, feedId = feedId)
        val roundTrip = FeedEntityMapper.toStopPoint(entity)

        assertEquals("rakvere", entity.cityId)
        assertEquals("rakvere-smoke", entity.feedId)
        assertEquals("RKV_C", entity.stopId)
        assertEquals(stopPoint.id.value, entity.stopId)
        assertEquals("Jaam", roundTrip.displayName)
        assertEquals(stopPoint, roundTrip)
    }

    @Test
    fun `same displayName with distinct stop ids remains separate`() {
        val outEntity =
            FeedEntityMapper.toStopPointEntity(
                stopPoint =
                    StopPoint(
                        id = StopPointId("RKV_A_OUT"),
                        stopGroupId = StopGroupId("group:keskpeatus-out"),
                        displayName = "Keskpeatus",
                        location = GeoPoint(59.3461, 26.3552),
                        cityId = cityId,
                        feedId = feedId,
                    ),
                cityId = cityId,
                feedId = feedId,
            )
        val inEntity =
            FeedEntityMapper.toStopPointEntity(
                stopPoint =
                    StopPoint(
                        id = StopPointId("RKV_A_IN"),
                        stopGroupId = StopGroupId("group:keskpeatus-in"),
                        displayName = "Keskpeatus",
                        location = GeoPoint(59.3463, 26.3555),
                        cityId = cityId,
                        feedId = feedId,
                    ),
                cityId = cityId,
                feedId = feedId,
            )

        assertNotEquals(outEntity.stopId, inEntity.stopId)
        assertEquals("Keskpeatus", outEntity.displayName)
        assertEquals("Keskpeatus", inEntity.displayName)

        val mappedOut = FeedEntityMapper.toStopPoint(outEntity)
        val mappedIn = FeedEntityMapper.toStopPoint(inEntity)
        assertNotEquals(mappedOut.id, mappedIn.id)
    }

    @Test
    fun `RoutePattern round-trip keeps scoped key order and duplicate stop ids`() {
        val routePattern =
            RoutePattern(
                id = RoutePatternId("pattern:T3"),
                routeLineId = RouteLineId("line:T3"),
                displayName = "T3 loop",
                cityId = cityId,
                feedId = feedId,
                stops =
                    listOf(
                        PatternStop(sequence = 1, stopPointId = StopPointId("RKV_A_OUT")),
                        PatternStop(sequence = 2, stopPointId = StopPointId("RKV_B")),
                        PatternStop(sequence = 3, stopPointId = StopPointId("RKV_A_OUT")),
                    ),
            )

        val entity = FeedEntityMapper.toRoutePatternEntity(routePattern, cityId = cityId, feedId = feedId)
        val stopEntities = FeedEntityMapper.toPatternStopEntities(routePattern, cityId = cityId, feedId = feedId)
        val roundTrip = FeedEntityMapper.toRoutePattern(entity, stopEntities)

        assertEquals("rakvere", entity.cityId)
        assertEquals("rakvere-smoke", entity.feedId)
        assertEquals("pattern:T3", entity.patternId)
        assertEquals(routePattern.id.value, entity.patternId)
        assertEquals(listOf(1, 2, 3), stopEntities.map { it.sequence })
        assertEquals(listOf("RKV_A_OUT", "RKV_B", "RKV_A_OUT"), stopEntities.map { it.stopId })
        assertEquals(routePattern, roundTrip)
    }

    @Test
    fun `StopPointId is never derived from displayName`() {
        val stopPoint =
            StopPoint(
                id = StopPointId("RKV_C"),
                stopGroupId = StopGroupId("group:jaam"),
                displayName = "Jaam",
                location = GeoPoint(59.3550, 26.3650),
                cityId = cityId,
                feedId = feedId,
            )
        val entity = FeedEntityMapper.toStopPointEntity(stopPoint, cityId = cityId, feedId = feedId)
        val roundTrip = FeedEntityMapper.toStopPoint(entity)

        assertEquals(StopPointId("RKV_C"), roundTrip.id)
        assertNotEquals(StopPointId("Jaam"), roundTrip.id)
        assertNotEquals(StopPointId("jaam"), roundTrip.id)
    }
}
