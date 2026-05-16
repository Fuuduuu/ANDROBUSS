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
        val uiState = SearchUiState(destinationInput = DestinationInputState.Empty, originStopPointId = StopPointId("152898"))

        assertFalse(
            isSearchButtonEnabled(
                uiState = uiState,
                destinationText = "",
                lastResolvedDestinationText = null,
            ),
        )
    }

    @Test
    fun `search button disabled when origin is null`() {
        val uiState =
            SearchUiState(
                destinationInput =
                    DestinationInputState.Resolved(
                        displayName = "Jaam",
                        candidates = listOf(ResolvedDestinationOption("Jaam", StopPointId("153420"))),
                    ),
                originStopPointId = null,
            )

        assertFalse(
            isSearchButtonEnabled(
                uiState = uiState,
                destinationText = "Jaam",
                lastResolvedDestinationText = "Jaam",
            ),
        )
    }

    @Test
    fun `search button enabled when destination resolved origin selected and text is unchanged`() {
        val uiState =
            SearchUiState(
                destinationInput =
                    DestinationInputState.Resolved(
                        displayName = "Jaam",
                        candidates = listOf(ResolvedDestinationOption("Jaam", StopPointId("153420"))),
                    ),
                originStopPointId = StopPointId("152898"),
            )

        assertTrue(
            isSearchButtonEnabled(
                uiState = uiState,
                destinationText = "Jaam",
                lastResolvedDestinationText = "Jaam",
            ),
        )
    }

    @Test
    fun `editing destination after resolved disables search until destination is selected again`() {
        val uiState =
            SearchUiState(
                destinationInput =
                    DestinationInputState.Resolved(
                        displayName = "Jaam",
                        candidates = listOf(ResolvedDestinationOption("Jaam", StopPointId("153420"))),
                    ),
                originStopPointId = StopPointId("152898"),
            )

        assertTrue(isSearchButtonEnabled(uiState, destinationText = "Jaam", lastResolvedDestinationText = "Jaam"))
        assertFalse(isSearchButtonEnabled(uiState, destinationText = "Jaam uus", lastResolvedDestinationText = "Jaam"))
        assertTrue(isSearchButtonEnabled(uiState, destinationText = "Jaam uus", lastResolvedDestinationText = "Jaam uus"))
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
    fun `destination empty prompt requires explicit selection action`() {
        val text = destinationStateMessage(DestinationInputState.Empty)
        assertContains(text, "Vali sihtkoht")
    }

    @Test
    fun `route destination not ready message is rider friendly`() {
        val text = routeStateMessage(RouteQueryState.DestinationNotReady)
        assertEquals("Vali esmalt sihtkoht.", text)
    }

    @Test
    fun `route no patterns message is rider friendly`() {
        val text = routeStateMessage(RouteQueryState.NoPatternsAvailable)
        assertEquals("Marsruudiandmed pole saadaval.", text)
    }

    @Test
    fun `route not found message is rider friendly`() {
        val text =
            routeStateMessage(
                RouteQueryState.RouteNotFound(
                    reason = RouteNotFoundDisplayReason.NO_DIRECT_PATTERN,
                ),
            )
        assertEquals("Otsemarsruuti ei leitud valitud peatuste vahel.", text)
    }

    @Test
    fun `route found summary does not expose raw stop ids`() {
        val lines = routeFoundSummaryLines(testRouteFoundSummary())
        assertContains(lines.joinToString("\n"), "✓ Marsruut leitud")
        assertFalse(lines.any { it.contains("152898") || it.contains("153420") || it.contains("pattern:T1") })
    }

    @Test
    fun `quick destination labels include expected entries and exclude Torma`() {
        val labels = quickDestinationOptions().map { it.label }

        assertContains(labels, "Rakvere bussijaam")
        assertContains(labels, "Polikliinik")
        assertContains(labels, "Näpi")
        assertContains(labels, "Keskväljak")
        assertContains(labels, "Põhjakeskus (Põhja)")
        assertFalse(labels.contains("Tõrma"))
    }

    @Test
    fun `quick destination section title is visible`() {
        assertEquals("Kiirvalikud", QUICK_DESTINATION_SECTION_TITLE)
    }

    @Test
    fun `quick destination helper text explains separate route search`() {
        assertContains(QUICK_DESTINATION_HELPER_TEXT, "vajuta „Otsi“")
    }

    @Test
    fun `Pohjakeskus label clarifies Pohja relationship`() {
        val option = quickDestinationOptions().first { it.queryText == "Põhja" }
        assertContains(option.label, "Põhja")
        assertContains(QUICK_DESTINATION_ALIAS_HELPER_TEXT, "Põhja")
    }

    @Test
    fun `quick destination section keeps resolver callback path and does not trigger route search`() {
        var displayedText = ""
        var destinationCallbackCount = 0
        var destinationQuery: String? = null
        var routeSearchCallCount = 0

        handleQuickDestinationSelection(
            label = "Rakvere bussijaam",
            queryText = "Rakvere bussijaam",
            setDestinationText = { displayedText = it },
            onDestinationSelect = {
                destinationCallbackCount += 1
                destinationQuery = it
            },
        )

        assertEquals("Rakvere bussijaam", displayedText)
        assertEquals(1, destinationCallbackCount)
        assertEquals("Rakvere bussijaam", destinationQuery)
        assertEquals(0, routeSearchCallCount)
    }

    @Test
    fun `destination section is ordered before quick destination section`() {
        val sections = searchContentSectionOrder()
        assertTrue(sections.indexOf(SearchContentSection.Destination) < sections.indexOf(SearchContentSection.QuickDestinations))
    }

    @Test
    fun `only preferred origin groups are shown inline`() {
        val inlineLabels = inlinePreferredOriginCandidateGroups(sampleOriginCandidateGroups()).map { it.displayName }
        assertContains(inlineLabels, "Rakvere bussijaam")
        assertContains(inlineLabels, "Polikliinik")
        assertContains(inlineLabels, "Näpi")
        assertFalse(inlineLabels.contains("Kooli"))
    }

    @Test
    fun `full origin catalog remains available for dialog`() {
        val inlineLabels = inlinePreferredOriginCandidateGroups(sampleOriginCandidateGroups()).map { it.displayName }
        val dialogLabels = filterOriginCandidateGroups(sampleOriginCandidateGroups(), "").map { it.displayName }

        assertFalse(inlineLabels.contains("Kooli"))
        assertContains(dialogLabels, "Kooli")
    }

    @Test
    fun `origin section no longer exposes old synthetic labels`() {
        val labels = preferredOriginDisplayOrder()
        assertFalse(labels.contains("RKV_A_OUT"))
        assertFalse(labels.contains("RKV_A_IN"))
        assertFalse(labels.contains("RKV_B"))
        assertFalse(labels.contains("RKV_C"))
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
    fun `single option group selection resolves concrete stopPointId`() {
        val singleGroup = sampleOriginCandidateGroups().first { it.displayName == "Polikliinik" }
        val outcome = resolveOriginGroupTap(group = singleGroup, currentlyExpandedGroupId = null)
        assertEquals(StopPointId("25482"), outcome.selectedStopPointId)
        assertNull(outcome.expandedGroupId)
    }

    @Test
    fun `multi option origin group requires explicit option selection`() {
        val group = sampleOriginCandidateGroups().first { it.displayName == "Rakvere bussijaam" }
        val outcome = resolveOriginGroupTap(group = group, currentlyExpandedGroupId = null)

        assertNull(outcome.selectedStopPointId)
        assertEquals(group.groupId, outcome.expandedGroupId)
    }

    @Test
    fun `multi option dialog option click selects stopPointId and dismisses dialog`() {
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
    fun `single option dialog group click selects stopPointId and dismisses dialog`() {
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
    fun `origin group label uses valikut wording`() {
        val group = sampleOriginCandidateGroups().first { it.displayName == "Rakvere bussijaam" }
        val label = originGroupLabel(group)
        assertContains(label, "valikut")
        assertFalse(label.contains("(${group.options.size})"))
    }

    @Test
    fun `origin option label uses marsruuti wording`() {
        val option = sampleOriginCandidateGroups().first { it.displayName == "Rakvere bussijaam" }.options.first()
        val label = originOptionLabel(option, index = 0)
        assertContains(label, "marsruuti")
        assertFalse(label.contains("(${option.routePatternCount})"))
    }

    @Test
    fun `search screen strings avoid live and realtime wording`() {
        val allCopy =
            listOf(
                QUICK_DESTINATION_SECTION_TITLE,
                QUICK_DESTINATION_HELPER_TEXT,
                QUICK_DESTINATION_ALIAS_HELPER_TEXT,
                ORIGIN_SEARCH_ACTION_TEXT,
                ORIGIN_DIALOG_TITLE,
                ORIGIN_DIALOG_NO_RESULTS_TEXT,
                ORIGIN_DIALOG_CANCEL_TEXT,
                feedStateMessage(FeedState.Ready),
                destinationStateMessage(DestinationInputState.Empty),
                routeStateMessage(RouteQueryState.Searching),
                routeStateMessage(RouteQueryState.RouteNotFound(RouteNotFoundDisplayReason.NO_DIRECT_PATTERN)),
            ).joinToString("\n")

        assertFalse(allCopy.contains("live", ignoreCase = true))
        assertFalse(allCopy.contains("realtime", ignoreCase = true))
        assertFalse(allCopy.contains("reaalajas", ignoreCase = true))
        assertFalse(allCopy.contains("pärisajas", ignoreCase = true))
    }

    private fun testRouteFoundSummary(): RouteFoundSummary =
        RouteFoundSummary(
            routePatternId = RoutePatternId("pattern:T1"),
            originStopPointId = StopPointId("152898"),
            destinationStopPointId = StopPointId("153420"),
            originSequence = 1,
            destinationSequence = 3,
            segmentStopCount = 3,
            segmentStopPointIds = listOf(StopPointId("152898"), StopPointId("109242"), StopPointId("153420")),
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
            OriginCandidateGroup(
                groupId = "rakvere-kooli",
                displayName = "Kooli",
                options =
                    listOf(
                        OriginCandidateOption(
                            stopPointId = StopPointId("999001"),
                            label = "Kooli",
                            routePatternCount = 1,
                        ),
                    ),
            ),
        )
}
