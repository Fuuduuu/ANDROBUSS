package ee.androbus.core.routing

import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId

data class DirectRouteCandidate(
    val routePatternId: RoutePatternId,
    val originStopPointId: StopPointId,
    val destinationStopPointId: StopPointId,
    val originSequence: Int,
    val destinationSequence: Int,
    val segmentStopCount: Int,
    val segmentStopPointIds: List<StopPointId>,
)

enum class DirectRouteNotFoundReason {
    ORIGIN_NOT_FOUND,
    DESTINATION_NOT_FOUND,
    SAME_STOP,
    NO_DIRECT_PATTERN,
    DESTINATION_NOT_AFTER_ORIGIN,
}

sealed interface DirectRouteSearchResult {
    data class Found(
        val candidates: List<DirectRouteCandidate>,
    ) : DirectRouteSearchResult

    data class NotFound(
        val reason: DirectRouteNotFoundReason,
    ) : DirectRouteSearchResult
}
