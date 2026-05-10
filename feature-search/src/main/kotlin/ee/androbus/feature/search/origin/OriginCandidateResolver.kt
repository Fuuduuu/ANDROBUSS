package ee.androbus.feature.search.origin

import ee.androbus.core.domain.GeoPoint
import java.util.Locale

class OriginCandidateResolver {
    private val estonianLocale = Locale.forLanguageTag("et-EE")

    fun fromManualText(query: String): OriginCandidateResult {
        val normalizedDisplayName = collapseWhitespace(query.trim())
        if (normalizedDisplayName.isBlank()) {
            return OriginCandidateResult.NotFound(OriginCandidateNotFoundReason.BLANK_QUERY)
        }

        val candidate =
            OriginCandidate(
                originId = "manual:${normalizedDisplayName.lowercase(estonianLocale)}",
                displayName = normalizedDisplayName,
                source = OriginCandidateSource.MANUAL_TEXT,
                confidence = OriginCandidateConfidence.MANUAL_TEXT_UNRESOLVED,
                coordinate = null,
                coordinateConfidence = OriginCoordinateConfidence.MANUAL_OR_UNKNOWN,
                stopPointIds = emptyList(),
                stopGroupNames = emptyList(),
                notes = "Manual origin seed only; stop-point lookup is a future pass.",
            )

        return OriginCandidateResult.Found(candidates = listOf(candidate))
    }

    fun fromCurrentLocation(location: GeoPoint?): OriginCandidateResult {
        if (location == null) {
            return OriginCandidateResult.NotFound(OriginCandidateNotFoundReason.MISSING_LOCATION)
        }

        val candidate =
            OriginCandidate(
                originId = "current-location",
                displayName = "Current location",
                source = OriginCandidateSource.CURRENT_LOCATION,
                confidence = OriginCandidateConfidence.COORDINATE_ONLY_UNRESOLVED,
                coordinate = location,
                coordinateConfidence = OriginCoordinateConfidence.PROVIDED_BY_USER_LOCATION,
                stopPointIds = emptyList(),
                stopGroupNames = emptyList(),
                notes = "Coordinate seed only; nearest-stop resolution is a future pass.",
            )

        return OriginCandidateResult.Found(candidates = listOf(candidate))
    }

    private fun collapseWhitespace(value: String): String =
        value.replace(Regex("\\s+"), " ").trim()
}

