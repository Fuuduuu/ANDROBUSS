package ee.androbus.feature.search.orchestration

import ee.androbus.feature.search.bridge.DirectRouteQueryBridgeResult
import ee.androbus.feature.search.resolution.StopCandidateEnrichmentResult

sealed interface DirectRouteQueryPreparationResult {
    /**
     * Preconditions passed and DirectRouteQueryBridge was called.
     */
    data class Executed(
        val bridgeResult: DirectRouteQueryBridgeResult,
    ) : DirectRouteQueryPreparationResult

    /**
     * Destination enrichment has more than one verified stop-point candidate.
     */
    data class DestinationAmbiguous(
        val enrichedCandidates: List<StopCandidateEnrichmentResult.Enriched>,
    ) : DirectRouteQueryPreparationResult

    /**
     * Destination enrichment had no usable verified stop-point candidate.
     */
    object DestinationUnresolved : DirectRouteQueryPreparationResult

    /**
     * No destination candidates existed before enrichment.
     */
    object NoCandidates : DirectRouteQueryPreparationResult

    /**
     * Caller did not provide explicit origin StopPointId.
     */
    object OriginNotProvided : DirectRouteQueryPreparationResult

    /**
     * Caller did not provide RoutePatterns.
     */
    object NoPatternsAvailable : DirectRouteQueryPreparationResult
}

