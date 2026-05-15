package ee.androbus.app.presentation.search

import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.StopPointId
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class SearchScreenStateTextTest {
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
                route =
                    RouteFoundSummary(
                        routePatternId = RoutePatternId("pattern:T1"),
                        originStopPointId = StopPointId("RKV_A_OUT"),
                        destinationStopPointId = StopPointId("RKV_C"),
                        originSequence = 1,
                        destinationSequence = 3,
                        segmentStopCount = 3,
                        segmentStopPointIds = listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_B"), StopPointId("RKV_C")),
                        candidateCount = 1,
                    ),
            )

        val text = routeStateMessage(state)
        assertContains(text, "marsruut leitud")
    }

    @Test
    fun `destination empty prompt requires explicit selection action`() {
        val text = destinationStateMessage(DestinationInputState.Empty)
        assertContains(text, "Vali sihtkoht")
    }
}
