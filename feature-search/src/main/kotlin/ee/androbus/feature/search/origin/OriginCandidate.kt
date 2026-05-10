package ee.androbus.feature.search.origin

import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.StopPointId

enum class OriginCandidateSource {
    MANUAL_TEXT,
    CURRENT_LOCATION,
    SAVED_PLACE,
    FUTURE_STOP_POINT_LOOKUP,
    FUTURE_GEOSPATIAL,
}

enum class OriginCandidateConfidence {
    MANUAL_TEXT_UNRESOLVED,
    COORDINATE_ONLY_UNRESOLVED,
    EXPLICIT_METADATA,
    UNCLEAR,
}

enum class OriginCoordinateConfidence {
    PROVIDED_BY_USER_LOCATION,
    MANUAL_OR_UNKNOWN,
    UNKNOWN,
}

data class OriginCandidate(
    val originId: String,
    val displayName: String,
    val source: OriginCandidateSource,
    val confidence: OriginCandidateConfidence,
    val coordinate: GeoPoint? = null,
    val coordinateConfidence: OriginCoordinateConfidence = OriginCoordinateConfidence.UNKNOWN,
    val stopPointIds: List<StopPointId> = emptyList(),
    val stopGroupNames: List<String> = emptyList(),
    val notes: String? = null,
) {
    init {
        require(originId.isNotBlank()) { "OriginCandidate originId cannot be blank." }
        require(displayName.isNotBlank()) { "OriginCandidate displayName cannot be blank." }
        require(stopGroupNames.all { it.isNotBlank() }) {
            "OriginCandidate stopGroupNames cannot contain blank values."
        }
        require(coordinate != null || coordinateConfidence != OriginCoordinateConfidence.PROVIDED_BY_USER_LOCATION) {
            "OriginCandidate coordinateConfidence cannot be PROVIDED_BY_USER_LOCATION when coordinate is null."
        }
        if (notes != null) {
            require(notes.isNotBlank()) { "OriginCandidate notes cannot be blank when provided." }
        }
    }
}

