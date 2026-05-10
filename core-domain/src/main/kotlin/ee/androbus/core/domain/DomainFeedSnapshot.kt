package ee.androbus.core.domain

/**
 * Parser-agnostic domain feed boundary shared across modules.
 *
 * stopPoints and routePatterns are kept in one snapshot so callers use a
 * consistent feed version when resolving candidates and querying routes.
 */
data class DomainFeedSnapshot(
    val cityId: CityId,
    val stopPoints: List<StopPoint>,
    val routePatterns: List<RoutePattern>,
)
