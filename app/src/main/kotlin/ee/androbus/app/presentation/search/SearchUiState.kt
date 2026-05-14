package ee.androbus.app.presentation.search

import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId

sealed interface FeedState {
    data object NotReady : FeedState

    data object Ready : FeedState

    data class Error(
        val message: String,
    ) : FeedState
}

sealed interface DestinationInputState {
    data object Empty : DestinationInputState

    data class Typed(
        val text: String,
    ) : DestinationInputState

    data object Resolving : DestinationInputState

    data class Resolved(
        val displayName: String,
        val candidates: List<ResolvedDestinationOption>,
    ) : DestinationInputState

    data class Ambiguous(
        val options: List<ResolvedDestinationOption>,
    ) : DestinationInputState

    data object NotFound : DestinationInputState
}

data class ResolvedDestinationOption(
    val stopGroupName: String,
    val stopPointId: StopPointId,
)

enum class RouteNotFoundDisplayReason {
    ORIGIN_NOT_FOUND,
    DESTINATION_NOT_FOUND,
    SAME_STOP,
    NO_DIRECT_PATTERN,
    DESTINATION_NOT_AFTER_ORIGIN,
}

data class RouteFoundSummary(
    val routePatternId: RoutePatternId,
    val originStopPointId: StopPointId,
    val destinationStopPointId: StopPointId,
    val originSequence: Int,
    val destinationSequence: Int,
    val segmentStopCount: Int,
    val segmentStopPointIds: List<StopPointId>,
    val candidateCount: Int,
)

sealed interface RouteQueryState {
    data object Idle : RouteQueryState

    data object Searching : RouteQueryState

    data object FeedNotAvailable : RouteQueryState

    data object DestinationNotReady : RouteQueryState

    data object OriginNotProvided : RouteQueryState

    data object NoPatternsAvailable : RouteQueryState

    data class RouteFound(
        val route: RouteFoundSummary,
    ) : RouteQueryState

    data class RouteNotFound(
        val reason: RouteNotFoundDisplayReason,
    ) : RouteQueryState

    data class Error(
        val message: String,
    ) : RouteQueryState
}

data class SearchUiState(
    val feedState: FeedState = FeedState.NotReady,
    val destinationInput: DestinationInputState = DestinationInputState.Empty,
    val originStopPointId: StopPointId? = null,
    val routeQueryState: RouteQueryState = RouteQueryState.Idle,
)
