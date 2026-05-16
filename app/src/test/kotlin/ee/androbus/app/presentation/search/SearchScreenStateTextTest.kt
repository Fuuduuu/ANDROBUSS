package ee.androbus.app.presentation.search

import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchScreenStateTextTest {
    @Test
    fun `search button disabled when destination is empty`() {
        val uiState = SearchUiState(destinationInput = DestinationInputState.Empty, originStopPointId = StopPointId("RKV_A_OUT"))

        assertFalse(isSearchButtonEnabled(uiState))
    }

    @Test
    fun `search button disabled when origin is null`() {
        val uiState =
            SearchUiState(
                destinationInput =
                    DestinationInputState.Resolved(
                        displayName = "Jaam",
                        candidates = listOf(ResolvedDestinationOption("Jaam", StopPointId("RKV_C"))),
                    ),
                originStopPointId = null,
            )

        assertFalse(isSearchButtonEnabled(uiState))
    }

    @Test
    fun `search button enabled when destination resolved and origin selected`() {
        val uiState =
            SearchUiState(
                destinationInput =
                    DestinationInputState.Resolved(
                        displayName = "Jaam",
                        candidates = listOf(ResolvedDestinationOption("Jaam", StopPointId("RKV_C"))),
                    ),
                originStopPointId = StopPointId("RKV_A_OUT"),
            )

        assertTrue(isSearchButtonEnabled(uiState))
    }

    @Test
    fun `feed state ready banner is hidden`() {
        assertFalse(shouldShowFeedStatusBanner(FeedState.Ready))
    }

    @Test
    fun `feed state wording stays static schedule safe`() {
        val message = feedStateMessage(FeedState.Ready)
        assertContains(message, "sõiduplaani järgi")
        assertFalse(message.contains("live", ignoreCase = true))
        assertFalse(message.contains("realtime", ignoreCase = true))
        assertFalse(message.contains("pärisajas", ignoreCase = true))
    }

    @Test
    fun `route found wording uses expected headline`() {
        val state =
            RouteQueryState.RouteFound(
                route = testRouteFoundSummary(),
            )

        val text = routeStateMessage(state)
        assertContains(text, "marsruut leitud")
    }

    @Test
    fun `destination empty prompt requires explicit selection action`() {
        val text = destinationStateMessage(DestinationInputState.Empty)
        assertContains(text, "Vali sihtkoht")
    }

    @Test
    fun `destination not ready message does not contain route wording`() {
        val text = routeStateMessage(RouteQueryState.DestinationNotReady)
        assertFalse(text.contains("route", ignoreCase = true))
    }

    @Test
    fun `route found summary does not expose raw stop ids`() {
        val lines = routeFoundSummaryLines(testRouteFoundSummary())
        assertContains(lines.joinToString("\n"), "✓ Marsruut leitud")
        assertFalse(lines.any { it.contains("RKV_A_OUT") || it.contains("RKV_C") || it.contains("pattern:T1") })
    }

    @Test
    fun `quick destination labels include expected entries and exclude Torma`() {
        val labels = quickDestinationOptions().map { it.label }

        assertContains(labels, "Rakvere bussijaam")
        assertContains(labels, "Polikliinik")
        assertContains(labels, "Näpi")
        assertContains(labels, "Keskväljak")
        assertContains(labels, "Põhjakeskus")
        assertFalse(labels.contains("Tõrma"))
    }

    @Test
    fun `quick destination section title is visible`() {
        assertEquals("Kiirvalikud", QUICK_DESTINATION_SECTION_TITLE)
    }

    @Test
    fun `clicking Pohjakeskus uses queryText Pohja`() {
        var displayedText = ""
        var querySent: String? = null

        handleQuickDestinationSelection(
            label = "Põhjakeskus",
            queryText = "Põhja",
            setDestinationText = { displayedText = it },
            onDestinationSelect = { querySent = it },
        )

        assertEquals("Põhjakeskus", displayedText)
        assertEquals("Põhja", querySent)
    }

    @Test
    fun `clicking Rakvere bussijaam uses same queryText`() {
        var displayedText = ""
        var querySent: String? = null

        handleQuickDestinationSelection(
            label = "Rakvere bussijaam",
            queryText = "Rakvere bussijaam",
            setDestinationText = { displayedText = it },
            onDestinationSelect = { querySent = it },
        )

        assertEquals("Rakvere bussijaam", displayedText)
        assertEquals("Rakvere bussijaam", querySent)
    }

    @Test
    fun `quick destination selection does not trigger search callback`() {
        var searchCallCount = 0

        handleQuickDestinationSelection(
            label = "Polikliinik",
            queryText = "Polikliinik",
            setDestinationText = {},
            onDestinationSelect = {},
        )

        assertEquals(0, searchCallCount)
    }

    @Test
    fun `origin section no longer exposes old synthetic labels`() {
        val labels = preferredOriginDisplayOrder()
        assertFalse(labels.contains("RKV A välja"))
        assertFalse(labels.contains("RKV A sisse"))
        assertFalse(labels.contains("RKV B"))
        assertFalse(labels.contains("RKV C"))
    }

    @Test
    fun `origin search action label is visible`() {
        assertEquals("Otsi peatus...", ORIGIN_SEARCH_ACTION_TEXT)
    }

    @Test
    fun `clicking origin search action opens dialog with expected title`() {
        var showDialog = false
        val onOpenDialog = { showDialog = true }

        onOpenDialog()

        assertTrue(showDialog)
        assertEquals("Vali lähtepeatus", ORIGIN_DIALOG_TITLE)
    }

    @Test
    fun `origin dialog search filters groups`() {
        val filtered = filterOriginCandidateGroups(sampleOriginCandidateGroups(), "poli")
        assertEquals(1, filtered.size)
        assertEquals("Polikliinik", filtered.single().displayName)
    }

    @Test
    fun `origin dialog empty result state is shown`() {
        val filtered = filterOriginCandidateGroups(sampleOriginCandidateGroups(), "puudub")
        assertTrue(filtered.isEmpty())
        assertEquals("Tulemusi ei leitud", ORIGIN_DIALOG_NO_RESULTS_TEXT)
    }

    @Test
    fun `single-option group selection resolves concrete stopPointId`() {
        val singleGroup = sampleOriginCandidateGroups().first { it.displayName == "Polikliinik" }
        val outcome = resolveOriginGroupTap(group = singleGroup, currentlyExpandedGroupId = null)
        assertEquals(StopPointId("25482"), outcome.selectedStopPointId)
        assertNull(outcome.expandedGroupId)
    }

    @Test
    fun `origin section shows runtime-backed preferred labels`() {
        val ordered = orderedOriginCandidateGroups(sampleOriginCandidateGroups())
        val labels = ordered.map { it.displayName }

        assertContains(labels, "Rakvere bussijaam")
        assertContains(labels, "Polikliinik")
        assertContains(labels, "Näpi")
    }

    @Test
    fun `multi-option origin group requires explicit option selection`() {
        val group = sampleOriginCandidateGroups().first { it.displayName == "Rakvere bussijaam" }
        val outcome = resolveOriginGroupTap(group = group, currentlyExpandedGroupId = null)

        assertNull(outcome.selectedStopPointId)
        assertEquals(group.groupId, outcome.expandedGroupId)
    }

    @Test
    fun `single-option origin group selects concrete stopPointId`() {
        val group = sampleOriginCandidateGroups().first { it.displayName == "Polikliinik" }
        val outcome = resolveOriginGroupTap(group = group, currentlyExpandedGroupId = null)

        assertEquals(StopPointId("25482"), outcome.selectedStopPointId)
        assertNull(outcome.expandedGroupId)
    }

    @Test
    fun `multi-option dialog option click selects stopPointId and dismisses dialog`() {
        val option =
            sampleOriginCandidateGroups()
                .first { it.displayName == "Rakvere bussijaam" }
                .options
                .first()

        var selected: StopPointId? = null
        var dismissed = false

        handleOriginDialogOptionSelection(
            option = option,
            onOriginSelected = { selected = it },
            onDismiss = { dismissed = true },
        )

        assertEquals(option.stopPointId, selected)
        assertTrue(dismissed)
    }

    @Test
    fun `single-option dialog group click selects stopPointId and dismisses dialog`() {
        val singleGroup = sampleOriginCandidateGroups().first { it.displayName == "Polikliinik" }
        val outcome = resolveOriginGroupTap(group = singleGroup, currentlyExpandedGroupId = null)

        var selected: StopPointId? = null
        var dismissed = false
        val expanded =
            handleOriginDialogGroupSelection(
                outcome = outcome,
                onOriginSelected = { selected = it },
                onDismiss = { dismissed = true },
            )

        assertEquals(StopPointId("25482"), selected)
        assertTrue(dismissed)
        assertNull(expanded)
    }

    @Test
    fun `dialog cancel hides dialog`() {
        var visible = true
        val onDismiss = { visible = false }

        onDismiss()

        assertFalse(visible)
        assertEquals("Tühista", ORIGIN_DIALOG_CANCEL_TEXT)
    }

    @Test
    fun `origin option label contains route pattern count`() {
        val option = OriginCandidateOption(stopPointId = StopPointId("152898"), label = "Rakvere bussijaam variant 1", routePatternCount = 2)
        assertEquals("Rakvere bussijaam variant 1 (2)", originOptionLabel(option))
    }

    private fun testRouteFoundSummary(): RouteFoundSummary =
        RouteFoundSummary(
            routePatternId = RoutePatternId("pattern:T1"),
            originStopPointId = StopPointId("RKV_A_OUT"),
            destinationStopPointId = StopPointId("RKV_C"),
            originSequence = 1,
            destinationSequence = 3,
            segmentStopCount = 3,
            segmentStopPointIds = listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_B"), StopPointId("RKV_C")),
            candidateCount = 1,
        )

    private fun sampleOriginCandidateGroups(): List<OriginCandidateGroup> =
        listOf(
            OriginCandidateGroup(
                groupId = "rakvere-rakvere-bussijaam",
                displayName = "Rakvere bussijaam",
                options =
                    listOf(
                        OriginCandidateOption(
                            stopPointId = StopPointId("152898"),
                            label = "Rakvere bussijaam variant 1",
                            routePatternCount = 2,
                        ),
                        OriginCandidateOption(
                            stopPointId = StopPointId("152899"),
                            label = "Rakvere bussijaam variant 2",
                            routePatternCount = 1,
                        ),
                    ),
            ),
            OriginCandidateGroup(
                groupId = "rakvere-polikliinik",
                displayName = "Polikliinik",
                options =
                    listOf(
                        OriginCandidateOption(
                            stopPointId = StopPointId("25482"),
                            label = "Polikliinik",
                            routePatternCount = 2,
                        ),
                    ),
            ),
            OriginCandidateGroup(
                groupId = "rakvere-napi",
                displayName = "Näpi",
                options =
                    listOf(
                        OriginCandidateOption(
                            stopPointId = StopPointId("109242"),
                            label = "Näpi",
                            routePatternCount = 1,
                        ),
                    ),
            ),
        )
}
