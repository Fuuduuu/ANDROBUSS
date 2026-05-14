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
    private val assetPath: String = "bootstrap/rakvere_bootstrap.json",
) {
    private val cityId = CityId("rakvere")
    private val feedId = FeedId("rakvere-bootstrap-v1")
    private val json = Json

    /**
     * Ensures bootstrap snapshot is available with cold-start-safe ordering.
     *
     * Order:
     * 1) If in-memory provider cache already has snapshot, return.
     * 2) Prepare provider from Room for this bootstrap feed scope.
     * 3) If Room load populated cache, return.
     * 4) Fallback to bundled asset import.
     * 5) Prepare again so cache serves imported snapshot.
     *
     * Safe to call repeatedly.
     * Missing/invalid asset is treated as FeedNotReady and does not crash.
     */
    suspend fun bootstrapIfNeeded() {
        if (provider.getSnapshot(cityId) != null) return
        provider.prepare(cityId = cityId, feedId = feedId)
        if (provider.getSnapshot(cityId) != null) return

        val dto = loadDto() ?: return
        val snapshot = dto.toDomainFeedSnapshot()

        importer.import(cityId = cityId, feedId = feedId, snapshot = snapshot)
        provider.prepare(cityId = cityId, feedId = feedId)
    }

    private fun loadDto(): BootstrapFeedDto? =
        try {
            context.assets.open(assetPath).use { stream ->
                json.decodeFromString<BootstrapFeedDto>(stream.bufferedReader().readText())
            }
        } catch (_: Exception) {
            null
        }
}
