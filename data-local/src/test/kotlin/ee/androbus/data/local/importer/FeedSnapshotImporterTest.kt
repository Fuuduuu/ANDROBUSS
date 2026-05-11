package ee.androbus.data.local.importer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import ee.androbus.data.local.dao.FeedSnapshotDao
import ee.androbus.data.local.database.AppDatabase
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class FeedSnapshotImporterTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: FeedSnapshotDao
    private lateinit var loader: RoomDomainFeedSnapshotLoader
    private lateinit var importer: FeedSnapshotImporter

    private val rakvere = CityId("rakvere")
    private val voru = CityId("voru")
    private val feedRakvere = FeedId("rakvere-smoke")
    private val feedVoru = FeedId("voru-smoke")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.feedSnapshotDao()
        loader = RoomDomainFeedSnapshotLoader(dao)
        importer = FeedSnapshotImporter(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `import writes stop points into Room`() =
        runDb {
            val snapshot =
                snapshot(
                    cityId = rakvere,
                    stopIds = listOf("RKV_A_OUT", "RKV_C"),
                    patternId = "pattern:T1",
                    patternStops = listOf("RKV_A_OUT", "RKV_C"),
                )

            importer.import(cityId = rakvere, feedId = feedRakvere, snapshot = snapshot)

            val loaded = loader.load(cityId = rakvere, feedId = feedRakvere)
            assertNotNull(loaded)
            assertEquals(setOf("RKV_A_OUT", "RKV_C"), loaded.stopPoints.map { it.id.value }.toSet())
        }

    @Test
    fun `import writes route patterns and keeps pattern stop order`() =
        runDb {
            val snapshot =
                snapshot(
                    cityId = rakvere,
                    stopIds = listOf("RKV_A_OUT", "RKV_B", "RKV_C"),
                    patternId = "pattern:T1",
                    patternStops = listOf("RKV_A_OUT", "RKV_B", "RKV_C"),
                )

            importer.import(cityId = rakvere, feedId = feedRakvere, snapshot = snapshot)

            val loaded = loader.load(cityId = rakvere, feedId = feedRakvere)
            assertNotNull(loaded)
            val pattern = loaded.routePatterns.single { it.id == RoutePatternId("pattern:T1") }
            assertEquals(listOf("RKV_A_OUT", "RKV_B", "RKV_C"), pattern.orderedStopPointIds().map { it.value })
        }

    @Test
    fun `import twice for same city and feed replaces previous snapshot`() =
        runDb {
            val first =
                snapshot(
                    cityId = rakvere,
                    stopIds = listOf("OLD_A", "OLD_B"),
                    patternId = "pattern:OLD",
                    patternStops = listOf("OLD_A", "OLD_B"),
                )
            val second =
                snapshot(
                    cityId = rakvere,
                    stopIds = listOf("NEW_A", "NEW_B"),
                    patternId = "pattern:NEW",
                    patternStops = listOf("NEW_A", "NEW_B"),
                )

            importer.import(cityId = rakvere, feedId = feedRakvere, snapshot = first)
            importer.import(cityId = rakvere, feedId = feedRakvere, snapshot = second)

            val stopRows = dao.getStopPoints(cityId = rakvere.value, feedId = feedRakvere.value)
            val patternRows = dao.getRoutePatterns(cityId = rakvere.value, feedId = feedRakvere.value)
            assertEquals(setOf("NEW_A", "NEW_B"), stopRows.map { it.stopId }.toSet())
            assertEquals(setOf("pattern:NEW"), patternRows.map { it.patternId }.toSet())
            assertTrue(dao.getPatternStops(rakvere.value, feedRakvere.value, "pattern:OLD").isEmpty())
            assertEquals(2, dao.getPatternStops(rakvere.value, feedRakvere.value, "pattern:NEW").size)
        }

    @Test
    fun `import preserves stop point id anti-fabrication`() =
        runDb {
            val snapshot =
                snapshot(
                    cityId = rakvere,
                    stopIds = listOf("RKV_A_OUT", "RKV_C"),
                    patternId = "pattern:T1",
                    patternStops = listOf("RKV_A_OUT", "RKV_C"),
                    displayNames = mapOf("RKV_C" to "Jaam"),
                )

            importer.import(cityId = rakvere, feedId = feedRakvere, snapshot = snapshot)

            val loaded = loader.load(cityId = rakvere, feedId = feedRakvere)
            assertNotNull(loaded)
            val id = loaded.stopPoints.single { it.displayName == "Jaam" }.id
            assertEquals(StopPointId("RKV_C"), id)
            assertNotEquals(StopPointId("Jaam"), id)
        }

    @Test
    fun `same local stop id in different city and feed scopes stays separate`() =
        runDb {
            val rakvereSnapshot =
                snapshot(
                    cityId = rakvere,
                    stopIds = listOf("SAME", "RKV_B"),
                    patternId = "pattern:RAK",
                    patternStops = listOf("SAME", "RKV_B"),
                )
            val voruSnapshot =
                snapshot(
                    cityId = voru,
                    stopIds = listOf("SAME", "VORU_B"),
                    patternId = "pattern:VORU",
                    patternStops = listOf("SAME", "VORU_B"),
                    snapshotFeedId = feedVoru,
                )

            importer.import(cityId = rakvere, feedId = feedRakvere, snapshot = rakvereSnapshot)
            importer.import(cityId = voru, feedId = feedVoru, snapshot = voruSnapshot)

            val rakvereLoaded = loader.load(cityId = rakvere, feedId = feedRakvere)
            val voruLoaded = loader.load(cityId = voru, feedId = feedVoru)
            assertNotNull(rakvereLoaded)
            assertNotNull(voruLoaded)

            assertEquals(setOf("SAME", "RKV_B"), rakvereLoaded.stopPoints.map { it.id.value }.toSet())
            assertEquals(setOf("SAME", "VORU_B"), voruLoaded.stopPoints.map { it.id.value }.toSet())
        }

    private fun snapshot(
        cityId: CityId,
        stopIds: List<String>,
        patternId: String,
        patternStops: List<String>,
        displayNames: Map<String, String> = emptyMap(),
        snapshotFeedId: FeedId = feedRakvere,
    ): DomainFeedSnapshot {
        val stopPoints =
            stopIds.map { stopId ->
                StopPoint(
                    id = StopPointId(stopId),
                    stopGroupId = StopGroupId("group:$stopId"),
                    displayName = displayNames[stopId] ?: stopId,
                    location = GeoPoint(59.0 + stopId.length / 100.0, 26.0 + stopId.length / 100.0),
                    cityId = cityId,
                    feedId = snapshotFeedId,
                )
            }

        val routePattern =
            RoutePattern(
                id = RoutePatternId(patternId),
                routeLineId = RouteLineId("line:${patternId.removePrefix("pattern:")}"),
                displayName = patternId,
                cityId = cityId,
                feedId = snapshotFeedId,
                stops =
                    patternStops.mapIndexed { index, stopId ->
                        PatternStop(
                            sequence = index + 1,
                            stopPointId = StopPointId(stopId),
                        )
                    },
            )

        return DomainFeedSnapshot(
            cityId = cityId,
            stopPoints = stopPoints,
            routePatterns = listOf(routePattern),
        )
    }

    private fun runDb(block: suspend () -> Unit) {
        runBlocking(Dispatchers.IO) { block() }
    }
}
