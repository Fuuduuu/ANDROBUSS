package ee.androbus.feature.search.resolution

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.destination.StopCandidateConfidence
import ee.androbus.feature.search.destination.StopCandidateSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StopCandidateEnricherTest {
    private val cityId = CityId("rakvere")

    private val rkvC =
        StopPoint(
            id = StopPointId("RKV_C"),
            displayName = "Jaam",
            cityId = cityId,
            stopGroupId = StopGroupId("group:jaam"),
            location = GeoPoint(59.3550, 26.3650),
            feedId = FeedId("rakvere-fixture"),
        )

    private val rkvAOut =
        StopPoint(
            id = StopPointId("RKV_A_OUT"),
            displayName = "Keskpeatus",
            cityId = cityId,
            stopGroupId = StopGroupId("group:keskpeatus-out"),
            location = GeoPoint(59.3461, 26.3552),
            feedId = FeedId("rakvere-fixture"),
        )

    private val rkvAIn =
        StopPoint(
            id = StopPointId("RKV_A_IN"),
            displayName = "Keskpeatus",
            cityId = cityId,
            stopGroupId = StopGroupId("group:keskpeatus-in"),
            location = GeoPoint(59.3463, 26.3555),
            feedId = FeedId("rakvere-fixture"),
        )

    @Test
    fun `exact name match enriches candidate`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvC)))
        val candidate = unresolvedCandidate("Jaam")

        val result = enricher.enrich(candidate, cityId)

        val enriched = assertIs<StopCandidateEnrichmentResult.Enriched>(result)
        assertEquals(listOf(StopPointId("RKV_C")), enriched.enrichedCandidate.stopPointIds)
        assertEquals(StopPointId("RKV_C"), enriched.verifiedCandidates.single().stopPointId)
    }

    @Test
    fun `normalized name match enriches candidate`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvC)))
        val candidate = unresolvedCandidate("  jaam ")

        val result = enricher.enrich(candidate, cityId)

        val enriched = assertIs<StopCandidateEnrichmentResult.Enriched>(result)
        assertEquals(listOf(StopPointId("RKV_C")), enriched.enrichedCandidate.stopPointIds)
    }

    @Test
    fun `same name two stops enriches with both ids and preserves order`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvAOut, rkvAIn)))
        val candidate = unresolvedCandidate("Keskpeatus")

        val result = enricher.enrich(candidate, cityId)

        val enriched = assertIs<StopCandidateEnrichmentResult.Enriched>(result)
        assertEquals(listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_A_IN")), enriched.enrichedCandidate.stopPointIds)
        assertEquals(2, enriched.verifiedCandidates.size)
        assertEquals(listOf(StopPointId("RKV_A_OUT"), StopPointId("RKV_A_IN")), enriched.verifiedCandidates.map { it.stopPointId })
    }

    @Test
    fun `enriched stopPointIds size equals verifiedCandidates size`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvAOut, rkvAIn)))
        val candidate = unresolvedCandidate("Keskpeatus")

        val result = enricher.enrich(candidate, cityId)

        val enriched = assertIs<StopCandidateEnrichmentResult.Enriched>(result)
        assertEquals(enriched.verifiedCandidates.size, enriched.enrichedCandidate.stopPointIds.size)
    }

    @Test
    fun `unknown name returns NotEnriched NoStopGroupMatch and preserves original candidate`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvC)))
        val candidate = unresolvedCandidate("Unknown")

        val result = enricher.enrich(candidate, cityId)

        val notEnriched = assertIs<StopCandidateEnrichmentResult.NotEnriched>(result)
        assertTrue(notEnriched.reason === StopPointResolutionResult.NotResolved.NoStopGroupMatch)
        assertEquals(candidate, notEnriched.originalCandidate)
        assertTrue(notEnriched.originalCandidate.stopPointIds.isEmpty())
    }

    @Test
    fun `empty index returns NotEnriched NoIndexAvailable and preserves original candidate`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(emptyList()))
        val candidate = unresolvedCandidate("Jaam")

        val result = enricher.enrich(candidate, cityId)

        val notEnriched = assertIs<StopCandidateEnrichmentResult.NotEnriched>(result)
        assertTrue(notEnriched.reason === StopPointResolutionResult.NotResolved.NoIndexAvailable)
        assertEquals(candidate, notEnriched.originalCandidate)
        assertTrue(notEnriched.originalCandidate.stopPointIds.isEmpty())
    }

    @Test
    fun `empty stop group branch can be propagated from resolver without changing StopCandidate invariants`() {
        val fakeResolver =
            object : StopPointResolver {
                override fun resolve(input: StopPointResolutionInput): StopPointResolutionResult =
                    StopPointResolutionResult.NotResolved.EmptyStopGroupName
            }
        val enricher = StopCandidateEnricher(fakeResolver)
        val candidate = unresolvedCandidate("Jaam")

        val result = enricher.enrich(candidate, cityId)

        val notEnriched = assertIs<StopCandidateEnrichmentResult.NotEnriched>(result)
        assertTrue(notEnriched.reason === StopPointResolutionResult.NotResolved.EmptyStopGroupName)
        assertEquals(candidate, notEnriched.originalCandidate)
    }

    @Test
    fun `anti fabrication guard keeps verified stop id only`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvC)))
        val candidate = unresolvedCandidate("Jaam")

        val result = enricher.enrich(candidate, cityId)

        val enriched = assertIs<StopCandidateEnrichmentResult.Enriched>(result)
        val resolvedId = enriched.enrichedCandidate.stopPointIds.single()
        assertEquals(StopPointId("RKV_C"), resolvedId)
        assertNotEquals(StopPointId("Jaam"), resolvedId)
        assertNotEquals(StopPointId("jaam"), resolvedId)
    }

    @Test
    fun `NotEnriched does not fabricate id on failure`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvC)))
        val candidate = unresolvedCandidate("Totally Unknown Stop")

        val result = enricher.enrich(candidate, cityId)

        val notEnriched = assertIs<StopCandidateEnrichmentResult.NotEnriched>(result)
        assertTrue(notEnriched.originalCandidate.stopPointIds.isEmpty())
    }

    @Test
    fun `verified candidates are exposed in Enriched`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvC)))
        val candidate = unresolvedCandidate("Jaam")

        val result = enricher.enrich(candidate, cityId)

        val enriched = assertIs<StopCandidateEnrichmentResult.Enriched>(result)
        assertEquals(1, enriched.verifiedCandidates.size)
        assertEquals(StopPointId("RKV_C"), enriched.verifiedCandidates.single().stopPointId)
    }

    @Test
    fun `multiple calls with same input are deterministic`() {
        val enricher = StopCandidateEnricher(InMemoryStopPointIndex(listOf(rkvAOut, rkvAIn)))
        val candidate = unresolvedCandidate("Keskpeatus")

        val first = enricher.enrich(candidate, cityId)
        val second = enricher.enrich(candidate, cityId)

        assertEquals(first, second)
    }

    @Test
    fun `enricher classes remain android free`() {
        listOf(
            StopCandidateEnricher::class.java,
            StopCandidateEnrichmentResult::class.java,
        ).forEach { clazz ->
            clazz.declaredConstructors.forEach { constructor ->
                constructor.parameterTypes.forEach { parameterType ->
                    assertFalse(
                        parameterType.name.startsWith("android."),
                        "Android type ${parameterType.name} found in ${clazz.name} constructor.",
                    )
                }
            }
            clazz.declaredMethods.forEach { method ->
                assertFalse(
                    method.returnType.name.startsWith("android."),
                    "Android return type ${method.returnType.name} found in ${clazz.name}.${method.name}.",
                )
                method.parameterTypes.forEach { parameterType ->
                    assertFalse(
                        parameterType.name.startsWith("android."),
                        "Android parameter type ${parameterType.name} found in ${clazz.name}.${method.name}.",
                    )
                }
            }
        }
    }

    private fun unresolvedCandidate(stopGroupName: String): StopCandidate =
        StopCandidate(
            targetId = "dest:$stopGroupName",
            stopGroupName = stopGroupName,
            source = StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME,
            confidence = StopCandidateConfidence.EXPLICIT_METADATA,
            notes = "Name-level unresolved destination candidate.",
            stopPointIds = emptyList(),
        )
}
