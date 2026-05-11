package ee.androbus.data.local.importer

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.FeedId
import ee.androbus.data.local.dao.FeedSnapshotDao
import ee.androbus.data.local.mapping.FeedEntityMapper

/**
 * Writes domain snapshot data into Room for one city + feed scope.
 *
 * This importer accepts domain objects only and remains parser-agnostic.
 */
class FeedSnapshotImporter(
    private val dao: FeedSnapshotDao,
) {
    suspend fun import(
        cityId: CityId,
        feedId: FeedId,
        snapshot: DomainFeedSnapshot,
    ) {
        val stopPointEntities =
            snapshot.stopPoints.map { stopPoint ->
                FeedEntityMapper.toStopPointEntity(
                    stopPoint = stopPoint,
                    cityId = cityId,
                    feedId = feedId,
                )
            }
        val routePatternEntities =
            snapshot.routePatterns.map { routePattern ->
                FeedEntityMapper.toRoutePatternEntity(
                    routePattern = routePattern,
                    cityId = cityId,
                    feedId = feedId,
                )
            }
        val patternStopEntities =
            snapshot.routePatterns.flatMap { routePattern ->
                FeedEntityMapper.toPatternStopEntities(
                    routePattern = routePattern,
                    cityId = cityId,
                    feedId = feedId,
                )
            }

        dao.replaceSnapshot(
            cityId = cityId.value,
            feedId = feedId.value,
            stops = stopPointEntities,
            patterns = routePatternEntities,
            patternStops = patternStopEntities,
        )
    }
}
