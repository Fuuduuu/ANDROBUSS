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
    /**
     * Reserved for future degraded-confidence mappings.
     *
     * PASS 11 intentionally uses EXPLICIT_METADATA for preferred-stop-group-name
     * candidates and keeps stopPointIds empty (name-level unresolved mapping).
     * UNCLEAR must not be used for those current metadata-only candidates.
     */
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
            "StopCandidate with UNCLEAR confidence must include explicit stopPointIds; name-only unresolved metadata candidates must not use UNCLEAR."
        }
        if (notes != null) {
            require(notes.isNotBlank()) { "StopCandidate notes cannot be blank when provided." }
        }
    }
}
