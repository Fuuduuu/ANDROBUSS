package ee.androbus.app.presentation.search

import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
}
