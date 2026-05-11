package ee.androbus.app.bootstrap

import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BootstrapFeedDtoTest {
    @Test
    fun `dto toDomainFeedSnapshot maps cityId and counts`() {
        val dto = sampleDto()

        val snapshot = dto.toDomainFeedSnapshot()

        assertEquals("rakvere", snapshot.cityId.value)
        assertEquals(4, snapshot.stopPoints.size)
        assertEquals(2, snapshot.routePatterns.size)
    }

    @Test
    fun `StopPointId comes from id field not displayName`() {
        val dto =
            sampleDto().copy(
                stopPoints =
                    sampleDto().stopPoints.map {
                        if (it.id == "RKV_C") it.copy(displayName = "Jaam") else it
                    },
            )

        val snapshot = dto.toDomainFeedSnapshot()
        val stop = snapshot.stopPoints.single { it.displayName == "Jaam" && it.id.value == "RKV_C" }

        assertEquals(StopPointId("RKV_C"), stop.id)
        assertNotEquals(StopPointId("Jaam"), stop.id)
    }

    @Test
    fun `RoutePattern stop order is preserved`() {
        val dto = sampleDto()

        val snapshot = dto.toDomainFeedSnapshot()
        val pattern = snapshot.routePatterns.single { it.id == RoutePatternId("pattern:T1") }

        assertEquals(
            listOf("RKV_A_OUT", "RKV_B", "RKV_C"),
            pattern.orderedStopPointIds().map { it.value },
        )
    }

    @Test
    fun `duplicate stop ids in route pattern are preserved`() {
        val dto = sampleDto()

        val snapshot = dto.toDomainFeedSnapshot()
        val pattern = snapshot.routePatterns.single { it.id == RoutePatternId("pattern:T3") }

        assertEquals(
            listOf("RKV_A_OUT", "RKV_B", "RKV_A_OUT"),
            pattern.orderedStopPointIds().map { it.value },
        )
    }

    private fun sampleDto(): BootstrapFeedDto =
        BootstrapFeedDto(
            cityId = "rakvere",
            feedId = "rakvere-bootstrap-v1",
            stopPoints =
                listOf(
                    StopPointDto("RKV_A_OUT", "group:keskpeatus-out", "Keskpeatus", 59.3461, 26.3552),
                    StopPointDto("RKV_A_IN", "group:keskpeatus-in", "Keskpeatus", 59.3463, 26.3555),
                    StopPointDto("RKV_B", "group:spordikeskus", "Spordikeskus", 59.35, 26.36),
                    StopPointDto("RKV_C", "group:jaam", "Jaam", 59.355, 26.365),
                ),
            routePatterns =
                listOf(
                    RoutePatternDto(
                        id = "pattern:T1",
                        routeLineId = "line:T1",
                        displayName = "T1",
                        stopIds = listOf("RKV_A_OUT", "RKV_B", "RKV_C"),
                    ),
                    RoutePatternDto(
                        id = "pattern:T3",
                        routeLineId = "line:T3",
                        displayName = "T3",
                        stopIds = listOf("RKV_A_OUT", "RKV_B", "RKV_A_OUT"),
                    ),
                ),
        )
}
