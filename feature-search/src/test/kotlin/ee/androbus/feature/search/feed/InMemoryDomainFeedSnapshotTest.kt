package ee.androbus.feature.search.feed

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.bridge.DirectRouteQueryBridgeResult
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.orchestration.DestinationEnrichmentResult
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationResult
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationUseCase
import ee.androbus.feature.search.resolution.InMemoryStopPointIndex
import ee.androbus.feature.search.resolution.StopCandidateEnrichmentResult
import ee.androbus.feature.search.resolution.StopPointResolutionConfidence
import ee.androbus.feature.search.resolution.StopPointResolutionInput
import ee.androbus.feature.search.resolution.StopPointResolutionResult
import ee.androbus.feature.search.resolution.StopPointResolutionSource
import ee.androbus.feature.search.resolution.VerifiedStopPointCandidate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryDomainFeedSnapshotTest {
    private val cityId = CityId("rakvere")
    private val otherCityId = CityId("parnu")

    private val rkvAOut =
        StopPoint(
            id = StopPointId("RKV_A_OUT"),
            displayName = "Keskpeatus",
            cityId = cityId,
            stopGroupId = StopGroupId("group:keskpeatus-out"),
            location = GeoPoint(59.3461, 26.3552),
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
                    PatternStop(stopPointId = StopPointId("RKV_C"), sequence = 2),
                ),
        )

    private val snapshot =
        DomainFeedSnapshot(
            cityId = cityId,
            stopPoints = listOf(rkvAOut, rkvC),
            routePatterns = listOf(patternT1),
        )

    @Test
    fun `getSnapshot returns snapshot for correct city`() {
        val provider = InMemoryDomainFeedSnapshot(snapshot)

        val result = provider.getSnapshot(cityId)

        assertNotNull(result)
        assertEquals(snapshot, result)
    }

    @Test
    fun `getSnapshot returns null for wrong city`() {
        val provider = InMemoryDomainFeedSnapshot(snapshot)

        val result = provider.getSnapshot(otherCityId)

        assertNull(result)
    }

    @Test
    fun `snapshot exposes stopPoints unchanged`() {
        val provider = InMemoryDomainFeedSnapshot(snapshot)
        val result = provider.getSnapshot(cityId)
        assertNotNull(result)

        assertEquals(snapshot.stopPoints, result.stopPoints)
        assertEquals(listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_C")), result.stopPoints.map { it.id })
    }

    @Test
    fun `snapshot exposes routePatterns unchanged`() {
        val provider = InMemoryDomainFeedSnapshot(snapshot)
        val result = provider.getSnapshot(cityId)
        assertNotNull(result)

        assertEquals(snapshot.routePatterns, result.routePatterns)
        assertEquals(listOf(RoutePatternId("pattern:T1")), result.routePatterns.map { it.id })
    }

    @Test
    fun `snapshot stopPoints can seed InMemoryStopPointIndex`() {
        val provider = InMemoryDomainFeedSnapshot(snapshot)
        val resolvedSnapshot = provider.getSnapshot(cityId)
        assertNotNull(resolvedSnapshot)

        val index = InMemoryStopPointIndex(resolvedSnapshot.stopPoints)
        val result = index.resolve(StopPointResolutionInput(stopGroupName = "Jaam", cityId = cityId))
        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)

        assertEquals(StopPointId("RKV_C"), resolved.candidates.single().stopPointId)
    }

    @Test
    fun `snapshot routePatterns can be supplied to DirectRouteQueryPreparationUseCase`() {
        val provider = InMemoryDomainFeedSnapshot(snapshot)
        val resolvedSnapshot = provider.getSnapshot(cityId)
        assertNotNull(resolvedSnapshot)

        val verifiedDestination =
            VerifiedStopPointCandidate(
                stopPointId = StopPointId("RKV_C"),
                stopGroupId = StopGroupId("group:jaam"),
                displayName = "Jaam",
                location = GeoPoint(59.3550, 26.3650),
                confidence = StopPointResolutionConfidence.EXACT_NAME_MATCH,
                source = StopPointResolutionSource.GTFS_STOP_ID,
            )
        val enrichedCandidate =
            StopCandidateEnrichmentResult.Enriched(
                enrichedCandidate =
                    StopCandidate(
                        targetId = "destination:jaam",
                        stopGroupName = "Jaam",
                        source = StopCandidateSource.MANUAL_METADATA,
                        confidence = StopCandidateConfidence.EXPLICIT_METADATA,
                        notes = "Test enriched candidate.",
                        stopPointIds = listOf(StopPointId("RKV_C")),
                    ),
                verifiedCandidates = listOf(verifiedDestination),
            )
        val destinationEnrichment =
            DestinationEnrichmentResult.Enriched(
                enrichedCandidates = listOf(enrichedCandidate),
                failedCandidates = emptyList(),
                isAmbiguous = false,
            )

        val useCase = DirectRouteQueryPreparationUseCase(DirectRouteQueryBridge(DirectRouteSearch()))
        val result =
            useCase.prepare(
                destinationEnrichment = destinationEnrichment,
                originStopPointId = StopPointId("RKV_A_OUT"),
                patterns = resolvedSnapshot.routePatterns,
            )

        val executed = assertIs<DirectRouteQueryPreparationResult.Executed>(result)
        assertIs<DirectRouteQueryBridgeResult.RouteFound>(executed.bridgeResult)
    }

    @Test
    fun `feed snapshot classes remain android free`() {
        listOf(
            DomainFeedSnapshot::class.java,
            DomainFeedSnapshotProvider::class.java,
            InMemoryDomainFeedSnapshot::class.java,
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
}
