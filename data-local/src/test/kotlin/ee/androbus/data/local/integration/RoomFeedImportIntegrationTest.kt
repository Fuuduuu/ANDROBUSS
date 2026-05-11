package ee.androbus.data.local.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.gtfs.GtfsDomainMapper
import ee.androbus.core.gtfs.GtfsFeedParser
import ee.androbus.core.gtfs.MappedGtfsFeed
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.data.local.database.AppDatabase
import ee.androbus.data.local.importer.FeedSnapshotImporter
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotLoader
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotProvider
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.bridge.DirectRouteQueryBridgeResult
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import ee.androbus.feature.search.orchestration.DestinationEnrichmentOrchestrator
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationResult
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationUseCase
import ee.androbus.feature.search.resolution.InMemoryStopPointIndex
import ee.androbus.feature.search.resolution.StopCandidateEnricher
import ee.androbus.feature.search.resolution.StopPointResolutionInput
import ee.androbus.feature.search.resolution.StopPointResolutionResult
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class RoomFeedImportIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var importer: FeedSnapshotImporter
    private lateinit var provider: RoomDomainFeedSnapshotProvider

    private val cityId = CityId("rakvere")
    private val feedId = FeedId("rakvere-smoke")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
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
    fun `parser to Room to provider roundtrip feeds search pipeline`() =
        runDb {
            val mappedFeed = mappedFeed()
            val snapshot =
                DomainFeedSnapshot(
                    cityId = cityId,
                    stopPoints = mappedFeed.stopPoints,
                    routePatterns = mappedFeed.routePatterns,
                )

            importer.import(cityId = cityId, feedId = feedId, snapshot = snapshot)
            provider.prepare(cityId = cityId, feedId = feedId)

            val loaded = provider.getSnapshot(cityId)
            assertNotNull(loaded)

            val loadedStopIds = loaded.stopPoints.map { it.id }.toSet()
            assertTrue(loadedStopIds.contains(StopPointId("RKV_A_OUT")))
            assertTrue(loadedStopIds.contains(StopPointId("RKV_A_IN")))
            assertTrue(loadedStopIds.contains(StopPointId("RKV_B")))
            assertTrue(loadedStopIds.contains(StopPointId("RKV_C")))

            val loadedPatternIds = loaded.routePatterns.map { it.id.value }.toSet()
            assertTrue(loadedPatternIds.contains("pattern:T1"))
            assertTrue(loadedPatternIds.contains("pattern:T3"))

            val index = InMemoryStopPointIndex(loaded.stopPoints)
            val resolved = assertIs<StopPointResolutionResult.Resolved>(index.resolve(StopPointResolutionInput("Jaam", cityId)))
            val resolvedStopPointId = resolved.candidates.single().stopPointId
            assertEquals(StopPointId("RKV_C"), resolvedStopPointId)
            assertNotEquals(StopPointId("Jaam"), resolvedStopPointId)

            val destinationEnrichment =
                DestinationEnrichmentOrchestrator(StopCandidateEnricher(index))
                    .enrichCandidates(candidates = listOf(candidate("Jaam")), cityId = cityId)

            val routeResult =
                DirectRouteQueryPreparationUseCase(DirectRouteQueryBridge(DirectRouteSearch()))
                    .prepare(
                        destinationEnrichment = destinationEnrichment,
                        originStopPointId = StopPointId("RKV_A_OUT"),
                        patterns = loaded.routePatterns,
                    )

            val executed = assertIs<DirectRouteQueryPreparationResult.Executed>(routeResult)
            assertIs<DirectRouteQueryBridgeResult.RouteFound>(executed.bridgeResult)
        }

    private fun mappedFeed(): MappedGtfsFeed {
        val parser = GtfsFeedParser()
        val mapper = GtfsDomainMapper()
        val parsed = parser.parseDirectory(smokeFixtureDirectory())
        return mapper.map(
            parsedFeed = parsed,
            cityId = cityId,
            feedId = feedId,
        )
    }

    private fun smokeFixtureDirectory(): Path {
        val candidates =
            listOf(
                Path.of("core-gtfs", "src", "test", "resources", "gtfs", "rakvere-smoke"),
                Path.of("..", "core-gtfs", "src", "test", "resources", "gtfs", "rakvere-smoke"),
            )

        return candidates
            .map { it.toAbsolutePath().normalize() }
            .firstOrNull { Files.isDirectory(it) }
            ?: error("Unable to find core-gtfs rakvere-smoke fixture directory from ${Path.of("").toAbsolutePath()}.")
    }

    private fun candidate(stopGroupName: String): StopCandidate =
        StopCandidate(
            targetId = "room:$stopGroupName",
            stopGroupName = stopGroupName,
            source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "PASS 23 parser->Room integration candidate.",
            stopPointIds = emptyList(),
        )

    private fun runDb(block: suspend () -> Unit) {
        runBlocking(Dispatchers.IO) { block() }
    }
}
