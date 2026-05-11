package ee.androbus.app.bootstrap

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import kotlinx.serialization.Serializable

@Serializable
data class BootstrapFeedDto(
    val cityId: String,
    val feedId: String,
    val stopPoints: List<StopPointDto>,
    val routePatterns: List<RoutePatternDto>,
)

@Serializable
data class StopPointDto(
    val id: String,
    val stopGroupId: String,
    val displayName: String,
    val lat: Double,
    val lon: Double,
    val platformCode: String? = null,
)

@Serializable
data class RoutePatternDto(
    val id: String,
    val routeLineId: String,
    val displayName: String,
    val stopIds: List<String>,
)

fun BootstrapFeedDto.toDomainFeedSnapshot(): DomainFeedSnapshot {
    val domainCityId = CityId(cityId)
    val domainFeedId = FeedId(feedId)

    val domainStopPoints =
        stopPoints.map { dto ->
            StopPoint(
                id = StopPointId(dto.id),
                stopGroupId = StopGroupId(dto.stopGroupId),
                displayName = dto.displayName,
                location = GeoPoint(dto.lat, dto.lon),
                cityId = domainCityId,
                feedId = domainFeedId,
                platformCode = dto.platformCode,
            )
        }

    val domainRoutePatterns =
        routePatterns.map { dto ->
            RoutePattern(
                id = RoutePatternId(dto.id),
                routeLineId = RouteLineId(dto.routeLineId),
                displayName = dto.displayName,
                cityId = domainCityId,
                feedId = domainFeedId,
                stops =
                    dto.stopIds.mapIndexed { index, stopId ->
                        PatternStop(
                            sequence = index + 1,
                            stopPointId = StopPointId(stopId),
                        )
                    },
            )
        }

    return DomainFeedSnapshot(
        cityId = domainCityId,
        stopPoints = domainStopPoints,
        routePatterns = domainRoutePatterns,
    )
}
