package ee.androbus.feature.search.resolution

import ee.androbus.core.domain.CityId
import ee.androbus.feature.search.destination.StopCandidate

class StopCandidateEnricher(
    private val stopPointResolver: StopPointResolver,
) {
    fun enrich(
        candidate: StopCandidate,
        cityId: CityId,
    ): StopCandidateEnrichmentResult {
        val input =
            StopPointResolutionInput(
                stopGroupName = candidate.stopGroupName,
                cityId = cityId,
            )

        return when (val result = stopPointResolver.resolve(input)) {
            is StopPointResolutionResult.Resolved -> {
                // IDs must come only from verified stop-point candidates.
                val verifiedIds = result.candidates.map { it.stopPointId }
                StopCandidateEnrichmentResult.Enriched(
                    enrichedCandidate = candidate.copy(stopPointIds = verifiedIds),
                    verifiedCandidates = result.candidates,
                )
            }

            is StopPointResolutionResult.NotResolved ->
                StopCandidateEnrichmentResult.NotEnriched(
                    originalCandidate = candidate,
                    reason = result,
                )
        }
    }
}
