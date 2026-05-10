package ee.androbus.feature.search.orchestration

import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.StopPointId
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.origin.OriginCandidate
import ee.androbus.feature.search.origin.OriginCandidateConfidence
import ee.androbus.feature.search.origin.OriginCandidateSource
import ee.androbus.feature.search.origin.OriginCoordinateConfidence

class DirectRouteQueryPreparationUseCase(
    private val bridge: DirectRouteQueryBridge,
) {
    /**
     * Prepares and executes direct-route query only when preconditions are safe.
     *
     * Caller supplies already-computed destination enrichment, explicit origin ID,
     * and route patterns.
     */
    fun prepare(
        destinationEnrichment: DestinationEnrichmentResult,
        originStopPointId: StopPointId?,
        patterns: List<RoutePattern>,
    ): DirectRouteQueryPreparationResult {
        return when (destinationEnrichment) {
            DestinationEnrichmentResult.NoCandidates -> DirectRouteQueryPreparationResult.NoCandidates

            is DestinationEnrichmentResult.NoneEnriched -> DirectRouteQueryPreparationResult.DestinationUnresolved

            is DestinationEnrichmentResult.Enriched -> {
                if (destinationEnrichment.isAmbiguous) {
                    return DirectRouteQueryPreparationResult.DestinationAmbiguous(destinationEnrichment.enrichedCandidates)
                }
                if (originStopPointId == null) {
                    return DirectRouteQueryPreparationResult.OriginNotProvided
                }
                if (patterns.isEmpty()) {
                    return DirectRouteQueryPreparationResult.NoPatternsAvailable
                }

                val verifiedDestinationCandidates =
                    destinationEnrichment.enrichedCandidates
                        .flatMap { it.verifiedCandidates }

                // Non-ambiguous enrichment must provide exactly one destination candidate.
                val destinationStopPointId = verifiedDestinationCandidates.single().stopPointId

                val bridgeResult =
                    bridge.query(
                        originCandidates = listOf(minimalOriginCandidate(originStopPointId)),
                        destinationCandidates = listOf(minimalDestinationCandidate(destinationStopPointId)),
                        patterns = patterns,
                    )

                DirectRouteQueryPreparationResult.Executed(bridgeResult)
            }
        }
    }

    private fun minimalOriginCandidate(stopPointId: StopPointId): OriginCandidate =
        OriginCandidate(
            originId = "origin:${stopPointId.value}",
            displayName = "resolved-origin",
            source = OriginCandidateSource.SAVED_PLACE,
            confidence = OriginCandidateConfidence.EXPLICIT_METADATA,
            coordinate = null,
            coordinateConfidence = OriginCoordinateConfidence.UNKNOWN,
            stopPointIds = listOf(stopPointId),
            stopGroupNames = emptyList(),
            notes = "Bridge-preparation wrapper with explicit origin StopPointId.",
        )

    private fun minimalDestinationCandidate(stopPointId: StopPointId): StopCandidate =
        StopCandidate(
            targetId = "destination:${stopPointId.value}",
            stopGroupName = "__resolved_destination__",
            source = StopCandidateSource.MANUAL_METADATA,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "Bridge-preparation wrapper with explicit destination StopPointId.",
            stopPointIds = listOf(stopPointId),
        )
}

