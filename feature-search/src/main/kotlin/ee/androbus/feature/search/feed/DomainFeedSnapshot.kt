package ee.androbus.feature.search.feed

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.StopPoint

/**
 * Parser-agnostic domain feed boundary for feature-search.
 *
 * stopPoints and routePatterns are kept in one snapshot so callers use a
 * consistent feed version when resolving candidates and querying routes.
 */
data class DomainFeedSnapshot(
    val cityId: CityId,
    val stopPoints: List<StopPoint>,
    val routePatterns: List<RoutePattern>,
)
