package ee.androbus.feature.search.feed

import ee.androbus.core.domain.CityId

/**
 * Provides domain-mapped feed data for a city.
 *
 * Implementations may be:
 * - in-memory (PASS 21)
 * - Room-backed later
 * - test fixture-backed later
 *
 * feature-search production code should depend on this interface and remain
 * parser-agnostic.
 */
interface DomainFeedSnapshotProvider {
    fun getSnapshot(cityId: CityId): DomainFeedSnapshot?
}
