package ee.androbus.feature.search.resolution

import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPointId

enum class StopPointResolutionConfidence {
    EXACT_NAME_MATCH,
    NORMALIZED_NAME_MATCH,
    FUTURE_COORDINATE_NEAREST,
}

enum class StopPointResolutionSource {
    GTFS_STOP_ID,
    FUTURE_GEOSPATIAL,
}

data class VerifiedStopPointCandidate(
    val stopPointId: StopPointId,
    val stopGroupId: StopGroupId,
    val displayName: String,
    val location: GeoPoint,
    val confidence: StopPointResolutionConfidence,
    val source: StopPointResolutionSource,
) {
    init {
        require(displayName.isNotBlank()) { "VerifiedStopPointCandidate displayName cannot be blank." }
    }
}

