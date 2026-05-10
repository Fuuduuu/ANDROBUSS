package ee.androbus.feature.search.bridge

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.routing.DirectRouteNotFoundReason
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.core.routing.DirectRouteSearchResult
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.origin.OriginCandidate
import ee.androbus.feature.search.origin.OriginCandidateConfidence
import ee.androbus.feature.search.origin.OriginCandidateSource
import ee.androbus.feature.search.origin.OriginCoordinateConfidence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DirectRouteQueryBridgeTest {
    @Test
    fun `both unresolved returns BothUnresolved`() {
        val bridge = bridgeThatFailsIfCalled()

        val result =
            bridge.query(
                originCandidates = listOf(unresolvedOriginCandidate()),
                destinationCandidates = listOf(unresolvedDestinationCandidate()),
                patterns = listOf(validPattern()),
            )

        assertTrue(result === DirectRouteQueryBridgeResult.NotReady.BothUnresolved)
    }

    @Test
    fun `origin unresolved returns OriginUnresolved`() {
        val bridge = bridgeThatFailsIfCalled()

        val result =
            bridge.query(
                originCandidates = listOf(unresolvedOriginCandidate()),
                destinationCandidates = listOf(resolvedDestinationCandidate(StopPointId("destination-1"))),
                patterns = listOf(validPattern()),
            )

        assertTrue(result === DirectRouteQueryBridgeResult.NotReady.OriginUnresolved)
    }

    @Test
    fun `destination unresolved returns DestinationUnresolved`() {
        val bridge = bridgeThatFailsIfCalled()

        val result =
            bridge.query(
                originCandidates = listOf(resolvedOriginCandidate(StopPointId("origin-1"))),
                destinationCandidates = listOf(unresolvedDestinationCandidate()),
                patterns = listOf(validPattern()),
            )

        assertTrue(result === DirectRouteQueryBridgeResult.NotReady.DestinationUnresolved)
    }

    @Test
    fun `resolved candidates with empty patterns returns NoPatternsAvailable and does not call search`() {
        val bridge = bridgeThatFailsIfCalled()

        val result =
            bridge.query(
                originCandidates = listOf(resolvedOriginCandidate(StopPointId("origin-1"))),
                destinationCandidates = listOf(resolvedDestinationCandidate(StopPointId("destination-1"))),
                patterns = emptyList(),
            )

        assertTrue(result === DirectRouteQueryBridgeResult.NotReady.NoPatternsAvailable)
    }

    @Test
    fun `route found when explicit stop point ids and valid pattern are provided`() {
        val bridge = DirectRouteQueryBridge(DirectRouteSearch())

        val result =
            bridge.query(
                originCandidates = listOf(resolvedOriginCandidate(StopPointId("origin-1"))),
                destinationCandidates = listOf(resolvedDestinationCandidate(StopPointId("destination-1"))),
                patterns = listOf(validPattern()),
            )

        val found = assertIs<DirectRouteQueryBridgeResult.RouteFound>(result)
        assertEquals(1, found.result.candidates.size)
        val candidate = found.result.candidates.single()
        assertEquals(StopPointId("origin-1"), candidate.originStopPointId)
        assertEquals(StopPointId("destination-1"), candidate.destinationStopPointId)
    }

    @Test
    fun `route not found when destination is not after origin in shared pattern`() {
        val bridge = DirectRouteQueryBridge(DirectRouteSearch())

        val result =
            bridge.query(
                originCandidates = listOf(resolvedOriginCandidate(StopPointId("origin-1"))),
                destinationCandidates = listOf(resolvedDestinationCandidate(StopPointId("destination-1"))),
                patterns = listOf(reverseOnlyPattern()),
            )

        val notFound = assertIs<DirectRouteQueryBridgeResult.RouteNotFound>(result)
        assertEquals(DirectRouteNotFoundReason.DESTINATION_NOT_AFTER_ORIGIN, notFound.result.reason)
    }

    @Test
    fun `names and manual text do not fabricate stop point ids`() {
        val bridge = bridgeThatFailsIfCalled()

        val origin =
            unresolvedOriginCandidate().copy(
                displayName = "Rakvere bussijaam",
                notes = "Manual text origin only.",
            )
        val destination =
            unresolvedDestinationCandidate().copy(
                stopGroupName = "Rakvere bussijaam",
                notes = "Name-only destination candidate.",
            )

        val result =
            bridge.query(
                originCandidates = listOf(origin),
                destinationCandidates = listOf(destination),
                patterns = listOf(validPattern()),
            )

        assertTrue(result === DirectRouteQueryBridgeResult.NotReady.BothUnresolved)
    }

    @Test
    fun `multiple resolved ids use first origin and first destination deterministically`() {
        var capturedOrigin: StopPointId? = null
        var capturedDestination: StopPointId? = null

        val bridge =
            DirectRouteQueryBridge(
                routeSearch =
                    DirectRouteSearchPort { origin, destination, _ ->
                        capturedOrigin = origin
                        capturedDestination = destination
                        DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.NO_DIRECT_PATTERN)
                    },
            )

        val result =
            bridge.query(
                originCandidates =
                    listOf(
                        unresolvedOriginCandidate().copy(
                            stopPointIds =
                                listOf(
                                    StopPointId("origin-first"),
                                    StopPointId("origin-second"),
                                ),
                        ),
                        resolvedOriginCandidate(StopPointId("origin-third")),
                    ),
                destinationCandidates =
                    listOf(
                        unresolvedDestinationCandidate().copy(
                            stopPointIds =
                                listOf(
                                    StopPointId("destination-first"),
                                    StopPointId("destination-second"),
                                ),
                        ),
                    ),
                patterns = listOf(validPattern()),
            )

        assertIs<DirectRouteQueryBridgeResult.RouteNotFound>(result)
        assertEquals(StopPointId("origin-first"), capturedOrigin)
        assertEquals(StopPointId("destination-first"), capturedDestination)
    }

    @Test
    fun `bridge classes remain android free`() {
        listOf(
            DirectRouteQueryBridge::class.java,
            DirectRouteQueryBridgeResult::class.java,
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

    private fun bridgeThatFailsIfCalled(): DirectRouteQueryBridge =
        DirectRouteQueryBridge(
            routeSearch =
                DirectRouteSearchPort { _, _, _ ->
                    error("DirectRouteSearch must not be called when bridge preconditions fail.")
                },
        )

    private fun unresolvedOriginCandidate(): OriginCandidate =
        OriginCandidate(
            originId = "manual-origin",
            displayName = "Manual origin",
            source = OriginCandidateSource.MANUAL_TEXT,
            confidence = OriginCandidateConfidence.MANUAL_TEXT_UNRESOLVED,
            coordinate = null,
            coordinateConfidence = OriginCoordinateConfidence.MANUAL_OR_UNKNOWN,
            stopPointIds = emptyList(),
            stopGroupNames = emptyList(),
            notes = "Unresolved origin seed.",
        )

    private fun resolvedOriginCandidate(stopPointId: StopPointId): OriginCandidate =
        unresolvedOriginCandidate().copy(
            originId = "resolved-origin",
            source = OriginCandidateSource.SAVED_PLACE,
            confidence = OriginCandidateConfidence.EXPLICIT_METADATA,
            stopPointIds = listOf(stopPointId),
        )

    private fun unresolvedDestinationCandidate(): StopCandidate =
        StopCandidate(
            targetId = "target-1",
            stopGroupName = "Destination Group",
            source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "Name-level unresolved candidate.",
            stopPointIds = emptyList(),
        )

    private fun resolvedDestinationCandidate(stopPointId: StopPointId): StopCandidate =
        unresolvedDestinationCandidate().copy(stopPointIds = listOf(stopPointId))

    private fun validPattern(): RoutePattern =
        RoutePattern(
            id = RoutePatternId("pattern-1"),
            routeLineId = RouteLineId("line-1"),
            displayName = "Pattern 1",
            cityId = CityId("rakvere"),
            feedId = FeedId("rakvere"),
            stops =
                listOf(
                    PatternStop(sequence = 1, stopPointId = StopPointId("origin-1")),
                    PatternStop(sequence = 2, stopPointId = StopPointId("middle-1")),
                    PatternStop(sequence = 3, stopPointId = StopPointId("destination-1")),
                ),
        )

    private fun reverseOnlyPattern(): RoutePattern =
        RoutePattern(
            id = RoutePatternId("pattern-2"),
            routeLineId = RouteLineId("line-1"),
            displayName = "Pattern 2",
            cityId = CityId("rakvere"),
            feedId = FeedId("rakvere"),
            stops =
                listOf(
                    PatternStop(sequence = 1, stopPointId = StopPointId("destination-1")),
                    PatternStop(sequence = 2, stopPointId = StopPointId("middle-1")),
                    PatternStop(sequence = 3, stopPointId = StopPointId("origin-1")),
                ),
        )
}

