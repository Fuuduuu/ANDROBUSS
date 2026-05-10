package ee.androbus.data.local.mapping

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import ee.androbus.data.local.entity.PatternStopEntity
import ee.androbus.data.local.entity.RoutePatternEntity
import ee.androbus.data.local.entity.StopPointEntity

object FeedEntityMapper {
    fun toStopPointEntity(
        stopPoint: StopPoint,
        cityId: CityId,
        feedId: FeedId,
    ): StopPointEntity =
        StopPointEntity(
            cityId = cityId.value,
            feedId = feedId.value,
            stopId = stopPoint.id.value,
            stopGroupId = stopPoint.stopGroupId.value,
            displayName = stopPoint.displayName,
            latitude = stopPoint.location.latitude,
            longitude = stopPoint.location.longitude,
            platformCode = stopPoint.platformCode,
        )

    fun toStopPoint(entity: StopPointEntity): StopPoint =
        StopPoint(
            id = StopPointId(entity.stopId),
            stopGroupId = StopGroupId(entity.stopGroupId),
            displayName = entity.displayName,
            location = GeoPoint(entity.latitude, entity.longitude),
            cityId = CityId(entity.cityId),
            feedId = FeedId(entity.feedId),
            platformCode = entity.platformCode,
        )

    fun toRoutePatternEntity(
        routePattern: RoutePattern,
        cityId: CityId,
        feedId: FeedId,
    ): RoutePatternEntity =
        RoutePatternEntity(
            cityId = cityId.value,
            feedId = feedId.value,
            patternId = routePattern.id.value,
            routeLineId = routePattern.routeLineId.value,
            displayName = routePattern.displayName,
        )

    fun toPatternStopEntities(
        routePattern: RoutePattern,
        cityId: CityId,
        feedId: FeedId,
    ): List<PatternStopEntity> =
        routePattern.stops.map { stop ->
            PatternStopEntity(
                cityId = cityId.value,
                feedId = feedId.value,
                patternId = routePattern.id.value,
                sequence = stop.sequence,
                stopId = stop.stopPointId.value,
            )
        }

    fun toRoutePattern(
        routePatternEntity: RoutePatternEntity,
        orderedPatternStops: List<PatternStopEntity>,
    ): RoutePattern =
        RoutePattern(
            id = RoutePatternId(routePatternEntity.patternId),
            routeLineId = RouteLineId(routePatternEntity.routeLineId),
            displayName = routePatternEntity.displayName,
            cityId = CityId(routePatternEntity.cityId),
            feedId = FeedId(routePatternEntity.feedId),
            stops = orderedPatternStops.sortedBy { it.sequence }.map { stop -> PatternStop(sequence = stop.sequence, stopPointId = StopPointId(stop.stopId)) },
        )
}
