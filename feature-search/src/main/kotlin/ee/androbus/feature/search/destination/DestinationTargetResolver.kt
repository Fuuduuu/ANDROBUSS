package ee.androbus.feature.search.destination

import ee.androbus.cityadapters.metadata.CityAdapterMetadata

class DestinationTargetResolver(
    private val queryNormalizer: DestinationQueryNormalizer = DestinationQueryNormalizer(),
) {
    fun resolvePlaceQuery(
        city: CityAdapterMetadata,
        query: String,
    ): DestinationResolutionResult {
        val strictQuery = queryNormalizer.normalizeForStrictMatch(query)
        if (strictQuery.isBlank()) {
            return DestinationResolutionResult.NotFound(DestinationNotFoundReason.BLANK_QUERY)
        }

        if (city.places.isEmpty()) {
            return DestinationResolutionResult.NotFound(DestinationNotFoundReason.NO_CITY_PLACES)
        }

        val flexibleQuery = queryNormalizer.normalizeForFlexibleMatch(query)

        val matches =
            city.places.mapNotNull { place ->
                bestMatchForPlace(
                    place = place,
                    strictQuery = strictQuery,
                    flexibleQuery = flexibleQuery,
                    city = city,
                )
            }

        if (matches.isEmpty()) {
            return DestinationResolutionResult.NotFound(DestinationNotFoundReason.NO_MATCH)
        }

        return DestinationResolutionResult.Found(
            matches =
                matches.sortedWith(
                    compareBy<DestinationMatch>(
                        { rankOf(it.confidence) },
                        { queryNormalizer.normalizeForFlexibleMatch(it.target.displayName) },
                    ),
                ),
        )
    }

    private fun bestMatchForPlace(
        place: ee.androbus.cityadapters.metadata.CityPlaceMetadata,
        strictQuery: String,
        flexibleQuery: String,
        city: CityAdapterMetadata,
    ): DestinationMatch? {
        val matchCandidates = listOf(place.displayName) + place.aliases

        var best: Candidate? = null
        matchCandidates.forEach { alias ->
            val strictAlias = queryNormalizer.normalizeForStrictMatch(alias)
            val flexibleAlias = queryNormalizer.normalizeForFlexibleMatch(alias)

            val confidence =
                when {
                    strictAlias == strictQuery -> DestinationTargetConfidence.EXACT_ALIAS
                    flexibleAlias == flexibleQuery -> DestinationTargetConfidence.NORMALIZED_ALIAS
                    flexibleQuery.isNotBlank() && flexibleAlias.contains(flexibleQuery) -> DestinationTargetConfidence.PARTIAL_ALIAS
                    else -> null
                } ?: return@forEach

            val candidate = Candidate(alias = alias, confidence = confidence)
            if (best == null || rankOf(candidate.confidence) < rankOf(best!!.confidence)) {
                best = candidate
            }
        }

        val chosen = best ?: return null
        val target = place.toDestinationTarget(city.cityId, chosen.confidence)
        return DestinationMatch(
            target = target,
            confidence = chosen.confidence,
            matchedAlias = chosen.alias,
        )
    }

    private fun rankOf(confidence: DestinationTargetConfidence): Int =
        when (confidence) {
            DestinationTargetConfidence.EXACT_ALIAS -> 0
            DestinationTargetConfidence.NORMALIZED_ALIAS -> 1
            DestinationTargetConfidence.PARTIAL_ALIAS -> 2
            DestinationTargetConfidence.UNCLEAR -> 3
        }

    private data class Candidate(
        val alias: String,
        val confidence: DestinationTargetConfidence,
    )
}
