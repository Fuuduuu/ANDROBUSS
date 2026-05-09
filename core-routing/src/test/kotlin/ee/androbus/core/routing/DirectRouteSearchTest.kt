package ee.androbus.core.routing

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DirectRouteSearchTest {
    private val search = DirectRouteSearch()

    @Test
    fun `finds direct route when origin appears before destination in same pattern`() {
        val pattern = pattern(
            id = "p-main",
            stopIds = listOf("A_OUT", "B", "C"),
        )

        val result = search.findDirectRoutes(
            origin = StopPointId("A_OUT"),
            destination = StopPointId("C"),
            patterns = listOf(pattern),
        )

        val found = assertIs<DirectRouteSearchResult.Found>(result)
        assertEquals(1, found.candidates.size)
        val candidate = found.candidates.single()
        assertEquals(RoutePatternId("p-main"), candidate.routePatternId)
        assertEquals(1, candidate.originSequence)
        assertEquals(3, candidate.destinationSequence)
        assertEquals(3, candidate.segmentStopCount)
        assertEquals(
            listOf(StopPointId("A_OUT"), StopPointId("B"), StopPointId("C")),
            candidate.segmentStopPointIds,
        )
    }

    @Test
    fun `reverse direction fails when destination appears before origin only`() {
        val pattern = pattern(
            id = "p-main",
            stopIds = listOf("A", "B", "C"),
        )

        val result = search.findDirectRoutes(
            origin = StopPointId("C"),
            destination = StopPointId("A"),
            patterns = listOf(pattern),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.DESTINATION_NOT_AFTER_ORIGIN, notFound.reason)
    }

    @Test
    fun `no shared pattern returns NO_DIRECT_PATTERN`() {
        val patternA = pattern(id = "p-a", stopIds = listOf("A", "B"))
        val patternB = pattern(id = "p-b", stopIds = listOf("C", "D"))

        val result = search.findDirectRoutes(
            origin = StopPointId("A"),
            destination = StopPointId("D"),
            patterns = listOf(patternA, patternB),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.NO_DIRECT_PATTERN, notFound.reason)
    }

    @Test
    fun `missing origin returns ORIGIN_NOT_FOUND`() {
        val pattern = pattern(id = "p-main", stopIds = listOf("A", "B", "C"))

        val result = search.findDirectRoutes(
            origin = StopPointId("X"),
            destination = StopPointId("C"),
            patterns = listOf(pattern),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.ORIGIN_NOT_FOUND, notFound.reason)
    }

    @Test
    fun `missing destination returns DESTINATION_NOT_FOUND`() {
        val pattern = pattern(id = "p-main", stopIds = listOf("A", "B", "C"))

        val result = search.findDirectRoutes(
            origin = StopPointId("A"),
            destination = StopPointId("X"),
            patterns = listOf(pattern),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.DESTINATION_NOT_FOUND, notFound.reason)
    }

    @Test
    fun `when both origin and destination are missing deterministic precedence returns ORIGIN_NOT_FOUND`() {
        val pattern = pattern(id = "p-main", stopIds = listOf("A", "B", "C"))

        val result = search.findDirectRoutes(
            origin = StopPointId("X"),
            destination = StopPointId("Y"),
            patterns = listOf(pattern),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.ORIGIN_NOT_FOUND, notFound.reason)
    }

    @Test
    fun `same origin destination returns SAME_STOP`() {
        val pattern = pattern(id = "p-main", stopIds = listOf("A", "B", "C"))

        val result = search.findDirectRoutes(
            origin = StopPointId("B"),
            destination = StopPointId("B"),
            patterns = listOf(pattern),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.SAME_STOP, notFound.reason)
    }

    @Test
    fun `preserves StopPointId identity and does not infer by shared human name`() {
        val outDirectionPattern = pattern(id = "p-out", stopIds = listOf("Keskpeatus_OUT", "B", "C"))
        val inDirectionPattern = pattern(id = "p-in", stopIds = listOf("Keskpeatus_IN", "D", "E"))

        val result = search.findDirectRoutes(
            origin = StopPointId("Keskpeatus_OUT"),
            destination = StopPointId("D"),
            patterns = listOf(outDirectionPattern, inDirectionPattern),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.NO_DIRECT_PATTERN, notFound.reason)
    }

    @Test
    fun `duplicate StopPointId loop pattern still finds valid later segment`() {
        val loopPattern = pattern(
            id = "p-loop",
            stopIds = listOf("A", "B", "A", "C"),
        )

        val result = search.findDirectRoutes(
            origin = StopPointId("A"),
            destination = StopPointId("C"),
            patterns = listOf(loopPattern),
        )

        val found = assertIs<DirectRouteSearchResult.Found>(result)
        val candidate = found.candidates.single()
        assertEquals(1, candidate.originSequence)
        assertEquals(4, candidate.destinationSequence)
        assertEquals(
            listOf(StopPointId("A"), StopPointId("B"), StopPointId("A"), StopPointId("C")),
            candidate.segmentStopPointIds,
        )
    }

    @Test
    fun `returns ordered segment stop point ids`() {
        val pattern = pattern(
            id = "p-segment",
            stopIds = listOf("S1", "S2", "S3", "S4"),
        )

        val result = search.findDirectRoutes(
            origin = StopPointId("S2"),
            destination = StopPointId("S4"),
            patterns = listOf(pattern),
        )

        val found = assertIs<DirectRouteSearchResult.Found>(result)
        assertEquals(
            listOf(StopPointId("S2"), StopPointId("S3"), StopPointId("S4")),
            found.candidates.single().segmentStopPointIds,
        )
    }

    @Test
    fun `does not use route pattern display names for routing identity`() {
        val pattern1 = pattern(id = "p-name-1", stopIds = listOf("A", "B"), displayName = "Shared Name")
        val pattern2 = pattern(id = "p-name-2", stopIds = listOf("C", "D"), displayName = "Shared Name")

        val result = search.findDirectRoutes(
            origin = StopPointId("A"),
            destination = StopPointId("D"),
            patterns = listOf(pattern1, pattern2),
        )

        val notFound = assertIs<DirectRouteSearchResult.NotFound>(result)
        assertEquals(DirectRouteNotFoundReason.NO_DIRECT_PATTERN, notFound.reason)
    }

    @Test
    fun `multiple matching patterns are returned deterministically`() {
        val patternB = pattern(id = "p-b", stopIds = listOf("A", "X", "C"))
        val patternA = pattern(id = "p-a", stopIds = listOf("A", "B", "C"))

        val result = search.findDirectRoutes(
            origin = StopPointId("A"),
            destination = StopPointId("C"),
            patterns = listOf(patternB, patternA),
        )

        val found = assertIs<DirectRouteSearchResult.Found>(result)
        assertEquals(listOf(RoutePatternId("p-a"), RoutePatternId("p-b")), found.candidates.map { it.routePatternId })
    }

    @Test
    fun `core routing remains Android free`() {
        listOf(
            DirectRouteSearch::class.java,
            DirectRouteCandidate::class.java,
            DirectRouteSearchResult.Found::class.java,
            DirectRouteSearchResult.NotFound::class.java,
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

    private fun pattern(
        id: String,
        stopIds: List<String>,
        displayName: String = "Pattern $id",
    ): RoutePattern =
        RoutePattern(
            id = RoutePatternId(id),
            routeLineId = RouteLineId("line-$id"),
            displayName = displayName,
            cityId = CityId("city-1"),
            stops = stopIds.mapIndexed { index, stopId ->
                PatternStop(sequence = index + 1, stopPointId = StopPointId(stopId))
            },
        )
}
