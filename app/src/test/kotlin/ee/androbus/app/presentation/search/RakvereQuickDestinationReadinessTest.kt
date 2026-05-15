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
    fun `runtime synthetic asset contains only expected synthetic names and excludes proposed labels`() {
        val runtime = loadRuntimeBootstrapDto()
        val runtimeNames = runtime.stopPoints.map { it.displayName }.toSet()

        assertEquals(runtimeSyntheticNames, runtimeNames)
        proposedQuickLabels.forEach { label ->
            assertFalse(runtimeNames.contains(label), "Runtime synthetic asset unexpectedly contains: $label")
        }
    }

    @Test
    fun `destination resolution works for labels present in active snapshot`() {
        val viewModel = createViewModel(loadRuntimeBootstrapDto().toDomainFeedSnapshot())

        viewModel.onDestinationChanged("Jaam")
        assertIs<DestinationInputState.Resolved>(viewModel.uiState.value.destinationInput)

        viewModel.onDestinationChanged("Keskpeatus")
        val ambiguous = assertIs<DestinationInputState.Ambiguous>(viewModel.uiState.value.destinationInput)
        assertTrue(ambiguous.options.size >= 2)
    }

    @Test
    fun `destination resolution does not resolve labels absent from active runtime snapshot`() {
        val viewModel = createViewModel(loadRuntimeBootstrapDto().toDomainFeedSnapshot())

        proposedQuickLabels.forEach { label ->
            viewModel.onDestinationChanged(label)
            assertIs<DestinationInputState.NotFound>(
                viewModel.uiState.value.destinationInput,
                "Proposed label should not resolve in runtime synthetic snapshot: $label",
            )
        }
    }

    @Test
    fun `real derived dev profile supports real labels but stays separate from runtime default`() {
        val runtime = loadRuntimeBootstrapDto()
        val devProfile = loadDevProfileDto()
        val devNames = devProfile.stopPoints.map { it.displayName }.toSet()

        assertEquals("rakvere-bootstrap-v1", runtime.feedId)
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

    private fun loadRuntimeBootstrapDto(): BootstrapFeedDto =
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
