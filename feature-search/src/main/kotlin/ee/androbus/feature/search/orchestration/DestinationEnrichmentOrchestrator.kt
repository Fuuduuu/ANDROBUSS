package ee.androbus.feature.search.orchestration

import ee.androbus.core.domain.CityId
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.resolution.StopCandidateEnricher
import ee.androbus.feature.search.resolution.StopCandidateEnrichmentResult

class DestinationEnrichmentOrchestrator(
    private val stopCandidateEnricher: StopCandidateEnricher,
) {
    /**
     * Enriches destination-side stop candidates only.
     *
     * Does not call DirectRouteQueryBridge.
     * Does not select a single verified stop-point candidate.
     * Does not fabricate StopPointId values from names or coordinates.
     */
    fun enrichCandidates(
        candidates: List<StopCandidate>,
        cityId: CityId,
    ): DestinationEnrichmentResult {
        if (candidates.isEmpty()) {
            return DestinationEnrichmentResult.NoCandidates
        }

        val results = candidates.map { stopCandidateEnricher.enrich(candidate = it, cityId = cityId) }
        val enriched = results.filterIsInstance<StopCandidateEnrichmentResult.Enriched>()
        val failed = results.filterIsInstance<StopCandidateEnrichmentResult.NotEnriched>()

        if (enriched.isEmpty()) {
            return DestinationEnrichmentResult.NoneEnriched(failedCandidates = failed)
        }

        val totalVerified = enriched.sumOf { it.verifiedCandidates.size }
        return DestinationEnrichmentResult.Enriched(
            enrichedCandidates = enriched,
            failedCandidates = failed,
            isAmbiguous = totalVerified > 1,
        )
    }
}

