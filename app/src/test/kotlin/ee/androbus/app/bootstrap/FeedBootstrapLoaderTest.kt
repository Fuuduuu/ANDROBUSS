package ee.androbus.app.bootstrap

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.StopPointId
import ee.androbus.data.local.database.AppDatabase
import ee.androbus.data.local.importer.FeedSnapshotImporter
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotLoader
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FeedBootstrapLoaderTest {
    private val cityId = CityId("rakvere")
    private val primaryFeedId = FeedId("rakvere-v20260428")
    private val fallbackFeedId = FeedId("rakvere-bootstrap-v1")

    private lateinit var database: AppDatabase
    private lateinit var provider: RoomDomainFeedSnapshotProvider
    private lateinit var importer: FeedSnapshotImporter
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = database.feedSnapshotDao()
        importer = FeedSnapshotImporter(dao)
        provider = RoomDomainFeedSnapshotProvider(RoomDomainFeedSnapshotLoader(dao))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `bootstrapIfNeeded loads real static runtime asset by default`() =
        runDb {
            val loader = FeedBootstrapLoader(context = context, importer = importer, provider = provider)

            loader.bootstrapIfNeeded()
            val snapshot = assertNotNull(provider.getSnapshot(cityId))
            assertEquals(98, snapshot.stopPoints.size)
            assertEquals(7, snapshot.routePatterns.size)
            assertTrue(snapshot.stopPoints.any { it.displayName == "Rakvere bussijaam" })
        }

    @Test
    fun `bootstrap preserves stop ids from asset and never uses display names as ids`() =
        runDb {
            val loader = FeedBootstrapLoader(context = context, importer = importer, provider = provider)

            loader.bootstrapIfNeeded()
            val snapshot = assertNotNull(provider.getSnapshot(cityId))

            val ids = snapshot.stopPoints.map { it.id }
            assertTrue(ids.contains(StopPointId("152898")))
            assertFalse(ids.any { it == StopPointId("Jaam") })
            assertFalse(ids.any { it == StopPointId("Rakvere bussijaam") })
            assertFalse(ids.any { it == StopPointId("Polikliinik") })
        }

    @Test
    fun `bootstrapIfNeeded is idempotent`() =
        runDb {
            val loader = FeedBootstrapLoader(context = context, importer = importer, provider = provider)

            loader.bootstrapIfNeeded()
            loader.bootstrapIfNeeded()

            val snapshot = assertNotNull(provider.getSnapshot(cityId))
            assertEquals(98, snapshot.stopPoints.size)
            assertEquals(7, snapshot.routePatterns.size)

            val dao = database.feedSnapshotDao()
            assertEquals(98, dao.getStopPoints(cityId = "rakvere", feedId = "rakvere-v20260428").size)
            assertEquals(7, dao.getRoutePatterns(cityId = "rakvere", feedId = "rakvere-v20260428").size)
        }

    @Test
    fun `bootstrapIfNeeded prepares existing Room snapshot before asset fallback`() =
        runDb {
            val snapshot = loadSnapshot("bootstrap/rakvere_feed_20260428.json")
            importer.import(
                cityId = cityId,
                feedId = primaryFeedId,
                snapshot = snapshot,
            )

            val freshProvider = RoomDomainFeedSnapshotProvider(RoomDomainFeedSnapshotLoader(database.feedSnapshotDao()))
            val loader =
                FeedBootstrapLoader(
                    context = context,
                    importer = importer,
                    provider = freshProvider,
                    primaryAssetPath = "bootstrap/missing-real.json",
                    assetPath = "bootstrap/missing.json",
                )

            loader.bootstrapIfNeeded()

            val loaded = assertNotNull(freshProvider.getSnapshot(cityId))
            assertEquals(snapshot.stopPoints.size, loaded.stopPoints.size)
            assertEquals(snapshot.routePatterns.size, loaded.routePatterns.size)
        }

    @Test
    fun `fallback synthetic asset works when primary asset is missing`() =
        runDb {
            val loader =
                FeedBootstrapLoader(
                    context = context,
                    importer = importer,
                    provider = provider,
                    primaryAssetPath = "bootstrap/missing-real.json",
                )

            loader.bootstrapIfNeeded()
            val snapshot = assertNotNull(provider.getSnapshot(cityId))
            assertEquals(4, snapshot.stopPoints.size)
            assertEquals(2, snapshot.routePatterns.size)

            val dao = database.feedSnapshotDao()
            assertEquals(4, dao.getStopPoints(cityId = "rakvere", feedId = fallbackFeedId.value).size)
            assertEquals(2, dao.getRoutePatterns(cityId = "rakvere", feedId = fallbackFeedId.value).size)
        }

    @Test
    fun `room empty and both assets missing is safe FeedNotReady style result`() =
        runDb {
            val loader =
                FeedBootstrapLoader(
                    context = context,
                    importer = importer,
                    provider = provider,
                    primaryAssetPath = "bootstrap/missing-real.json",
                    assetPath = "bootstrap/missing.json",
                )

            loader.bootstrapIfNeeded()

            assertNull(provider.getSnapshot(cityId))
        }

    private fun runDb(block: suspend () -> Unit) {
        runBlocking(Dispatchers.IO) { block() }
    }

    private fun loadSnapshot(assetPath: String): DomainFeedSnapshot {
        val dtoText =
            context.assets.open(assetPath).use { stream ->
                stream.bufferedReader().readText()
            }
        val dto = Json.decodeFromString<BootstrapFeedDto>(dtoText)
        return dto.toDomainFeedSnapshot()
    }
}
