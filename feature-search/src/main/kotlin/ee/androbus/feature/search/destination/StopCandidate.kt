package ee.androbus.feature.search.destination

import ee.androbus.core.domain.StopPointId

enum class StopCandidateSource {
    CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
    MANUAL_METADATA,
    FUTURE_GEOSPATIAL,
    FUTURE_STOP_GROUP_LOOKUP,
}

enum class StopCandidateConfidence {
    EXPLICIT_METADATA,
    NAME_ONLY_UNRESOLVED,
    UNCLEAR,
}

data class StopCandidate(
    val targetId: String,
    val stopGroupName: String,
    val source: StopCandidateSource,
    val confidence: StopCandidateConfidence,
    val notes: String? = null,
    val stopPointIds: List<StopPointId> = emptyList(),
) {
    init {
        require(targetId.isNotBlank()) { "StopCandidate targetId cannot be blank." }
        require(stopGroupName.isNotBlank()) { "StopCandidate stopGroupName cannot be blank." }
        require(stopPointIds.isNotEmpty() || confidence != StopCandidateConfidence.UNCLEAR) {
            "StopCandidate with UNCLEAR confidence must be justified in a later pass."
        }
        if (notes != null) {
            require(notes.isNotBlank()) { "StopCandidate notes cannot be blank when provided." }
        }
    }
}
