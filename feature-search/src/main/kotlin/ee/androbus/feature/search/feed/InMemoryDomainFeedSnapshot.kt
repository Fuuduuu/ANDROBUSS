package ee.androbus.feature.search.feed

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.DomainFeedSnapshotProvider

/**
 * Pure Kotlin single-city in-memory provider.
 *
 * Suitable for:
 * - tests with hand-built domain data
 * - temporary bootstrap before Room exists
 * - parser-test callers that convert `MappedGtfsFeed` into `DomainFeedSnapshot`
 *   outside feature-search production runtime wiring
 *
 * Wave 1 multi-city extension can replace this with Map<CityId, DomainFeedSnapshot>.
 */
class InMemoryDomainFeedSnapshot(
    private val snapshot: DomainFeedSnapshot,
) : DomainFeedSnapshotProvider {
    override fun getSnapshot(cityId: CityId): DomainFeedSnapshot? =
        if (snapshot.cityId == cityId) snapshot else null
}
