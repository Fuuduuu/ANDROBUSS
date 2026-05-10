package ee.androbus.feature.search.resolution

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.StopPoint
import java.util.Locale

/**
 * PASS 14 accepts coordinateHint as future nearest-stop seed input, but this
 * in-memory resolver intentionally ignores it and resolves by stop-group name only.
 */
class InMemoryStopPointIndex(
    private val stopPoints: List<StopPoint>,
) : StopPointResolver {
    private val locale = Locale.forLanguageTag("et-EE")

    private val index: Map<IndexKey, List<StopPoint>> =
        stopPoints.groupBy { stopPoint ->
            IndexKey(
                cityId = stopPoint.cityId,
                normalizedName = normalizeName(stopPoint.displayName),
            )
        }

    override fun resolve(input: StopPointResolutionInput): StopPointResolutionResult {
        val trimmedInput = input.stopGroupName.trim()
        if (trimmedInput.isBlank()) {
            return StopPointResolutionResult.NotResolved.EmptyStopGroupName
        }

        if (index.isEmpty()) {
            return StopPointResolutionResult.NotResolved.NoIndexAvailable
        }

        val key =
            IndexKey(
                cityId = input.cityId,
                normalizedName = normalizeName(trimmedInput),
            )

        val matches = index[key] ?: return StopPointResolutionResult.NotResolved.NoStopGroupMatch

        val candidates =
            matches.map { stopPoint ->
                VerifiedStopPointCandidate(
                    stopPointId = stopPoint.id,
                    stopGroupId = stopPoint.stopGroupId,
                    displayName = stopPoint.displayName,
                    location = stopPoint.location,
                    confidence =
                        if (trimmedInput == stopPoint.displayName.trim()) {
                            StopPointResolutionConfidence.EXACT_NAME_MATCH
                        } else {
                            StopPointResolutionConfidence.NORMALIZED_NAME_MATCH
                        },
                    source = StopPointResolutionSource.GTFS_STOP_ID,
                )
            }

        return StopPointResolutionResult.Resolved(candidates)
    }

    private fun normalizeName(raw: String): String =
        raw
            .trim()
            .lowercase(locale)
            .replace(Regex("\\s+"), " ")

    private data class IndexKey(
        val cityId: CityId,
        val normalizedName: String,
    )
}

