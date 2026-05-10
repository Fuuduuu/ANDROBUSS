package ee.androbus.feature.search.orchestration

import ee.androbus.feature.search.resolution.StopCandidateEnrichmentResult

sealed interface DestinationEnrichmentResult {
    /**
     * At least one stop candidate was enriched.
     *
     * isAmbiguous is true when the total verified stop-point candidate count
     * across enriched candidates is greater than one.
     *
     * This result does not select a candidate.
     */
    data class Enriched(
        val enrichedCandidates: List<StopCandidateEnrichmentResult.Enriched>,
        val failedCandidates: List<StopCandidateEnrichmentResult.NotEnriched>,
        val isAmbiguous: Boolean,
    ) : DestinationEnrichmentResult

    /**
     * Candidates were provided, but none could be enriched.
     */
    data class NoneEnriched(
        val failedCandidates: List<StopCandidateEnrichmentResult.NotEnriched>,
    ) : DestinationEnrichmentResult

    /**
     * No destination candidates were supplied.
     */
    object NoCandidates : DestinationEnrichmentResult
}

