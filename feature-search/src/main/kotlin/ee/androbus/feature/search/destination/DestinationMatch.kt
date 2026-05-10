package ee.androbus.feature.search.destination

data class DestinationMatch(
    val target: DestinationTarget,
    val confidence: DestinationTargetConfidence,
    val matchedAlias: String? = null,
)

enum class DestinationNotFoundReason {
    BLANK_QUERY,
    NO_CITY_PLACES,
    NO_MATCH,
}

sealed interface DestinationResolutionResult {
    data class Found(
        val matches: List<DestinationMatch>,
    ) : DestinationResolutionResult

    data class NotFound(
        val reason: DestinationNotFoundReason,
    ) : DestinationResolutionResult
}
