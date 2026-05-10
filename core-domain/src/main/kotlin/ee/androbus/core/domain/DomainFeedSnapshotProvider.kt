package ee.androbus.core.domain

/**
 * Provides domain-mapped feed data for a city.
 *
 * Implementations may be:
 * - in-memory
 * - Room-backed later
 * - test fixture-backed later
 *
 * Runtime modules should depend on this interface and remain parser-agnostic.
 */
interface DomainFeedSnapshotProvider {
    fun getSnapshot(cityId: CityId): DomainFeedSnapshot?
}
