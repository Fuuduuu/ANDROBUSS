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
    fun `bootstrapIfNeeded loads snapshot from bundled asset`() =
        runDb {
            val loader = FeedBootstrapLoader(context = context, importer = importer, provider = provider)

            loader.bootstrapIfNeeded()
            val snapshot = assertNotNull(provider.getSnapshot(CityId("rakvere")))
            assertTrue(snapshot.stopPoints.isNotEmpty())
            assertTrue(snapshot.routePatterns.isNotEmpty())
        }

    @Test
    fun `bootstrap preserves explicit stop ids and never uses display names as ids`() =
        runDb {
            val loader = FeedBootstrapLoader(context = context, importer = importer, provider = provider)

            loader.bootstrapIfNeeded()
            val snapshot = assertNotNull(provider.getSnapshot(CityId("rakvere")))

            val ids = snapshot.stopPoints.map { it.id }
            assertTrue(ids.contains(StopPointId("RKV_A_OUT")))
            assertTrue(ids.contains(StopPointId("RKV_C")))
            assertFalse(ids.any { it == StopPointId("Jaam") })
        }

    @Test
    fun `bootstrapIfNeeded is idempotent`() =
        runDb {
            val loader = FeedBootstrapLoader(context = context, importer = importer, provider = provider)

            loader.bootstrapIfNeeded()
            loader.bootstrapIfNeeded()

            val snapshot = assertNotNull(provider.getSnapshot(CityId("rakvere")))
            assertEquals(4, snapshot.stopPoints.size)
            assertEquals(2, snapshot.routePatterns.size)

            val dao = database.feedSnapshotDao()
            assertEquals(4, dao.getStopPoints(cityId = "rakvere", feedId = "rakvere-bootstrap-v1").size)
            assertEquals(2, dao.getRoutePatterns(cityId = "rakvere", feedId = "rakvere-bootstrap-v1").size)
        }

    @Test
    fun `bootstrapIfNeeded prepares existing Room snapshot before asset fallback`() =
        runDb {
            val snapshot = loadBundledSnapshot()
            importer.import(
                cityId = CityId("rakvere"),
                feedId = FeedId("rakvere-bootstrap-v1"),
                snapshot = snapshot,
            )

            val freshProvider = RoomDomainFeedSnapshotProvider(RoomDomainFeedSnapshotLoader(database.feedSnapshotDao()))
            val loader =
                FeedBootstrapLoader(
                    context = context,
                    importer = importer,
                    provider = freshProvider,
                    assetPath = "bootstrap/missing.json",
                )

            loader.bootstrapIfNeeded()

            val loaded = assertNotNull(freshProvider.getSnapshot(CityId("rakvere")))
            assertEquals(snapshot.stopPoints.size, loaded.stopPoints.size)
            assertEquals(snapshot.routePatterns.size, loaded.routePatterns.size)
        }

    @Test
    fun `missing asset is safe FeedNotReady style result`() =
        runDb {
            val loader =
                FeedBootstrapLoader(
                    context = context,
                    importer = importer,
                    provider = provider,
                    assetPath = "bootstrap/missing.json",
                )

            loader.bootstrapIfNeeded()

            assertNull(provider.getSnapshot(CityId("rakvere")))
        }

    private fun runDb(block: suspend () -> Unit) {
        runBlocking(Dispatchers.IO) { block() }
    }

    private fun loadBundledSnapshot(): DomainFeedSnapshot {
        val dtoText =
            context.assets.open("bootstrap/rakvere_bootstrap.json").use { stream ->
                stream.bufferedReader().readText()
            }
        val dto = Json.decodeFromString<BootstrapFeedDto>(dtoText)
        return dto.toDomainFeedSnapshot()
    }
}
