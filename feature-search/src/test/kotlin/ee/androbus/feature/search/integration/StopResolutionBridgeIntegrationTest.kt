package ee.androbus.feature.search.integration

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
import ee.androbus.core.routing.DirectRouteNotFoundReason
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.bridge.DirectRouteQueryBridgeResult
import ee.androbus.feature.search.bridge.DirectRouteSearchPort
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.origin.OriginCandidate
import ee.androbus.feature.search.origin.OriginCandidateConfidence
import ee.androbus.feature.search.origin.OriginCandidateSource
import ee.androbus.feature.search.origin.OriginCoordinateConfidence
import ee.androbus.feature.search.resolution.InMemoryStopPointIndex
import ee.androbus.feature.search.resolution.StopPointResolutionInput
import ee.androbus.feature.search.resolution.StopPointResolutionResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StopResolutionBridgeIntegrationTest {
    private val cityId = CityId("rakvere")

    private val rkvAOut =
        StopPoint(
            id = StopPointId("RKV_A_OUT"),
            displayName = "Keskpeatus",
            cityId = cityId,
            stopGroupId = StopGroupId("group:keskpeatus-out"),
            location = GeoPoint(59.3461, 26.3552),
            feedId = FeedId("rakvere-fixture"),
        )

    private val rkvAIn =
        StopPoint(
            id = StopPointId("RKV_A_IN"),
            displayName = "Keskpeatus",
            cityId = cityId,
            stopGroupId = StopGroupId("group:keskpeatus-in"),
            location = GeoPoint(59.3463, 26.3555),
            feedId = FeedId("rakvere-fixture"),
        )

    private val rkvB =
        StopPoint(
            id = StopPointId("RKV_B"),
            displayName = "Spordikeskus",
            cityId = cityId,
            stopGroupId = StopGroupId("group:spordikeskus"),
            location = GeoPoint(59.3500, 26.3600),
            feedId = FeedId("rakvere-fixture"),
        )

    private val rkvC =
        StopPoint(
            id = StopPointId("RKV_C"),
            displayName = "Jaam",
            cityId = cityId,
            stopGroupId = StopGroupId("group:jaam"),
            location = GeoPoint(59.3550, 26.3650),
            feedId = FeedId("rakvere-fixture"),
        )

    private val patternT1 =
        RoutePattern(
            id = RoutePatternId("pattern:T1"),
            routeLineId = RouteLineId("line:T1"),
            displayName = "T1",
            cityId = cityId,
            feedId = FeedId("rakvere-fixture"),
            stops =
                listOf(
                    PatternStop(stopPointId = StopPointId("RKV_A_OUT"), sequence = 1),
                    PatternStop(stopPointId = StopPointId("RKV_B"), sequence = 2),
                    PatternStop(stopPointId = StopPointId("RKV_C"), sequence = 3),
                ),
        )

    @Test
    fun `full pipeline from unresolved to RouteFound`() {
        val index = InMemoryStopPointIndex(stopPoints())
        val bridge = DirectRouteQueryBridge(DirectRouteSearch())

        var origin = unresolvedOriginCandidate("Keskpeatus")
        var destination = unresolvedDestinationCandidate("Jaam")

        val step0 = bridge.query(listOf(origin), listOf(destination), listOf(patternT1))
        assertTrue(step0 === DirectRouteQueryBridgeResult.NotReady.BothUnresolved)

        val resolvedDestinationIds = resolvedStopPointIds(index, "Jaam")
        assertEquals(listOf(StopPointId("RKV_C")), resolvedDestinationIds)
        destination = destination.copy(stopPointIds = resolvedDestinationIds)

        val step1 = bridge.query(listOf(origin), listOf(destination), listOf(patternT1))
        assertTrue(step1 === DirectRouteQueryBridgeResult.NotReady.OriginUnresolved)

        val resolvedOriginIds = resolvedStopPointIds(index, "Keskpeatus")
        assertEquals(listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_A_IN")), resolvedOriginIds)
        origin = origin.copy(stopPointIds = listOf(resolvedOriginIds.first()))

        val step2 = bridge.query(listOf(origin), listOf(destination), listOf(patternT1))
        val found = assertIs<DirectRouteQueryBridgeResult.RouteFound>(step2)
        val candidate = found.result.candidates.single()
        assertEquals(StopPointId("RKV_A_OUT"), candidate.originStopPointId)
        assertEquals(StopPointId("RKV_C"), candidate.destinationStopPointId)
    }

    @Test
    fun `same name resolves two stop points with different routing outcomes`() {
        val index = InMemoryStopPointIndex(stopPoints())
        val bridge = DirectRouteQueryBridge(DirectRouteSearch())

        val keskpeatusIds = resolvedStopPointIds(index, "Keskpeatus")
        assertEquals(listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_A_IN")), keskpeatusIds)

        val destination = unresolvedDestinationCandidate("Jaam").copy(stopPointIds = listOf(StopPointId("RKV_C")))

        val fromOut =
            unresolvedOriginCandidate("Keskpeatus").copy(
                stopPointIds = listOf(StopPointId("RKV_A_OUT")),
            )
        val fromOutResult = bridge.query(listOf(fromOut), listOf(destination), listOf(patternT1))
        assertIs<DirectRouteQueryBridgeResult.RouteFound>(fromOutResult)

        val fromIn =
            unresolvedOriginCandidate("Keskpeatus").copy(
                stopPointIds = listOf(StopPointId("RKV_A_IN")),
            )
        val fromInResult = bridge.query(listOf(fromIn), listOf(destination), listOf(patternT1))
        val notFound = assertIs<DirectRouteQueryBridgeResult.RouteNotFound>(fromInResult)
        assertEquals(DirectRouteNotFoundReason.ORIGIN_NOT_FOUND, notFound.result.reason)
    }

    @Test
    fun `reverse direction returns RouteNotFound`() {
        val bridge = DirectRouteQueryBridge(DirectRouteSearch())

        val origin = unresolvedOriginCandidate("Jaam").copy(stopPointIds = listOf(StopPointId("RKV_C")))
        val destination = unresolvedDestinationCandidate("Keskpeatus").copy(stopPointIds = listOf(StopPointId("RKV_A_OUT")))

        val result = bridge.query(listOf(origin), listOf(destination), listOf(patternT1))
        val notFound = assertIs<DirectRouteQueryBridgeResult.RouteNotFound>(result)
        assertEquals(DirectRouteNotFoundReason.DESTINATION_NOT_AFTER_ORIGIN, notFound.result.reason)
    }

    @Test
    fun `no patterns available returns NotReady`() {
        val bridge = DirectRouteQueryBridge(DirectRouteSearch())

        val origin = unresolvedOriginCandidate("Keskpeatus").copy(stopPointIds = listOf(StopPointId("RKV_A_OUT")))
        val destination = unresolvedDestinationCandidate("Jaam").copy(stopPointIds = listOf(StopPointId("RKV_C")))

        val result = bridge.query(listOf(origin), listOf(destination), patterns = emptyList())
        assertTrue(result === DirectRouteQueryBridgeResult.NotReady.NoPatternsAvailable)
    }

    @Test
    fun `anti fabrication guard keeps GTFS stop ids only`() {
        val index = InMemoryStopPointIndex(stopPoints())

        val jaamIds = resolvedStopPointIds(index, "Jaam")
        val resolvedId = jaamIds.single()
        assertEquals(StopPointId("RKV_C"), resolvedId)
        assertNotEquals(StopPointId("Jaam"), resolvedId)
        assertNotEquals(StopPointId("jaam"), resolvedId)

        val unknown =
            index.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Totally Unknown Stop",
                    cityId = cityId,
                ),
            )
        assertTrue(unknown === StopPointResolutionResult.NotResolved.NoStopGroupMatch)
    }

    @Test
    fun `unresolved candidates do not call direct route search`() {
        val bridge =
            DirectRouteQueryBridge(
                routeSearch =
                    DirectRouteSearchPort { _, _, _ ->
                        error("DirectRouteSearch must not be called for unresolved candidate preconditions.")
                    },
            )

        val unresolvedOrigin = unresolvedOriginCandidate("Keskpeatus")
        val unresolvedDestination = unresolvedDestinationCandidate("Jaam")

        val bothUnresolved =
            bridge.query(
                originCandidates = listOf(unresolvedOrigin),
                destinationCandidates = listOf(unresolvedDestination),
                patterns = listOf(patternT1),
            )
        assertTrue(bothUnresolved === DirectRouteQueryBridgeResult.NotReady.BothUnresolved)

        val destinationResolved = unresolvedDestination.copy(stopPointIds = listOf(StopPointId("RKV_C")))
        val originUnresolved =
            bridge.query(
                originCandidates = listOf(unresolvedOrigin),
                destinationCandidates = listOf(destinationResolved),
                patterns = listOf(patternT1),
            )
        assertTrue(originUnresolved === DirectRouteQueryBridgeResult.NotReady.OriginUnresolved)
    }

    private fun stopPoints(): List<StopPoint> = listOf(rkvAOut, rkvAIn, rkvB, rkvC)

    private fun unresolvedOriginCandidate(displayName: String): OriginCandidate =
        OriginCandidate(
            originId = "origin:$displayName",
            displayName = displayName,
            source = OriginCandidateSource.MANUAL_TEXT,
            confidence = OriginCandidateConfidence.MANUAL_TEXT_UNRESOLVED,
            coordinate = null,
            coordinateConfidence = OriginCoordinateConfidence.MANUAL_OR_UNKNOWN,
            stopPointIds = emptyList(),
            stopGroupNames = emptyList(),
            notes = "Name-level unresolved origin candidate for integration test.",
        )

    private fun unresolvedDestinationCandidate(stopGroupName: String): StopCandidate =
        StopCandidate(
            targetId = "dest:$stopGroupName",
            stopGroupName = stopGroupName,
            source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "Name-level unresolved destination candidate for integration test.",
            stopPointIds = emptyList(),
        )

    private fun resolvedStopPointIds(
        index: InMemoryStopPointIndex,
        stopGroupName: String,
    ): List<StopPointId> {
        val resolved =
            index.resolve(
                StopPointResolutionInput(
                    stopGroupName = stopGroupName,
                    cityId = cityId,
                ),
            )
        return (resolved as? StopPointResolutionResult.Resolved)
            ?.candidates
            ?.map { it.stopPointId }
            .orEmpty()
    }
}

