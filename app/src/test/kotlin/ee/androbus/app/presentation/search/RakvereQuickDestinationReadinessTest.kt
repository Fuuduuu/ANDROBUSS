package ee.androbus.app.presentation.search

import ee.androbus.app.bootstrap.BootstrapFeedDto
import ee.androbus.app.bootstrap.toDomainFeedSnapshot
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationUseCase
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RakvereQuickDestinationReadinessTest {
    private val cityId = CityId("rakvere")
    private val runtimeSyntheticNames = setOf("Keskpeatus", "Spordikeskus", "Jaam")
    private val proposedQuickLabels =
        listOf(
            "Rakvere bussijaam",
            "Polikliinik",
            "Põhjakeskus",
            "Näpi",
            "Keskväljak",
            "Tõrma",
        )

    @Test
    fun `runtime real static asset contains expected Rakvere labels`() {
        val runtime = loadRuntimeRealBootstrapDto()
        val runtimeNames = runtime.stopPoints.map { it.displayName }.toSet()

        assertEquals("rakvere-v20260428", runtime.feedId)
        assertTrue(runtimeNames.contains("Rakvere bussijaam"))
        assertTrue(runtimeNames.contains("Polikliinik"))
        assertTrue(runtimeNames.contains("Näpi"))
        assertTrue(runtimeNames.contains("Keskväljak"))
    }

    @Test
    fun `destination resolution works for labels present in real runtime-like snapshot`() {
        val viewModel = createViewModel(loadRuntimeRealBootstrapDto().toDomainFeedSnapshot())

        viewModel.onDestinationChanged("Rakvere bussijaam")
        val state = viewModel.uiState.value.destinationInput
        val resolvedStopIds =
            when (state) {
                is DestinationInputState.Resolved -> state.candidates.map { it.stopPointId }
                is DestinationInputState.Ambiguous -> state.options.map { it.stopPointId }
                else -> emptyList()
            }

        assertTrue(
            state is DestinationInputState.Resolved || state is DestinationInputState.Ambiguous,
            "Expected resolvable destination state for Rakvere bussijaam, got $state",
        )
        assertTrue(resolvedStopIds.isNotEmpty())
        assertFalse(resolvedStopIds.any { it == ee.androbus.core.domain.StopPointId("Rakvere bussijaam") })
    }

    @Test
    fun `destination resolution does not resolve labels absent from active runtime snapshot`() {
        val viewModel = createViewModel(loadRuntimeRealBootstrapDto().toDomainFeedSnapshot())

        viewModel.onDestinationChanged("Seda peatust ei ole")
        assertIs<DestinationInputState.NotFound>(viewModel.uiState.value.destinationInput)
    }

    @Test
    fun `real derived dev profile stays separate from runtime default identity`() {
        val runtime = loadRuntimeRealBootstrapDto()
        val devProfile = loadDevProfileDto()
        val devNames = devProfile.stopPoints.map { it.displayName }.toSet()

        assertEquals("rakvere-v20260428", runtime.feedId)
        assertTrue(devProfile.feedId.contains("dev", ignoreCase = true) || devProfile.feedId.contains("profile", ignoreCase = true))
        assertNotEquals(runtime.feedId, devProfile.feedId)

        assertTrue(devNames.contains("Rakvere bussijaam"))
        assertTrue(devNames.contains("Polikliinik"))
        assertTrue(devNames.contains("Näpi"))
        assertTrue(devNames.contains("Keskväljak"))
    }

    private fun createViewModel(snapshot: DomainFeedSnapshot): SearchViewModel =
        SearchViewModel(
            snapshotProvider = FakeSnapshotProvider(snapshot = snapshot),
            routeQueryPreparationUseCase = defaultRouteQueryPreparationUseCase(),
        )

    private fun defaultRouteQueryPreparationUseCase(): DirectRouteQueryPreparationUseCase =
        DirectRouteQueryPreparationUseCase(
            bridge = DirectRouteQueryBridge(DirectRouteSearch()),
        )

    @Test
    fun `synthetic fallback asset remains synthetic when loaded explicitly`() {
        val synthetic = loadSyntheticBootstrapDto()
        val syntheticNames = synthetic.stopPoints.map { it.displayName }.toSet()

        assertEquals("rakvere-bootstrap-v1", synthetic.feedId)
        assertEquals(runtimeSyntheticNames, syntheticNames)
        proposedQuickLabels.forEach { label ->
            assertFalse(syntheticNames.contains(label), "Synthetic fallback unexpectedly contains: $label")
        }
    }

    @Test
    fun `runtime real static stop ids come from id fields not label text`() {
        val runtime = loadRuntimeRealBootstrapDto()
        val snapshot = runtime.toDomainFeedSnapshot()
        val ids = snapshot.stopPoints.map { it.id.value }.toSet()

        assertTrue(ids.contains("152898"))
        assertFalse(ids.contains("Jaam"))
        assertFalse(ids.contains("Rakvere bussijaam"))
        assertFalse(ids.contains("Polikliinik"))
    }

    private fun loadRuntimeRealBootstrapDto(): BootstrapFeedDto =
        decodeDto(
            readRepoFile(
                listOf(
                    "app/src/main/assets/bootstrap/rakvere_feed_20260428.json",
                    "src/main/assets/bootstrap/rakvere_feed_20260428.json",
                ),
            ),
        )

    private fun loadSyntheticBootstrapDto(): BootstrapFeedDto =
        decodeDto(
            readRepoFile(
                listOf(
                    "app/src/main/assets/bootstrap/rakvere_bootstrap.json",
                    "src/main/assets/bootstrap/rakvere_bootstrap.json",
                ),
            ),
        )

    private fun loadDevProfileDto(): BootstrapFeedDto {
        val fromClasspath =
            javaClass.classLoader
                .getResourceAsStream("bootstrap/rakvere_dev_profile_v1.json")
                ?.bufferedReader()
                ?.use { it.readText() }

        val text =
            fromClasspath
                ?: readRepoFile(
                    listOf(
                        "app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json",
                        "src/test/resources/bootstrap/rakvere_dev_profile_v1.json",
                    ),
                )

        return decodeDto(text)
    }

    private fun decodeDto(text: String): BootstrapFeedDto = Json.decodeFromString(text)

    private fun readRepoFile(candidates: List<String>): String {
        candidates.forEach { candidate ->
            val path = Path.of(candidate)
            if (Files.exists(path)) {
                return path.toFile().readText()
            }
        }
        error("None of the candidate files exist: ${candidates.joinToString()}")
    }

    private class FakeSnapshotProvider(
        private val snapshot: DomainFeedSnapshot,
    ) : DomainFeedSnapshotProvider {
        override fun getSnapshot(cityId: CityId): DomainFeedSnapshot? =
            if (cityId.value == "rakvere") snapshot else null
    }
}
