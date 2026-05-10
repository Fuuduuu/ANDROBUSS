package ee.androbus.data.local.provider

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.domain.FeedId

/**
 * Load-then-serve Room-backed provider baseline.
 *
 * prepare(cityId, feedId) selects and caches the active feed snapshot for city.
 * getSnapshot(cityId) serves cache only and never hits Room.
 */
class RoomDomainFeedSnapshotProvider(
    private val loader: RoomDomainFeedSnapshotLoader,
) : DomainFeedSnapshotProvider {
    private val cache = mutableMapOf<CityId, DomainFeedSnapshot>()

    suspend fun prepare(
        cityId: CityId,
        feedId: FeedId,
    ) {
        val loaded = loader.load(cityId = cityId, feedId = feedId)
        if (loaded == null) {
            cache.remove(cityId)
        } else {
            cache[cityId] = loaded
        }
    }

    override fun getSnapshot(cityId: CityId): DomainFeedSnapshot? = cache[cityId]
}
