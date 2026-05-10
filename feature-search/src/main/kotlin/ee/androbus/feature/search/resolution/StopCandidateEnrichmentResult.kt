package ee.androbus.feature.search.resolution

import ee.androbus.feature.search.destination.StopCandidate

sealed interface StopCandidateEnrichmentResult {
    /**
     * Resolution succeeded.
     *
     * enrichedCandidate is the original StopCandidate with stopPointIds
     * populated from verifiedCandidates.
     *
     * verifiedCandidates are preserved for future ranking/detail/map/UI use.
     */
    data class Enriched(
        val enrichedCandidate: StopCandidate,
        val verifiedCandidates: List<VerifiedStopPointCandidate>,
    ) : StopCandidateEnrichmentResult

    /**
     * Resolution failed.
     *
     * originalCandidate is preserved unchanged so callers can explain/fallback/log.
     */
    data class NotEnriched(
        val originalCandidate: StopCandidate,
        val reason: StopPointResolutionResult.NotResolved,
    ) : StopCandidateEnrichmentResult
}
