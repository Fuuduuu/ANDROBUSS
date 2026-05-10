package ee.androbus.feature.search.orchestration

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.routing.DirectRouteNotFoundReason
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.core.routing.DirectRouteSearchResult
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.bridge.DirectRouteQueryBridgeResult
import ee.androbus.feature.search.bridge.DirectRouteSearchPort
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.resolution.StopCandidateEnrichmentResult
import ee.androbus.feature.search.resolution.StopPointResolutionConfidence
import ee.androbus.feature.search.resolution.StopPointResolutionResult
import ee.androbus.feature.search.resolution.StopPointResolutionSource
import ee.androbus.feature.search.resolution.VerifiedStopPointCandidate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class DirectRouteQueryPreparationUseCaseTest {
    private val cityId = CityId("rakvere")
    private val feedId = FeedId("rakvere-fixture")
    private val originId = StopPointId("RKV_A_OUT")
    private val destinationId = StopPointId("RKV_C")
    private val midId = StopPointId("RKV_B")
    private val reverseOrigin = StopPointId("RKV_C")
    private val reverseDestination = StopPointId("RKV_A_OUT")

    private val patternT1 =
        RoutePattern(
            id = RoutePatternId("pattern:T1"),
            routeLineId = RouteLineId("line:1"),
            displayName = "T1",
            cityId = cityId,
            stops =
                listOf(
                    PatternStop(sequence = 1, stopPointId = originId),
                    PatternStop(sequence = 2, stopPointId = midId),
                    PatternStop(sequence = 3, stopPointId = destinationId),
                ),
            feedId = feedId,
        )

    private val noRoutePattern =
        RoutePattern(
            id = RoutePatternId("pattern:NO_ROUTE"),
            routeLineId = RouteLineId("line:2"),
            displayName = "NoRoute",
            cityId = cityId,
            stops =
                listOf(
                    PatternStop(sequence = 1, stopPointId = StopPointId("X1")),
                    PatternStop(sequence = 2, stopPointId = StopPointId("X2")),
                ),
            feedId = feedId,
        )

    @Test
    fun `NoCandidates input returns NoCandidates and does not call bridge`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val result = useCase.prepare(
            destinationEnrichment = DestinationEnrichmentResult.NoCandidates,
            originStopPointId = originId,
            patterns = listOf(patternT1),
        )

        assertTrue(result === DirectRouteQueryPreparationResult.NoCandidates)
        assertEquals(0, probe.callCount)
    }

    @Test
    fun `NoneEnriched input returns DestinationUnresolved and does not call bridge`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val failedCandidate =
            StopCandidateEnrichmentResult.NotEnriched(
                originalCandidate = unresolvedCandidate("Unknown"),
                reason = StopPointResolutionResult.NotResolved.NoStopGroupMatch,
            )

        val result = useCase.prepare(
            destinationEnrichment = DestinationEnrichmentResult.NoneEnriched(failedCandidates = listOf(failedCandidate)),
            originStopPointId = originId,
            patterns = listOf(patternT1),
        )

        assertTrue(result === DirectRouteQueryPreparationResult.DestinationUnresolved)
        assertEquals(0, probe.callCount)
    }

    @Test
    fun `ambiguous destination returns DestinationAmbiguous and does not call bridge`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)
        val ambiguousEnrichment =
            DestinationEnrichmentResult.Enriched(
                enrichedCandidates =
                    listOf(
                        enrichedCandidate(
                            verifiedCandidates =
                                listOf(
                                    verified(destinationId, "Jaam"),
                                    verified(StopPointId("RKV_C_ALT"), "Jaam"),
                                ),
                        ),
                    ),
                failedCandidates = emptyList(),
                isAmbiguous = true,
            )

        val result = useCase.prepare(
            destinationEnrichment = ambiguousEnrichment,
            originStopPointId = originId,
            patterns = listOf(patternT1),
        )

        val ambiguous = assertIs<DirectRouteQueryPreparationResult.DestinationAmbiguous>(result)
        assertEquals(ambiguousEnrichment.enrichedCandidates, ambiguous.enrichedCandidates)
        assertEquals(0, probe.callCount)
    }

    @Test
    fun `origin null returns OriginNotProvided and does not call bridge`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val result = useCase.prepare(
            destinationEnrichment = nonAmbiguousEnrichment(destinationId),
            originStopPointId = null,
            patterns = listOf(patternT1),
        )

        assertTrue(result === DirectRouteQueryPreparationResult.OriginNotProvided)
        assertEquals(0, probe.callCount)
    }

    @Test
    fun `no patterns returns NoPatternsAvailable and does not call bridge`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val result = useCase.prepare(
            destinationEnrichment = nonAmbiguousEnrichment(destinationId),
            originStopPointId = originId,
            patterns = emptyList(),
        )

        assertTrue(result === DirectRouteQueryPreparationResult.NoPatternsAvailable)
        assertEquals(0, probe.callCount)
    }

    @Test
    fun `safe preconditions execute bridge and return RouteFound`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val result = useCase.prepare(
            destinationEnrichment = nonAmbiguousEnrichment(destinationId),
            originStopPointId = originId,
            patterns = listOf(patternT1),
        )

        val executed = assertIs<DirectRouteQueryPreparationResult.Executed>(result)
        val found = assertIs<DirectRouteQueryBridgeResult.RouteFound>(executed.bridgeResult)
        assertEquals(1, probe.callCount)
        assertEquals(originId, found.result.candidates.single().originStopPointId)
        assertEquals(destinationId, found.result.candidates.single().destinationStopPointId)
        assertEquals(destinationId, probe.lastCall!!.destination)
    }

    @Test
    fun `safe preconditions can execute bridge and return RouteNotFound`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val result = useCase.prepare(
            destinationEnrichment = nonAmbiguousEnrichment(destinationId),
            originStopPointId = originId,
            patterns = listOf(noRoutePattern),
        )

        val executed = assertIs<DirectRouteQueryPreparationResult.Executed>(result)
        val notFound = assertIs<DirectRouteQueryBridgeResult.RouteNotFound>(executed.bridgeResult)
        assertEquals(DirectRouteNotFoundReason.ORIGIN_NOT_FOUND, notFound.result.reason)
        assertEquals(1, probe.callCount)
    }

    @Test
    fun `reverse direction executes bridge and returns DESTINATION_NOT_AFTER_ORIGIN`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val result = useCase.prepare(
            destinationEnrichment = nonAmbiguousEnrichment(reverseDestination),
            originStopPointId = reverseOrigin,
            patterns = listOf(patternT1),
        )

        val executed = assertIs<DirectRouteQueryPreparationResult.Executed>(result)
        val notFound = assertIs<DirectRouteQueryBridgeResult.RouteNotFound>(executed.bridgeResult)
        assertEquals(DirectRouteNotFoundReason.DESTINATION_NOT_AFTER_ORIGIN, notFound.result.reason)
        assertEquals(1, probe.callCount)
    }

    @Test
    fun `exact-one guard fails loudly when non-ambiguous enrichment contains multiple verified candidates`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)
        val malformed =
            DestinationEnrichmentResult.Enriched(
                enrichedCandidates =
                    listOf(
                        enrichedCandidate(
                            verifiedCandidates =
                                listOf(
                                    verified(destinationId, "Jaam"),
                                    verified(StopPointId("RKV_C_ALT"), "Jaam"),
                                ),
                        ),
                    ),
                failedCandidates = emptyList(),
                isAmbiguous = false,
            )

        assertFailsWith<IllegalArgumentException> {
            useCase.prepare(
                destinationEnrichment = malformed,
                originStopPointId = originId,
                patterns = listOf(patternT1),
            )
        }
        assertEquals(0, probe.callCount)
    }

    @Test
    fun `anti-fabrication destination id comes from verified candidate only`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)

        val result = useCase.prepare(
            destinationEnrichment = nonAmbiguousEnrichment(destinationId),
            originStopPointId = originId,
            patterns = listOf(patternT1),
        )

        assertIs<DirectRouteQueryPreparationResult.Executed>(result)
        val routedDestination = probe.lastCall!!.destination
        assertEquals(StopPointId("RKV_C"), routedDestination)
        assertNotEquals(StopPointId("Jaam"), routedDestination)
        assertNotEquals(StopPointId("jaam"), routedDestination)
    }

    @Test
    fun `use-case and result classes remain android free`() {
        listOf(
            DirectRouteQueryPreparationUseCase::class.java,
            DirectRouteQueryPreparationResult::class.java,
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

    @Test
    fun `deterministic repeated calls with same input return same result type and payload`() {
        val probe = BridgeProbe()
        val useCase = DirectRouteQueryPreparationUseCase(probe.bridge)
        val input = nonAmbiguousEnrichment(destinationId)

        val first = useCase.prepare(input, originId, listOf(patternT1))
        val second = useCase.prepare(input, originId, listOf(patternT1))

        assertEquals(first, second)
        assertEquals(2, probe.callCount)
    }

    private fun nonAmbiguousEnrichment(destination: StopPointId): DestinationEnrichmentResult.Enriched =
        DestinationEnrichmentResult.Enriched(
            enrichedCandidates = listOf(enrichedCandidate(verifiedCandidates = listOf(verified(destination, "Jaam")))),
            failedCandidates = emptyList(),
            isAmbiguous = false,
        )

    private fun enrichedCandidate(
        verifiedCandidates: List<VerifiedStopPointCandidate>,
    ): StopCandidateEnrichmentResult.Enriched =
        StopCandidateEnrichmentResult.Enriched(
            enrichedCandidate =
                StopCandidate(
                    targetId = "destination-candidate",
                    stopGroupName = "Jaam",
                    source = StopCandidateSource.MANUAL_METADATA,
                    confidence = StopCandidateConfidence.EXPLICIT_METADATA,
                    notes = "Prepared enriched candidate for route-query preparation test.",
                    stopPointIds = verifiedCandidates.map { it.stopPointId },
                ),
            verifiedCandidates = verifiedCandidates,
        )

    private fun verified(
        stopPointId: StopPointId,
        displayName: String,
    ): VerifiedStopPointCandidate =
        VerifiedStopPointCandidate(
            stopPointId = stopPointId,
            stopGroupId = StopGroupId("group:${stopPointId.value}"),
            displayName = displayName,
            location = GeoPoint(59.3550, 26.3650),
            confidence = StopPointResolutionConfidence.EXACT_NAME_MATCH,
            source = StopPointResolutionSource.GTFS_STOP_ID,
        )

    private fun unresolvedCandidate(stopGroupName: String): StopCandidate =
        StopCandidate(
            targetId = "dest:$stopGroupName",
            stopGroupName = stopGroupName,
            source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "Unresolved destination candidate for test.",
            stopPointIds = emptyList(),
        )

    private class BridgeProbe {
        var callCount: Int = 0
        var lastCall: BridgeCall? = null

        val bridge =
            DirectRouteQueryBridge(
                DirectRouteSearchPort { origin, destination, patterns ->
                    callCount += 1
                    lastCall = BridgeCall(origin = origin, destination = destination, patterns = patterns)
                    DirectRouteSearch().findDirectRoutes(origin, destination, patterns)
                },
            )
    }

    private data class BridgeCall(
        val origin: StopPointId,
        val destination: StopPointId,
        val patterns: List<RoutePattern>,
    )
}

