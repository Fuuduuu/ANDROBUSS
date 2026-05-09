package ee.androbus.core.routing

import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.StopPointId

class DirectRouteSearch {
    fun findDirectRoutes(
        origin: StopPointId,
        destination: StopPointId,
        patterns: List<RoutePattern>,
    ): DirectRouteSearchResult {
        if (origin == destination) {
            return DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.SAME_STOP)
        }

        val originExists = patterns.any { pattern ->
            pattern.stops.any { it.stopPointId == origin }
        }
        if (!originExists) {
            return DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.ORIGIN_NOT_FOUND)
        }

        val destinationExists = patterns.any { pattern ->
            pattern.stops.any { it.stopPointId == destination }
        }
        if (!destinationExists) {
            return DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.DESTINATION_NOT_FOUND)
        }

        val candidates = mutableListOf<DirectRouteCandidate>()
        var sharedPatternFound = false

        patterns.forEach { pattern ->
            val originIndices = pattern.stops.indices.filter { index ->
                pattern.stops[index].stopPointId == origin
            }
            val destinationIndices = pattern.stops.indices.filter { index ->
                pattern.stops[index].stopPointId == destination
            }

            if (originIndices.isEmpty() || destinationIndices.isEmpty()) {
                return@forEach
            }
            sharedPatternFound = true

            val chosenPair = selectEarliestValidPair(originIndices, destinationIndices) ?: return@forEach
            val originIndex = chosenPair.first
            val destinationIndex = chosenPair.second

            val originStop = pattern.stops[originIndex]
            val destinationStop = pattern.stops[destinationIndex]
            val segmentStops = pattern.stops.subList(originIndex, destinationIndex + 1)

            candidates += DirectRouteCandidate(
                routePatternId = pattern.id,
                originStopPointId = originStop.stopPointId,
                destinationStopPointId = destinationStop.stopPointId,
                originSequence = originStop.sequence,
                destinationSequence = destinationStop.sequence,
                segmentStopCount = segmentStops.size,
                segmentStopPointIds = segmentStops.map { it.stopPointId },
            )
        }

        if (candidates.isNotEmpty()) {
            return DirectRouteSearchResult.Found(
                candidates.sortedWith(
                    compareBy<DirectRouteCandidate>(
                        { it.routePatternId.value },
                        { it.originSequence },
                        { it.destinationSequence },
                    ),
                ),
            )
        }

        if (sharedPatternFound) {
            return DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.DESTINATION_NOT_AFTER_ORIGIN)
        }

        return DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.NO_DIRECT_PATTERN)
    }

    private fun selectEarliestValidPair(
        originIndices: List<Int>,
        destinationIndices: List<Int>,
    ): Pair<Int, Int>? {
        val sortedDestinations = destinationIndices.sorted()
        originIndices.sorted().forEach { originIndex ->
            val destinationIndex = sortedDestinations.firstOrNull { it > originIndex } ?: return@forEach
            return originIndex to destinationIndex
        }
        return null
    }
}
