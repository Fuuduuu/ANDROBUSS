package ee.androbus.feature.search.integration

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.gtfs.GtfsDomainMapper
import ee.androbus.core.gtfs.GtfsFeedParser
import ee.androbus.core.gtfs.MappedGtfsFeed
import ee.androbus.core.routing.DirectRouteNotFoundReason
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.bridge.DirectRouteQueryBridgeResult
import ee.androbus.feature.search.bridge.DirectRouteSearchPort
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.orchestration.DestinationEnrichmentOrchestrator
import ee.androbus.feature.search.orchestration.DestinationEnrichmentResult
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationResult
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationUseCase
import ee.androbus.feature.search.resolution.InMemoryStopPointIndex
import ee.androbus.feature.search.resolution.StopCandidateEnricher
import ee.androbus.feature.search.resolution.StopCandidateEnrichmentResult
import ee.androbus.feature.search.resolution.StopPointResolutionInput
import ee.androbus.feature.search.resolution.StopPointResolutionResult
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GtfsFixtureSearchPipelineIntegrationTest {
    private val cityId = CityId("rakvere")
    private val feedId = FeedId("rakvere-smoke")

    @Test
    fun `parser and mapper produce expected stop points`() {
        val mapped = mappedFeed()
        val ids = mapped.stopPoints.map { it.id }.toSet()

        assertEquals(4, mapped.stopPoints.size)
        assertTrue(ids.contains(StopPointId("RKV_A_OUT")))
        assertTrue(ids.contains(StopPointId("RKV_A_IN")))
        assertTrue(ids.contains(StopPointId("RKV_B")))
        assertTrue(ids.contains(StopPointId("RKV_C")))
    }

    @Test
    fun `parser and mapper produce expected route patterns`() {
        val mapped = mappedFeed()

        assertEquals(3, mapped.routePatterns.size)
        val t1 = mapped.routePatterns.single { it.id == RoutePatternId("pattern:T1") }
        assertEquals(
            listOf(
                StopPointId("RKV_A_OUT"),
                StopPointId("RKV_B"),
                StopPointId("RKV_C"),
            ),
            t1.orderedStopPointIds(),
        )
    }

    @Test
    fun `in-memory index seeded from parser output resolves Jaam without id fabrication`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)

        val result = index.resolve(StopPointResolutionInput(stopGroupName = "Jaam", cityId = cityId))
        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)
        val stopPointId = resolved.candidates.single().stopPointId

        assertEquals(StopPointId("RKV_C"), stopPointId)
        assertNotEquals(StopPointId("Jaam"), stopPointId)
        assertNotEquals(StopPointId("jaam"), stopPointId)
    }

    @Test
    fun `AnniVibe lesson - same-name Keskpeatus resolves to two distinct stop ids`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)

        val result = index.resolve(StopPointResolutionInput(stopGroupName = "Keskpeatus", cityId = cityId))
        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)
        val ids = resolved.candidates.map { it.stopPointId }

        assertEquals(2, ids.size)
        assertTrue(ids.contains(StopPointId("RKV_A_OUT")))
        assertTrue(ids.contains(StopPointId("RKV_A_IN")))
    }

    @Test
    fun `stop candidate enricher with parser-seeded index enriches Jaam`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)
        val enricher = StopCandidateEnricher(index)

        val result = enricher.enrich(candidate("Jaam"), cityId)
        val enriched = assertIs<StopCandidateEnrichmentResult.Enriched>(result)

        assertEquals(listOf(StopPointId("RKV_C")), enriched.enrichedCandidate.stopPointIds)
    }

    @Test
    fun `destination orchestrator with parser data is non-ambiguous for Jaam`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)
        val enricher = StopCandidateEnricher(index)
        val orchestrator = DestinationEnrichmentOrchestrator(enricher)

        val result = orchestrator.enrichCandidates(candidates = listOf(candidate("Jaam")), cityId = cityId)
        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)

        assertFalse(enriched.isAmbiguous)
        assertEquals(1, enriched.enrichedCandidates.single().verifiedCandidates.size)
    }

    @Test
    fun `destination orchestrator with parser data is ambiguous for Keskpeatus`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)
        val enricher = StopCandidateEnricher(index)
        val orchestrator = DestinationEnrichmentOrchestrator(enricher)

        val result = orchestrator.enrichCandidates(candidates = listOf(candidate("Keskpeatus")), cityId = cityId)
        val enriched = assertIs<DestinationEnrichmentResult.Enriched>(result)

        assertTrue(enriched.isAmbiguous)
        assertEquals(2, enriched.enrichedCandidates.single().verifiedCandidates.size)
    }

    @Test
    fun `full parser-derived pipeline produces RouteFound`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)
        val enricher = StopCandidateEnricher(index)
        val orchestrator = DestinationEnrichmentOrchestrator(enricher)
        val destinationEnrichment = orchestrator.enrichCandidates(candidates = listOf(candidate("Jaam")), cityId = cityId)
        val useCase = DirectRouteQueryPreparationUseCase(DirectRouteQueryBridge(DirectRouteSearch()))

        val result = useCase.prepare(
            destinationEnrichment = destinationEnrichment,
            originStopPointId = StopPointId("RKV_A_OUT"),
            patterns = mapped.routePatterns,
        )

        val executed = assertIs<DirectRouteQueryPreparationResult.Executed>(result)
        assertIs<DirectRouteQueryBridgeResult.RouteFound>(executed.bridgeResult)
    }

    @Test
    fun `full parser-derived pipeline produces RouteNotFound for reverse direction`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)
        val enricher = StopCandidateEnricher(index)
        val orchestrator = DestinationEnrichmentOrchestrator(enricher)
        val destinationEnrichment = orchestrator.enrichCandidates(candidates = listOf(candidate("Jaam")), cityId = cityId)
        val useCase = DirectRouteQueryPreparationUseCase(DirectRouteQueryBridge(DirectRouteSearch()))

        val result = useCase.prepare(
            destinationEnrichment = destinationEnrichment,
            originStopPointId = StopPointId("RKV_A_IN"),
            patterns = mapped.routePatterns,
        )

        val executed = assertIs<DirectRouteQueryPreparationResult.Executed>(result)
        val notFound = assertIs<DirectRouteQueryBridgeResult.RouteNotFound>(executed.bridgeResult)
        assertEquals(DirectRouteNotFoundReason.DESTINATION_NOT_AFTER_ORIGIN, notFound.result.reason)
    }

    @Test
    fun `ambiguous destination blocks query preparation before bridge call`() {
        val mapped = mappedFeed()
        val index = InMemoryStopPointIndex(mapped.stopPoints)
        val enricher = StopCandidateEnricher(index)
        val orchestrator = DestinationEnrichmentOrchestrator(enricher)
        val ambiguous = orchestrator.enrichCandidates(candidates = listOf(candidate("Keskpeatus")), cityId = cityId)
        val throwingBridge =
            DirectRouteQueryBridge(
                DirectRouteSearchPort { _, _, _ ->
                    error("DirectRouteQueryBridge must not be called for ambiguous destination.")
                },
            )
        val useCase = DirectRouteQueryPreparationUseCase(throwingBridge)

        val result = useCase.prepare(
            destinationEnrichment = ambiguous,
            originStopPointId = StopPointId("RKV_A_OUT"),
            patterns = mapped.routePatterns,
        )

        assertIs<DirectRouteQueryPreparationResult.DestinationAmbiguous>(result)
    }

    @Test
    fun `loop pattern T3 is preserved by parser and mapper`() {
        val mapped = mappedFeed()

        val t3 = mapped.routePatterns.single { it.id == RoutePatternId("pattern:T3") }
        assertEquals(
            listOf(
                StopPointId("RKV_A_OUT"),
                StopPointId("RKV_B"),
                StopPointId("RKV_A_OUT"),
            ),
            t3.orderedStopPointIds(),
        )
    }

    private fun mappedFeed(): MappedGtfsFeed {
        val parser = GtfsFeedParser()
        val mapper = GtfsDomainMapper()
        val parsed = parser.parseDirectory(smokeFixtureDirectory())
        return mapper.map(
            parsedFeed = parsed,
            cityId = cityId,
            feedId = feedId,
        )
    }

    private fun smokeFixtureDirectory(): Path {
        val candidates =
            listOf(
                Path.of("core-gtfs", "src", "test", "resources", "gtfs", "rakvere-smoke"),
                Path.of("..", "core-gtfs", "src", "test", "resources", "gtfs", "rakvere-smoke"),
            )

        return candidates
            .map { it.toAbsolutePath().normalize() }
            .firstOrNull { Files.isDirectory(it) }
            ?: error("Unable to find core-gtfs rakvere-smoke fixture directory from ${Path.of("").toAbsolutePath()}.")
    }

    // PASS 20 intentionally uses synthetic smoke fixture names:
    // "Jaam", "Keskpeatus", "Spordikeskus".
    private fun candidate(stopGroupName: String): StopCandidate =
        StopCandidate(
            targetId = "fixture:$stopGroupName",
            stopGroupName = stopGroupName,
            source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "PASS 20 synthetic fixture candidate.",
            stopPointIds = emptyList(),
        )
}
