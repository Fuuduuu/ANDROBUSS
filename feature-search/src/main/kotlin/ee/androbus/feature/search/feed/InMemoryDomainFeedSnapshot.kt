package ee.androbus.feature.search.feed

import ee.androbus.core.domain.CityId

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
