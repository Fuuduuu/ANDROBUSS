package ee.androbus.feature.search.resolution

sealed interface StopPointResolutionResult {
    data class Resolved(
        val candidates: List<VerifiedStopPointCandidate>,
    ) : StopPointResolutionResult

    sealed interface NotResolved : StopPointResolutionResult {
        object EmptyStopGroupName : NotResolved

        object NoStopGroupMatch : NotResolved

        object NoIndexAvailable : NotResolved
    }
}

