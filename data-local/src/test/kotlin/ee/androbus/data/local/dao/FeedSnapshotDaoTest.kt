package ee.androbus.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class FeedSnapshotDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: FeedSnapshotDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.feedSnapshotDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and query stop points returns scoped rows`() =
        runDb {
            dao.insertStopPoints(
                listOf(
                    stopPointEntity(stopId = "RKV_A_OUT"),
                    stopPointEntity(stopId = "RKV_C"),
                ),
            )

            val result = dao.getStopPoints(cityId = "rakvere", feedId = "feed-a")
            assertEquals(2, result.size)
            assertEquals(setOf("RKV_A_OUT", "RKV_C"), result.map { it.stopId }.toSet())
        }

    @Test
    fun `wrong cityId and feedId return empty stop point results`() =
        runDb {
            dao.insertStopPoints(listOf(stopPointEntity(stopId = "RKV_C")))

            assertTrue(dao.getStopPoints(cityId = "parnu", feedId = "feed-a").isEmpty())
            assertTrue(dao.getStopPoints(cityId = "rakvere", feedId = "feed-b").isEmpty())
        }

    @Test
    fun `same stopId may exist across feed scopes`() =
        runDb {
            dao.insertStopPoints(
                listOf(
                    stopPointEntity(stopId = "RKV_C", cityId = "rakvere", feedId = "feed-a"),
                    stopPointEntity(stopId = "RKV_C", cityId = "rakvere", feedId = "feed-b"),
                    stopPointEntity(stopId = "RKV_C", cityId = "parnu", feedId = "feed-a"),
                ),
            )

            assertEquals(1, dao.getStopPoints(cityId = "rakvere", feedId = "feed-a").size)
            assertEquals(1, dao.getStopPoints(cityId = "rakvere", feedId = "feed-b").size)
            assertEquals(1, dao.getStopPoints(cityId = "parnu", feedId = "feed-a").size)
        }

    @Test
    fun `insert and query route patterns returns scoped rows`() =
        runDb {
            dao.insertRoutePatterns(
                listOf(
                    routePatternEntity(patternId = "pattern:T1"),
                    routePatternEntity(patternId = "pattern:T3"),
                ),
            )

            val result = dao.getRoutePatterns(cityId = "rakvere", feedId = "feed-a")
            assertEquals(2, result.size)
            assertEquals(setOf("pattern:T1", "pattern:T3"), result.map { it.patternId }.toSet())
        }

    @Test
    fun `same patternId may exist across feed scopes`() =
        runDb {
            dao.insertRoutePatterns(
                listOf(
                    routePatternEntity(patternId = "pattern:T1", cityId = "rakvere", feedId = "feed-a"),
                    routePatternEntity(patternId = "pattern:T1", cityId = "rakvere", feedId = "feed-b"),
                    routePatternEntity(patternId = "pattern:T1", cityId = "parnu", feedId = "feed-a"),
                ),
            )

            assertEquals(1, dao.getRoutePatterns(cityId = "rakvere", feedId = "feed-a").size)
            assertEquals(1, dao.getRoutePatterns(cityId = "rakvere", feedId = "feed-b").size)
            assertEquals(1, dao.getRoutePatterns(cityId = "parnu", feedId = "feed-a").size)
        }

    @Test
    fun `pattern stops are returned in sequence order and duplicate stop ids survive`() =
        runDb {
            dao.insertRoutePatterns(listOf(routePatternEntity(patternId = "pattern:T3")))
            dao.insertPatternStops(
                listOf(
                    patternStopEntity(patternId = "pattern:T3", sequence = 3, stopId = "RKV_A_OUT"),
                    patternStopEntity(patternId = "pattern:T3", sequence = 1, stopId = "RKV_A_OUT"),
                    patternStopEntity(patternId = "pattern:T3", sequence = 2, stopId = "RKV_B"),
                ),
            )

            val result = dao.getPatternStops(cityId = "rakvere", feedId = "feed-a", patternId = "pattern:T3")
            assertEquals(listOf(1, 2, 3), result.map { it.sequence })
            assertEquals(listOf("RKV_A_OUT", "RKV_B", "RKV_A_OUT"), result.map { it.stopId })
        }

    @Test
    fun `deleteRoutePatterns affects only matching city and feed scope`() =
        runDb {
            dao.insertRoutePatterns(
                listOf(
                    routePatternEntity(patternId = "pattern:T1", cityId = "rakvere", feedId = "feed-a"),
                    routePatternEntity(patternId = "pattern:T2", cityId = "rakvere", feedId = "feed-b"),
                ),
            )
            dao.insertPatternStops(
                listOf(
                    patternStopEntity(patternId = "pattern:T1", cityId = "rakvere", feedId = "feed-a", sequence = 1, stopId = "RKV_A_OUT"),
                    patternStopEntity(patternId = "pattern:T1", cityId = "rakvere", feedId = "feed-a", sequence = 2, stopId = "RKV_C"),
                    patternStopEntity(patternId = "pattern:T2", cityId = "rakvere", feedId = "feed-b", sequence = 1, stopId = "RKV_A_OUT"),
                    patternStopEntity(patternId = "pattern:T2", cityId = "rakvere", feedId = "feed-b", sequence = 2, stopId = "RKV_C"),
                ),
            )

            dao.deleteRoutePatterns(cityId = "rakvere", feedId = "feed-a")

            assertTrue(dao.getRoutePatterns(cityId = "rakvere", feedId = "feed-a").isEmpty())
            assertEquals(1, dao.getRoutePatterns(cityId = "rakvere", feedId = "feed-b").size)
            assertTrue(dao.getPatternStops(cityId = "rakvere", feedId = "feed-a", patternId = "pattern:T1").isEmpty())
            assertEquals(2, dao.getPatternStops(cityId = "rakvere", feedId = "feed-b", patternId = "pattern:T2").size)
        }

    @Test
    fun `deleteStopPoints removes only matching city and feed scope`() =
        runDb {
            dao.insertStopPoints(
                listOf(
                    stopPointEntity(stopId = "RKV_C", cityId = "rakvere", feedId = "feed-a"),
                    stopPointEntity(stopId = "RKV_C", cityId = "rakvere", feedId = "feed-b"),
                ),
            )

            dao.deleteStopPoints(cityId = "rakvere", feedId = "feed-a")

            assertTrue(dao.getStopPoints(cityId = "rakvere", feedId = "feed-a").isEmpty())
            assertEquals(1, dao.getStopPoints(cityId = "rakvere", feedId = "feed-b").size)
        }

    private fun stopPointEntity(
        stopId: String,
        cityId: String = "rakvere",
        feedId: String = "feed-a",
    ): StopPointEntity =
        StopPointEntity(
            cityId = cityId,
            feedId = feedId,
            stopId = stopId,
            stopGroupId = "group:$stopId",
            displayName = if (stopId == "RKV_C") "Jaam" else "Keskpeatus",
            latitude = 59.35,
            longitude = 26.36,
            platformCode = null,
        )

    private fun routePatternEntity(
        patternId: String,
        cityId: String = "rakvere",
        feedId: String = "feed-a",
    ): RoutePatternEntity =
        RoutePatternEntity(
            cityId = cityId,
            feedId = feedId,
            patternId = patternId,
            routeLineId = "line:${patternId.removePrefix("pattern:")}",
            displayName = patternId,
        )

    private fun patternStopEntity(
        patternId: String,
        sequence: Int,
        stopId: String,
        cityId: String = "rakvere",
        feedId: String = "feed-a",
    ): PatternStopEntity =
        PatternStopEntity(
            cityId = cityId,
            feedId = feedId,
            patternId = patternId,
            sequence = sequence,
            stopId = stopId,
        )

    private fun runDb(block: suspend () -> Unit) {
        runBlocking(Dispatchers.IO) { block() }
    }
}
