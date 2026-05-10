package ee.androbus.data.local.provider

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.FeedId
import ee.androbus.data.local.dao.FeedSnapshotDao
import ee.androbus.data.local.mapping.FeedEntityMapper

class RoomDomainFeedSnapshotLoader(
    private val feedSnapshotDao: FeedSnapshotDao,
) {
    suspend fun load(
        cityId: CityId,
        feedId: FeedId,
    ): DomainFeedSnapshot? {
        val stopPointEntities = feedSnapshotDao.getStopPoints(cityId = cityId.value, feedId = feedId.value)
        val routePatternEntities = feedSnapshotDao.getRoutePatterns(cityId = cityId.value, feedId = feedId.value)

        if (stopPointEntities.isEmpty() && routePatternEntities.isEmpty()) {
            return null
        }

        val stopPoints = stopPointEntities.map(FeedEntityMapper::toStopPoint)
        val routePatterns =
            routePatternEntities.map { patternEntity ->
                val orderedStops =
                    feedSnapshotDao.getPatternStops(
                        cityId = cityId.value,
                        feedId = feedId.value,
                        patternId = patternEntity.patternId,
                    )
                FeedEntityMapper.toRoutePattern(
                    routePatternEntity = patternEntity,
                    orderedPatternStops = orderedStops,
                )
            }

        return DomainFeedSnapshot(
            cityId = cityId,
            stopPoints = stopPoints,
            routePatterns = routePatterns,
        )
    }
}
