package ee.androbus.feature.search.destination

class PlaceToStopCandidateResolver {
    fun resolveCandidates(target: DestinationTarget): PlaceToStopCandidateResult {
        if (target.source != DestinationTargetSource.CITY_PLACE_METADATA) {
            return PlaceToStopCandidateResult.NotFound(PlaceToStopCandidateNotFoundReason.UNSUPPORTED_TARGET_SOURCE)
        }

        if (target.preferredStopGroupNames.isEmpty()) {
            return PlaceToStopCandidateResult.NotFound(PlaceToStopCandidateNotFoundReason.NO_PREFERRED_STOP_GROUPS)
        }

        val candidates =
            target.preferredStopGroupNames.map { stopGroupName ->
                StopCandidate(
                    targetId = target.id,
                    stopGroupName = stopGroupName,
                    source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
                    confidence = StopCandidateConfidence.EXPLICIT_METADATA,
                    notes = "Name-only stop-group candidate from metadata; stop-point mapping unresolved.",
                    stopPointIds = emptyList(),
                )
            }

        return PlaceToStopCandidateResult.Found(candidates)
    }
}
