package ee.androbus.app.presentation.search

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SearchViewModelTest {
    private val cityId = CityId("rakvere")

    @Test
    fun `initial state is FeedNotReady and Empty`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(snapshot = null))

        val state = viewModel.uiState.value
        assertEquals(FeedState.NotReady, state.feedState)
        assertEquals(DestinationInputState.Empty, state.destinationInput)
    }

    @Test
    fun `refreshFeedState moves to Ready when snapshot exists`() {
        val provider = FakeSnapshotProvider(snapshot = null)
        val viewModel = SearchViewModel(provider)

        provider.snapshot = sampleSnapshot()
        viewModel.refreshFeedState()

        assertEquals(FeedState.Ready, viewModel.uiState.value.feedState)
    }

    @Test
    fun `refreshFeedState stays NotReady when snapshot missing`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(snapshot = null))

        viewModel.refreshFeedState()

        assertEquals(FeedState.NotReady, viewModel.uiState.value.feedState)
    }

    @Test
    fun `onDestinationChanged blank sets Empty`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(sampleSnapshot()))

        viewModel.onDestinationChanged("   ")

        assertEquals(DestinationInputState.Empty, viewModel.uiState.value.destinationInput)
    }

    @Test
    fun `onDestinationChanged Jaam resolves with persisted stop id`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(sampleSnapshot()))

        viewModel.onDestinationChanged("Jaam")

        val resolved = assertIs<DestinationInputState.Resolved>(viewModel.uiState.value.destinationInput)
        val ids = resolved.candidates.map { it.stopPointId }
        assertTrue(ids.contains(StopPointId("RKV_C")))
        assertFalse(ids.contains(StopPointId("Jaam")))
    }

    @Test
    fun `onDestinationChanged Keskpeatus stays ambiguous with two options`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(sampleSnapshot()))

        viewModel.onDestinationChanged("Keskpeatus")

        val ambiguous = assertIs<DestinationInputState.Ambiguous>(viewModel.uiState.value.destinationInput)
        val ids = ambiguous.options.map { it.stopPointId }
        assertTrue(ids.contains(StopPointId("RKV_A_OUT")))
        assertTrue(ids.contains(StopPointId("RKV_A_IN")))
        assertTrue(ids.size >= 2)
    }

    @Test
    fun `onAmbiguousOptionSelected resolves to selected option only`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(sampleSnapshot()))
        viewModel.onDestinationChanged("Keskpeatus")
        val ambiguous = assertIs<DestinationInputState.Ambiguous>(viewModel.uiState.value.destinationInput)
        val selected = ambiguous.options.first { it.stopPointId == StopPointId("RKV_A_IN") }

        viewModel.onAmbiguousOptionSelected(selected)

        val resolved = assertIs<DestinationInputState.Resolved>(viewModel.uiState.value.destinationInput)
        assertEquals(1, resolved.candidates.size)
        assertEquals(StopPointId("RKV_A_IN"), resolved.candidates.single().stopPointId)
    }

    @Test
    fun `when feed not ready destination change stays typed and does not fabricate id`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(snapshot = null))

        viewModel.onDestinationChanged("Jaam")

        val typed = assertIs<DestinationInputState.Typed>(viewModel.uiState.value.destinationInput)
        assertEquals("Jaam", typed.text)
        assertEquals(FeedState.NotReady, viewModel.uiState.value.feedState)
    }

    @Test
    fun `anti fabrication keeps stop id from resolver not display name`() {
        val viewModel = SearchViewModel(FakeSnapshotProvider(sampleSnapshot()))

        viewModel.onDestinationChanged("Jaam")

        val resolved = assertIs<DestinationInputState.Resolved>(viewModel.uiState.value.destinationInput)
        val stopPointId = resolved.candidates.single().stopPointId
        assertEquals(StopPointId("RKV_C"), stopPointId)
        assertNotEquals(StopPointId("Jaam"), stopPointId)
    }

    @Test
    fun `SearchViewModel does not reference direct route query classes`() {
        val source = loadSearchViewModelSource()

        assertFalse(source.contains("DirectRouteQueryPreparationUseCase"))
        assertFalse(source.contains("DirectRouteQueryBridge"))
    }

    private fun sampleSnapshot(): DomainFeedSnapshot =
        DomainFeedSnapshot(
            cityId = cityId,
            stopPoints =
                listOf(
                    StopPoint(
                        id = StopPointId("RKV_A_OUT"),
                        stopGroupId = StopGroupId("group:keskpeatus-out"),
                        displayName = "Keskpeatus",
                        location = GeoPoint(59.3461, 26.3552),
                        cityId = cityId,
                        feedId = FeedId("rakvere-bootstrap-v1"),
                    ),
                    StopPoint(
                        id = StopPointId("RKV_A_IN"),
                        stopGroupId = StopGroupId("group:keskpeatus-in"),
                        displayName = "Keskpeatus",
                        location = GeoPoint(59.3463, 26.3555),
                        cityId = cityId,
                        feedId = FeedId("rakvere-bootstrap-v1"),
                    ),
                    StopPoint(
                        id = StopPointId("RKV_C"),
                        stopGroupId = StopGroupId("group:jaam"),
                        displayName = "Jaam",
                        location = GeoPoint(59.3550, 26.3650),
                        cityId = cityId,
                        feedId = FeedId("rakvere-bootstrap-v1"),
                    ),
                ),
            routePatterns = emptyList(),
        )

    private fun loadSearchViewModelSource(): String {
        val candidates =
            listOf(
                Path.of("src", "main", "kotlin", "ee", "androbus", "app", "presentation", "search", "SearchViewModel.kt"),
                Path.of("app", "src", "main", "kotlin", "ee", "androbus", "app", "presentation", "search", "SearchViewModel.kt"),
            )
        val path = candidates.firstOrNull { Files.exists(it) }
        require(path != null) { "SearchViewModel source file not found." }
        return Files.newBufferedReader(path).use { it.readText() }
    }

    private class FakeSnapshotProvider(
        var snapshot: DomainFeedSnapshot?,
    ) : DomainFeedSnapshotProvider {
        override fun getSnapshot(cityId: CityId): DomainFeedSnapshot? =
            if (cityId.value == "rakvere") snapshot else null
    }
}
