package ee.androbus.app.presentation.search

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

data class SearchUiState(
    val feedState: FeedState = FeedState.NotReady,
    val destinationInput: DestinationInputState = DestinationInputState.Empty,
    val originStopPointId: StopPointId? = null,
)
