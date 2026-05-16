package ee.androbus.app.bootstrap

import android.content.Context
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.data.local.importer.FeedSnapshotImporter
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotProvider
import kotlinx.serialization.json.Json

class FeedBootstrapLoader(
    private val context: Context,
    private val importer: FeedSnapshotImporter,
    private val provider: RoomDomainFeedSnapshotProvider,
    private val primaryAssetPath: String = "bootstrap/rakvere_feed_20260428.json",
    private val assetPath: String = "bootstrap/rakvere_bootstrap.json",
) {
    private val cityId = CityId("rakvere")
    private val primaryFeedId = FeedId("rakvere-v20260428")
    private val fallbackFeedId = FeedId("rakvere-bootstrap-v1")
    private val json = Json

    /**
     * Ensures bootstrap snapshot is available with cold-start-safe primary/fallback ordering.
     *
     * Order:
     * 1) If in-memory provider cache already has snapshot, return.
     * 2) Try loading existing Room snapshot for primary feed scope.
     * 3) If Room load populated cache, return.
     * 4) Try importing the primary real static runtime asset.
     * 5) If primary import is unavailable, fallback to synthetic asset import.
     * 6) Prepare again so cache serves imported snapshot.
     *
     * Safe to call repeatedly.
     * Missing/invalid asset is treated as FeedNotReady and does not crash.
     */
    suspend fun bootstrapIfNeeded() {
        if (provider.getSnapshot(cityId) != null) return
        provider.prepare(cityId = cityId, feedId = primaryFeedId)
        if (provider.getSnapshot(cityId) != null) return

        val primaryDto = loadDto(primaryAssetPath)
        if (primaryDto != null) {
            val primarySnapshot = primaryDto.toDomainFeedSnapshot()
            importer.import(cityId = cityId, feedId = primaryFeedId, snapshot = primarySnapshot)
            provider.prepare(cityId = cityId, feedId = primaryFeedId)
            return
        }

        val fallbackDto = loadDto(assetPath) ?: return
        val fallbackSnapshot = fallbackDto.toDomainFeedSnapshot()
        importer.import(cityId = cityId, feedId = fallbackFeedId, snapshot = fallbackSnapshot)
        provider.prepare(cityId = cityId, feedId = fallbackFeedId)
    }

    private fun loadDto(path: String): BootstrapFeedDto? =
        try {
            context.assets.open(path).use { stream ->
                json.decodeFromString<BootstrapFeedDto>(stream.bufferedReader().readText())
            }
        } catch (_: Exception) {
            null
        }
}
