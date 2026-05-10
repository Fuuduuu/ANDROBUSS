package ee.androbus.data.local.provider

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.core.routing.DirectRouteSearchResult
import ee.androbus.data.local.database.AppDatabase
import ee.androbus.data.local.entity.PatternStopEntity
import ee.androbus.data.local.entity.RoutePatternEntity
import ee.androbus.data.local.entity.StopPointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class RoomDomainFeedSnapshotLoaderTest {
    private lateinit var database: AppDatabase
    private lateinit var loader: RoomDomainFeedSnapshotLoader
    private lateinit var provider: RoomDomainFeedSnapshotProvider

    private val cityId = CityId("rakvere")
    private val feedA = FeedId("feed-a")
    private val feedB = FeedId("feed-b")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        loader = RoomDomainFeedSnapshotLoader(database.feedSnapshotDao())
        provider = RoomDomainFeedSnapshotProvider(loader)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getSnapshot returns null before prepare`() {
        assertNull(provider.getSnapshot(cityId))
    }

    @Test
    fun `prepare loads snapshot into cache`() =
        runDb {
            seedFeed(cityId = "rakvere", feedId = "feed-a")

            provider.prepare(cityId = cityId, feedId = feedA)
            val snapshot = provider.getSnapshot(cityId)

            assertNotNull(snapshot)
            assertEquals(cityId, snapshot.cityId)
        }

    @Test
    fun `unprepared city returns null`() =
        runDb {
            seedFeed(cityId = "rakvere", feedId = "feed-a")
            provider.prepare(cityId = cityId, feedId = feedA)

            assertNull(provider.getSnapshot(CityId("parnu")))
        }

    @Test
    fun `prepare with missing feed clears cache for city`() =
        runDb {
            seedFeed(cityId = "rakvere", feedId = "feed-a")
            provider.prepare(cityId = cityId, feedId = feedA)
            assertNotNull(provider.getSnapshot(cityId))

            provider.prepare(cityId = cityId, feedId = feedB)
            assertNull(provider.getSnapshot(cityId))
        }

    @Test
    fun `loaded snapshot contains expected scoped stopPoints and routePatterns`() =
        runDb {
            seedFeed(cityId = "rakvere", feedId = "feed-a")

            provider.prepare(cityId = cityId, feedId = feedA)
            val snapshot = provider.getSnapshot(cityId)
            assertNotNull(snapshot)

            assertEquals(setOf("RKV_A_OUT", "RKV_B", "RKV_C"), snapshot.stopPoints.map { it.id.value }.toSet())
            assertEquals(setOf("pattern:T1", "pattern:T3"), snapshot.routePatterns.map { it.id.value }.toSet())
        }

    @Test
    fun `routePatterns preserve stop order and duplicate stop ids after Room load`() =
        runDb {
            seedFeed(cityId = "rakvere", feedId = "feed-a")

            provider.prepare(cityId = cityId, feedId = feedA)
            val snapshot = provider.getSnapshot(cityId)
            assertNotNull(snapshot)

            val t1 = snapshot.routePatterns.single { it.id.value == "pattern:T1" }
            assertEquals(listOf("RKV_A_OUT", "RKV_B", "RKV_C"), t1.orderedStopPointIds().map { it.value })

            val t3 = snapshot.routePatterns.single { it.id.value == "pattern:T3" }
            assertEquals(listOf("RKV_A_OUT", "RKV_B", "RKV_A_OUT"), t3.orderedStopPointIds().map { it.value })
        }

    @Test
    fun `loaded routePatterns parity with PASS 21 in-memory flow via direct route search`() =
        runDb {
            seedFeed(cityId = "rakvere", feedId = "feed-a")

            provider.prepare(cityId = cityId, feedId = feedA)
            val snapshot = provider.getSnapshot(cityId)
            assertNotNull(snapshot)

            val result =
                DirectRouteSearch().findDirectRoutes(
                    origin = StopPointId("RKV_A_OUT"),
                    destination = StopPointId("RKV_C"),
                    patterns = snapshot.routePatterns,
                )

            assertIs<DirectRouteSearchResult.Found>(result)
        }

    @Test
    fun `DomainFeedSnapshotProvider type comes from core-domain package`() {
        assertEquals(
            "ee.androbus.core.domain.DomainFeedSnapshotProvider",
            DomainFeedSnapshotProvider::class.java.name,
        )
    }

    private suspend fun seedFeed(
        cityId: String,
        feedId: String,
    ) {
        val dao = database.feedSnapshotDao()
        dao.insertStopPoints(
            listOf(
                StopPointEntity(
                    cityId = cityId,
                    feedId = feedId,
                    stopId = "RKV_A_OUT",
                    stopGroupId = "group:keskpeatus-out",
                    displayName = "Keskpeatus",
                    latitude = 59.3461,
                    longitude = 26.3552,
                    platformCode = null,
                ),
                StopPointEntity(
                    cityId = cityId,
                    feedId = feedId,
                    stopId = "RKV_B",
                    stopGroupId = "group:spordikeskus",
                    displayName = "Spordikeskus",
                    latitude = 59.3500,
                    longitude = 26.3600,
                    platformCode = null,
                ),
                StopPointEntity(
                    cityId = cityId,
                    feedId = feedId,
                    stopId = "RKV_C",
                    stopGroupId = "group:jaam",
                    displayName = "Jaam",
                    latitude = 59.3550,
                    longitude = 26.3650,
                    platformCode = null,
                ),
            ),
        )

        dao.insertRoutePatterns(
            listOf(
                RoutePatternEntity(
                    cityId = cityId,
                    feedId = feedId,
                    patternId = "pattern:T1",
                    routeLineId = "line:T1",
                    displayName = "T1",
                ),
                RoutePatternEntity(
                    cityId = cityId,
                    feedId = feedId,
                    patternId = "pattern:T3",
                    routeLineId = "line:T3",
                    displayName = "T3",
                ),
            ),
        )

        dao.insertPatternStops(
            listOf(
                PatternStopEntity(cityId = cityId, feedId = feedId, patternId = "pattern:T1", sequence = 3, stopId = "RKV_C"),
                PatternStopEntity(cityId = cityId, feedId = feedId, patternId = "pattern:T1", sequence = 1, stopId = "RKV_A_OUT"),
                PatternStopEntity(cityId = cityId, feedId = feedId, patternId = "pattern:T1", sequence = 2, stopId = "RKV_B"),
                PatternStopEntity(cityId = cityId, feedId = feedId, patternId = "pattern:T3", sequence = 1, stopId = "RKV_A_OUT"),
                PatternStopEntity(cityId = cityId, feedId = feedId, patternId = "pattern:T3", sequence = 2, stopId = "RKV_B"),
                PatternStopEntity(cityId = cityId, feedId = feedId, patternId = "pattern:T3", sequence = 3, stopId = "RKV_A_OUT"),
            ),
        )
    }

    private fun runDb(block: suspend () -> Unit) {
        runBlocking(Dispatchers.IO) { block() }
    }
}
