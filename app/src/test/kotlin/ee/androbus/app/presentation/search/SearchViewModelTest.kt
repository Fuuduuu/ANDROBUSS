package ee.androbus.app.presentation.search

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshot
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.routing.DirectRouteNotFoundReason
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.core.routing.DirectRouteSearchResult
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.bridge.DirectRouteSearchPort
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationUseCase
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
        val viewModel = createViewModel(snapshot = null)

        val state = viewModel.uiState.value
        assertEquals(FeedState.NotReady, state.feedState)
        assertEquals(DestinationInputState.Empty, state.destinationInput)
        assertEquals(RouteQueryState.Idle, state.routeQueryState)
    }

    @Test
    fun `refreshFeedState moves to Ready when snapshot exists`() {
        val provider = FakeSnapshotProvider(snapshot = null)
        val viewModel = SearchViewModel(provider, defaultRouteQueryPreparationUseCase())

        provider.snapshot = sampleSnapshot()
        viewModel.refreshFeedState()

        assertEquals(FeedState.Ready, viewModel.uiState.value.feedState)
    }

    @Test
    fun `refreshFeedState stays NotReady when snapshot missing`() {
        val viewModel = createViewModel(snapshot = null)

        viewModel.refreshFeedState()

        assertEquals(FeedState.NotReady, viewModel.uiState.value.feedState)
        assertTrue(viewModel.uiState.value.originCandidates.isEmpty())
    }

    @Test
    fun `originCandidates are non-empty with runtime-like snapshot`() {
        val runtimeSnapshot = runtimeLikeSnapshot()
        val viewModel = createViewModel(runtimeSnapshot)

        val groups = viewModel.uiState.value.originCandidates
        assertTrue(groups.isNotEmpty())
        assertTrue(groups.any { it.displayName == "Rakvere bussijaam" })
        assertTrue(groups.any { it.displayName == "Polikliinik" })
    }

    @Test
    fun `originCandidates stop ids are valid snapshot and route-pattern members`() {
        val runtimeSnapshot = runtimeLikeSnapshot()
        val viewModel = createViewModel(runtimeSnapshot)
        val originStopPointIds = viewModel.uiState.value.originCandidates.flatMap { it.options }.map { it.stopPointId }

        val snapshotStopIds = runtimeSnapshot.stopPoints.map { it.id }.toSet()
        val routePatternStopIds = runtimeSnapshot.routePatterns.flatMap { it.stops }.map { it.stopPointId }.toSet()

        assertTrue(originStopPointIds.isNotEmpty())
        assertTrue(originStopPointIds.all { it in snapshotStopIds })
        assertTrue(originStopPointIds.all { it in routePatternStopIds })
    }

    @Test
    fun `originCandidates do not include old synthetic ids`() {
        val viewModel = createViewModel(runtimeLikeSnapshot())
        val originIds = viewModel.uiState.value.originCandidates.flatMap { it.options }.map { it.stopPointId.value }

        assertFalse(originIds.contains("RKV_A_OUT"))
        assertFalse(originIds.contains("RKV_A_IN"))
        assertFalse(originIds.contains("RKV_B"))
        assertFalse(originIds.contains("RKV_C"))
    }

    @Test
    fun `selecting runtime origin candidate stores originStopPointId`() {
        val viewModel = createViewModel(runtimeLikeSnapshot())
        val option =
            viewModel.uiState.value.originCandidates
                .first { it.displayName == "Polikliinik" }
                .options
                .first()

        viewModel.onOriginStopPointChanged(option.stopPointId)

        assertEquals(option.stopPointId, viewModel.uiState.value.originStopPointId)
    }

    @Test
    fun `searchRoute with runtime origin candidate does not return ORIGIN_NOT_FOUND`() {
        val viewModel = createViewModel(runtimeLikeSnapshot())
        val originCandidate =
            viewModel.uiState.value.originCandidates
                .first { it.displayName == "Rakvere bussijaam" }
                .options
                .first()

        viewModel.onOriginStopPointChanged(originCandidate.stopPointId)
        viewModel.onDestinationChanged("Keskväljak")
        viewModel.searchRoute()

        val routeQueryState = viewModel.uiState.value.routeQueryState
        if (routeQueryState is RouteQueryState.RouteNotFound) {
            assertNotEquals(RouteNotFoundDisplayReason.ORIGIN_NOT_FOUND, routeQueryState.reason)
        }
    }

    @Test
    fun `onDestinationChanged blank sets Empty`() {
        val viewModel = createViewModel(sampleSnapshot())

        viewModel.onDestinationChanged("   ")

        assertEquals(DestinationInputState.Empty, viewModel.uiState.value.destinationInput)
    }

    @Test
    fun `onDestinationChanged Jaam resolves with persisted stop id`() {
        val viewModel = createViewModel(sampleSnapshot())

        viewModel.onDestinationChanged("Jaam")

        val resolved = assertIs<DestinationInputState.Resolved>(viewModel.uiState.value.destinationInput)
        val ids = resolved.candidates.map { it.stopPointId }
        assertTrue(ids.contains(StopPointId("RKV_C")))
        assertFalse(ids.contains(StopPointId("Jaam")))
    }

    @Test
    fun `onDestinationChanged Keskpeatus stays ambiguous with two options`() {
        val viewModel = createViewModel(sampleSnapshot())

        viewModel.onDestinationChanged("Keskpeatus")

        val ambiguous = assertIs<DestinationInputState.Ambiguous>(viewModel.uiState.value.destinationInput)
        val ids = ambiguous.options.map { it.stopPointId }
        assertTrue(ids.contains(StopPointId("RKV_A_OUT")))
        assertTrue(ids.contains(StopPointId("RKV_A_IN")))
        assertTrue(ids.size >= 2)
    }

    @Test
    fun `searchRoute with no origin returns OriginNotProvided not RouteNotFound`() {
        val viewModel = createViewModel(sampleSnapshot())
        viewModel.onDestinationChanged("Jaam")

        viewModel.searchRoute()

        assertEquals(RouteQueryState.OriginNotProvided, viewModel.uiState.value.routeQueryState)
        assertFalse(viewModel.uiState.value.routeQueryState is RouteQueryState.RouteNotFound)
    }

    @Test
    fun `searchRoute with feed null returns FeedNotAvailable not RouteNotFound`() {
        val viewModel = createViewModel(snapshot = null)
        viewModel.onDestinationChanged("Jaam")

        viewModel.searchRoute()

        assertEquals(RouteQueryState.FeedNotAvailable, viewModel.uiState.value.routeQueryState)
        assertFalse(viewModel.uiState.value.routeQueryState is RouteQueryState.RouteNotFound)
    }

    @Test
    fun `searchRoute with ambiguous destination returns DestinationNotReady not RouteNotFound`() {
        val viewModel = createViewModel(sampleSnapshot())
        viewModel.onDestinationChanged("Keskpeatus")
        viewModel.onOriginStopPointChanged(StopPointId("RKV_A_OUT"))

        viewModel.searchRoute()

        assertEquals(RouteQueryState.DestinationNotReady, viewModel.uiState.value.routeQueryState)
        assertFalse(viewModel.uiState.value.routeQueryState is RouteQueryState.RouteNotFound)
    }

    @Test
    fun `searchRoute uses verified candidate stopPointId path not displayName`() {
        val spy = routeSearchSpyUseCase { _, _, _ ->
            DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.NO_DIRECT_PATTERN)
        }
        val viewModel = SearchViewModel(FakeSnapshotProvider(sampleSnapshot()), spy.useCase)
        viewModel.onDestinationChanged("Jaam")
        viewModel.onOriginStopPointChanged(StopPointId("RKV_A_OUT"))

        viewModel.searchRoute()

        assertEquals(1, spy.callCount())
        assertEquals(StopPointId("RKV_C"), spy.lastDestinationStopPointId())
        assertNotEquals(StopPointId("Jaam"), spy.lastDestinationStopPointId())
    }

    @Test
    fun `route query uses DirectRouteQueryPreparationUseCase path`() {
        val spy = routeSearchSpyUseCase { _, _, _ ->
            DirectRouteSearchResult.NotFound(DirectRouteNotFoundReason.SAME_STOP)
        }
        val viewModel = SearchViewModel(FakeSnapshotProvider(sampleSnapshot()), spy.useCase)
        viewModel.onDestinationChanged("Jaam")
        viewModel.onOriginStopPointChanged(StopPointId("RKV_A_OUT"))

        viewModel.searchRoute()

        assertEquals(1, spy.callCount())
        val notFound = assertIs<RouteQueryState.RouteNotFound>(viewModel.uiState.value.routeQueryState)
        assertEquals(RouteNotFoundDisplayReason.SAME_STOP, notFound.reason)
    }

    @Test
    fun `ambiguous destination is never auto selected`() {
        val viewModel = createViewModel(sampleSnapshot())
        viewModel.onDestinationChanged("Keskpeatus")
        viewModel.onOriginStopPointChanged(StopPointId("RKV_A_OUT"))

        val beforeSearch = assertIs<DestinationInputState.Ambiguous>(viewModel.uiState.value.destinationInput)
        assertTrue(beforeSearch.options.size >= 2)

        viewModel.searchRoute()

        assertIs<DestinationInputState.Ambiguous>(viewModel.uiState.value.destinationInput)
        assertEquals(RouteQueryState.DestinationNotReady, viewModel.uiState.value.routeQueryState)
    }

    @Test
    fun `onAmbiguousOptionSelected resolves to selected option only`() {
        val viewModel = createViewModel(sampleSnapshot())
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
        val viewModel = createViewModel(snapshot = null)

        viewModel.onDestinationChanged("Jaam")

        val typed = assertIs<DestinationInputState.Typed>(viewModel.uiState.value.destinationInput)
        assertEquals("Jaam", typed.text)
        assertEquals(FeedState.NotReady, viewModel.uiState.value.feedState)
    }

    private fun sampleSnapshot(
        routePatterns: List<RoutePattern> = sampleRoutePatterns(),
    ): DomainFeedSnapshot =
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
                        id = StopPointId("RKV_B"),
                        stopGroupId = StopGroupId("group:spordikeskus"),
                        displayName = "Spordikeskus",
                        location = GeoPoint(59.3500, 26.3600),
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
            routePatterns = routePatterns,
        )

    private fun sampleRoutePatterns(): List<RoutePattern> =
        listOf(
            RoutePattern(
                id = RoutePatternId("pattern:T1"),
                routeLineId = RouteLineId("route:1"),
                displayName = "T1 Keskpeatus - Jaam",
                cityId = cityId,
                stops =
                    listOf(
                        PatternStop(sequence = 1, stopPointId = StopPointId("RKV_A_OUT")),
                        PatternStop(sequence = 2, stopPointId = StopPointId("RKV_B")),
                        PatternStop(sequence = 3, stopPointId = StopPointId("RKV_C")),
                    ),
                feedId = FeedId("rakvere-bootstrap-v1"),
            ),
            RoutePattern(
                id = RoutePatternId("pattern:T2"),
                routeLineId = RouteLineId("route:2"),
                displayName = "T2 Keskpeatus - Jaam",
                cityId = cityId,
                stops =
                    listOf(
                        PatternStop(sequence = 1, stopPointId = StopPointId("RKV_A_IN")),
                        PatternStop(sequence = 2, stopPointId = StopPointId("RKV_B")),
                        PatternStop(sequence = 3, stopPointId = StopPointId("RKV_C")),
                    ),
                feedId = FeedId("rakvere-bootstrap-v1"),
            ),
        )

    private fun runtimeLikeSnapshot(
        routePatterns: List<RoutePattern> = runtimeLikeRoutePatterns(),
    ): DomainFeedSnapshot =
        DomainFeedSnapshot(
            cityId = cityId,
            stopPoints =
                listOf(
                    StopPoint(
                        id = StopPointId("152898"),
                        stopGroupId = StopGroupId("rakvere-rakvere-bussijaam"),
                        displayName = "Rakvere bussijaam",
                        location = GeoPoint(59.3465663, 26.3647413),
                        cityId = cityId,
                        feedId = FeedId("rakvere-v20260428"),
                    ),
                    StopPoint(
                        id = StopPointId("152899"),
                        stopGroupId = StopGroupId("rakvere-rakvere-bussijaam"),
                        displayName = "Rakvere bussijaam",
                        location = GeoPoint(59.346621, 26.364677),
                        cityId = cityId,
                        feedId = FeedId("rakvere-v20260428"),
                    ),
                    StopPoint(
                        id = StopPointId("25482"),
                        stopGroupId = StopGroupId("rakvere-polikliinik"),
                        displayName = "Polikliinik",
                        location = GeoPoint(59.3445495, 26.3653342),
                        cityId = cityId,
                        feedId = FeedId("rakvere-v20260428"),
                    ),
                    StopPoint(
                        id = StopPointId("25483"),
                        stopGroupId = StopGroupId("rakvere-polikliinik"),
                        displayName = "Polikliinik",
                        location = GeoPoint(59.3445611, 26.3644414),
                        cityId = cityId,
                        feedId = FeedId("rakvere-v20260428"),
                    ),
                    StopPoint(
                        id = StopPointId("109242"),
                        stopGroupId = StopGroupId("rakvere-napi"),
                        displayName = "Näpi",
                        location = GeoPoint(59.3598284, 26.3871624),
                        cityId = cityId,
                        feedId = FeedId("rakvere-v20260428"),
                    ),
                    StopPoint(
                        id = StopPointId("25484"),
                        stopGroupId = StopGroupId("rakvere-keskvaljak"),
                        displayName = "Keskväljak",
                        location = GeoPoint(59.3481224, 26.3611505),
                        cityId = cityId,
                        feedId = FeedId("rakvere-v20260428"),
                    ),
                    StopPoint(
                        id = StopPointId("32583"),
                        stopGroupId = StopGroupId("rakvere-pohja"),
                        displayName = "Põhja",
                        location = GeoPoint(59.3599385, 26.344425),
                        cityId = cityId,
                        feedId = FeedId("rakvere-v20260428"),
                    ),
                ),
            routePatterns = routePatterns,
        )

    private fun runtimeLikeRoutePatterns(): List<RoutePattern> =
        listOf(
            RoutePattern(
                id = RoutePatternId("runtime-pattern:1"),
                routeLineId = RouteLineId("runtime-route:1"),
                displayName = "Rakvere bussijaam - Näpi",
                cityId = cityId,
                stops =
                    listOf(
                        PatternStop(sequence = 1, stopPointId = StopPointId("152898")),
                        PatternStop(sequence = 2, stopPointId = StopPointId("25482")),
                        PatternStop(sequence = 3, stopPointId = StopPointId("25484")),
                        PatternStop(sequence = 4, stopPointId = StopPointId("109242")),
                    ),
                feedId = FeedId("rakvere-v20260428"),
            ),
            RoutePattern(
                id = RoutePatternId("runtime-pattern:2"),
                routeLineId = RouteLineId("runtime-route:2"),
                displayName = "Põhja - Polikliinik",
                cityId = cityId,
                stops =
                    listOf(
                        PatternStop(sequence = 1, stopPointId = StopPointId("32583")),
                        PatternStop(sequence = 2, stopPointId = StopPointId("25483")),
                        PatternStop(sequence = 3, stopPointId = StopPointId("152899")),
                    ),
                feedId = FeedId("rakvere-v20260428"),
            ),
        )

    private fun routeSearchSpyUseCase(
        resultFactory: (origin: StopPointId, destination: StopPointId, patterns: List<RoutePattern>) -> DirectRouteSearchResult,
    ): RouteSearchSpy {
        var calls = 0
        var lastOrigin: StopPointId? = null
        var lastDestination: StopPointId? = null

        val bridge =
            DirectRouteQueryBridge(
                routeSearch =
                    DirectRouteSearchPort { origin, destination, patterns ->
                        calls += 1
                        lastOrigin = origin
                        lastDestination = destination
                        resultFactory(origin, destination, patterns)
                    },
            )

        return RouteSearchSpy(
            useCase = DirectRouteQueryPreparationUseCase(bridge = bridge),
            callCount = { calls },
            lastOriginStopPointId = { lastOrigin },
            lastDestinationStopPointId = { lastDestination },
        )
    }

    private fun defaultRouteQueryPreparationUseCase(): DirectRouteQueryPreparationUseCase =
        DirectRouteQueryPreparationUseCase(
            bridge = DirectRouteQueryBridge(DirectRouteSearch()),
        )

    private fun createViewModel(snapshot: DomainFeedSnapshot?): SearchViewModel =
        SearchViewModel(
            snapshotProvider = FakeSnapshotProvider(snapshot = snapshot),
            routeQueryPreparationUseCase = defaultRouteQueryPreparationUseCase(),
        )

    private data class RouteSearchSpy(
        val useCase: DirectRouteQueryPreparationUseCase,
        val callCount: () -> Int,
        val lastOriginStopPointId: () -> StopPointId?,
        val lastDestinationStopPointId: () -> StopPointId?,
    )

    private class FakeSnapshotProvider(
        var snapshot: DomainFeedSnapshot?,
    ) : DomainFeedSnapshotProvider {
        override fun getSnapshot(cityId: CityId): DomainFeedSnapshot? =
            if (cityId.value == "rakvere") snapshot else null
    }
}
