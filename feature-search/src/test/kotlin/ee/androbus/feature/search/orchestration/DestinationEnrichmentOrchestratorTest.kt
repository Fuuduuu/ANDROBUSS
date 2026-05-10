package ee.androbus.feature.search.orchestration

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.resolution.InMemoryStopPointIndex
import ee.androbus.feature.search.resolution.StopCandidateEnricher
import ee.androbus.feature.search.resolution.StopCandidateEnrichmentResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DestinationEnrichmentOrchestratorTest {
    private val cityId = CityId("rakvere")

    private val rkvC =
        StopPoint(
            id = StopPointId("RKV_C"),
            displayName = "Jaam",
            cityId = cityId,
            stopGroupId = StopGroupId("group:jaam"),
            location = GeoPoint(59.3550, 26.3650),
            feedId = FeedId("rakvere-fixture"),
        )

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

    @Test
    fun `empty candidates return NoCandidates`() {
        val orchestrator = orchestratorWithStops(rkvC)

        val result = orchestrator.enrichCandidates(candidates = emptyList(), cityId = cityId)

        assertTrue(result === DestinationEnrichmentResult.NoCandidates)
    }

    @Test
    fun `all candidates fail returns NoneEnriched`() {
        val orchestrator = orchestratorWithStops(rkvC)
        val candidates = listOf(unresolvedCandidate("Unknown-1"), unresolvedCandidate("Unknown-2"))

        val result = orchestrator.enrichCandidates(candidates = candidates, cityId = cityId)

        val none = assertIs<DestinationEnrichmentResult.NoneEnriched>(result)
        assertEquals(2, none.failedCandidates.size)
    }

    @Test
    fun `exactly one verified candidate returns Enriched with isAmbiguous false`() {
        val orchestrator = orchestratorWithStops(rkvC)

        val result = orchestrator.enrichCandidates(candidates = listOf(unresolvedCandidate("Jaam")), cityId = cityId)

        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)
        assertFalse(enriched.isAmbiguous)
        assertEquals(1, enriched.enrichedCandidates.size)
        assertEquals(0, enriched.failedCandidates.size)
        assertEquals(listOf(StopPointId("RKV_C")), enriched.enrichedCandidates.single().enrichedCandidate.stopPointIds)
    }

    @Test
    fun `same name two stops returns Enriched with isAmbiguous true and preserves both candidates`() {
        val orchestrator = orchestratorWithStops(rkvAOut, rkvAIn)

        val result = orchestrator.enrichCandidates(candidates = listOf(unresolvedCandidate("Keskpeatus")), cityId = cityId)

        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)
        assertTrue(enriched.isAmbiguous)
        val ids = enriched.enrichedCandidates.single().verifiedCandidates.map { it.stopPointId }
        assertEquals(listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_A_IN")), ids)
    }

    @Test
    fun `mixed success and failure preserves failedCandidates`() {
        val orchestrator = orchestratorWithStops(rkvC)
        val candidates = listOf(unresolvedCandidate("Jaam"), unresolvedCandidate("Unknown"))

        val result = orchestrator.enrichCandidates(candidates = candidates, cityId = cityId)

        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)
        assertEquals(1, enriched.enrichedCandidates.size)
        assertEquals(1, enriched.failedCandidates.size)
        assertTrue(enriched.failedCandidates.single().reason is ee.androbus.feature.search.resolution.StopPointResolutionResult.NotResolved.NoStopGroupMatch)
    }

    @Test
    fun `isAmbiguous false when total verified count is exactly one`() {
        val orchestrator = orchestratorWithStops(rkvC)

        val result = orchestrator.enrichCandidates(candidates = listOf(unresolvedCandidate("Jaam")), cityId = cityId)

        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)
        val totalVerified = enriched.enrichedCandidates.sumOf { it.verifiedCandidates.size }
        assertEquals(1, totalVerified)
        assertFalse(enriched.isAmbiguous)
    }

    @Test
    fun `isAmbiguous true when total verified count is greater than one`() {
        val orchestrator = orchestratorWithStops(rkvC, rkvAOut, rkvAIn)
        val candidates = listOf(unresolvedCandidate("Jaam"), unresolvedCandidate("Keskpeatus"))

        val result = orchestrator.enrichCandidates(candidates = candidates, cityId = cityId)

        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)
        val totalVerified = enriched.enrichedCandidates.sumOf { it.verifiedCandidates.size }
        assertTrue(totalVerified > 1)
        assertTrue(enriched.isAmbiguous)
    }

    @Test
    fun `orchestrator has no DirectRouteQueryBridge dependency`() {
        val constructorParamTypes =
            DestinationEnrichmentOrchestrator::class.java.declaredConstructors
                .flatMap { it.parameterTypes.toList() }
        assertEquals(
            listOf(StopCandidateEnricher::class.java),
            constructorParamTypes,
            "DestinationEnrichmentOrchestrator must only depend on StopCandidateEnricher.",
        )

        val allReferencedTypes =
            buildList {
                addAll(DestinationEnrichmentOrchestrator::class.java.declaredFields.map { it.type })
                DestinationEnrichmentOrchestrator::class.java.declaredMethods.forEach { method ->
                    add(method.returnType)
                    addAll(method.parameterTypes)
                }
            }

        assertFalse(
            allReferencedTypes.any { it.name.contains("DirectRouteQueryBridge") },
            "DestinationEnrichmentOrchestrator must not reference DirectRouteQueryBridge.",
        )
    }

    @Test
    fun `anti fabrication keeps verified stop id only`() {
        val orchestrator = orchestratorWithStops(rkvC)

        val result = orchestrator.enrichCandidates(candidates = listOf(unresolvedCandidate("Jaam")), cityId = cityId)

        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)
        val resolvedId = enriched.enrichedCandidates.single().enrichedCandidate.stopPointIds.single()
        assertEquals(StopPointId("RKV_C"), resolvedId)
        assertNotEquals(StopPointId("Jaam"), resolvedId)
        assertNotEquals(StopPointId("jaam"), resolvedId)
    }

    @Test
    fun `multiple calls with same input are deterministic`() {
        val orchestrator = orchestratorWithStops(rkvAOut, rkvAIn)
        val candidates = listOf(unresolvedCandidate("Keskpeatus"))

        val first = orchestrator.enrichCandidates(candidates = candidates, cityId = cityId)
        val second = orchestrator.enrichCandidates(candidates = candidates, cityId = cityId)

        assertEquals(first, second)
    }

    @Test
    fun `orchestrator classes remain android free`() {
        listOf(
            DestinationEnrichmentOrchestrator::class.java,
            DestinationEnrichmentResult::class.java,
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
    fun `enriched results preserve verified candidate lists`() {
        val orchestrator = orchestratorWithStops(rkvAOut, rkvAIn)

        val result = orchestrator.enrichCandidates(candidates = listOf(unresolvedCandidate("Keskpeatus")), cityId = cityId)

        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)
        val candidate = enriched.enrichedCandidates.single()
        assertEquals(
            candidate.verifiedCandidates.map { it.stopPointId },
            candidate.enrichedCandidate.stopPointIds,
        )
        assertIs<StopCandidateEnrichmentResult.Enriched>(candidate)
    }

    private fun unresolvedCandidate(stopGroupName: String): StopCandidate =
        StopCandidate(
            targetId = "dest:$stopGroupName",
            stopGroupName = stopGroupName,
            source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "Name-level unresolved destination candidate.",
            stopPointIds = emptyList(),
        )

    private fun orchestratorWithStops(vararg stops: StopPoint): DestinationEnrichmentOrchestrator =
        DestinationEnrichmentOrchestrator(
            stopCandidateEnricher = StopCandidateEnricher(InMemoryStopPointIndex(stops.toList())),
        )
}

