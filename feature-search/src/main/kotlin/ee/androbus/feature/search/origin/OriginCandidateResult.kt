package ee.androbus.feature.search.origin

enum class OriginCandidateNotFoundReason {
    BLANK_QUERY,
    MISSING_LOCATION,
}

sealed interface OriginCandidateResult {
    data class Found(
        val candidates: List<OriginCandidate>,
    ) : OriginCandidateResult

    data class NotFound(
        val reason: OriginCandidateNotFoundReason,
    ) : OriginCandidateResult
}

