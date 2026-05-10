package ee.androbus.feature.search.destination

enum class PlaceToStopCandidateNotFoundReason {
    NO_PREFERRED_STOP_GROUPS,
    UNSUPPORTED_TARGET_SOURCE,
}

sealed interface PlaceToStopCandidateResult {
    data class Found(
        val candidates: List<StopCandidate>,
    ) : PlaceToStopCandidateResult

    data class NotFound(
        val reason: PlaceToStopCandidateNotFoundReason,
    ) : PlaceToStopCandidateResult
}
