package ee.androbus.feature.search.origin

import ee.androbus.core.domain.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OriginCandidateResolverTest {
    private val resolver = OriginCandidateResolver()

    @Test
    fun `blank manual text returns BLANK_QUERY`() {
        val result = resolver.fromManualText("")
        val notFound = assertIs<OriginCandidateResult.NotFound>(result)
        assertEquals(OriginCandidateNotFoundReason.BLANK_QUERY, notFound.reason)
    }

    @Test
    fun `whitespace only manual text returns BLANK_QUERY`() {
        val result = resolver.fromManualText("   ")
        val notFound = assertIs<OriginCandidateResult.NotFound>(result)
        assertEquals(OriginCandidateNotFoundReason.BLANK_QUERY, notFound.reason)
    }

    @Test
    fun `nonblank manual text returns one unresolved candidate`() {
        val result = resolver.fromManualText("  Rakvere   bussijaam  ")
        val found = assertIs<OriginCandidateResult.Found>(result)

        assertEquals(1, found.candidates.size)
        val candidate = found.candidates.single()
        assertEquals("Rakvere bussijaam", candidate.displayName)
        assertEquals(OriginCandidateSource.MANUAL_TEXT, candidate.source)
        assertEquals(OriginCandidateConfidence.MANUAL_TEXT_UNRESOLVED, candidate.confidence)
    }

    @Test
    fun `manual text candidate keeps stopPointIds empty`() {
        val result = resolver.fromManualText("Rakvere bussijaam")
        val found = assertIs<OriginCandidateResult.Found>(result)
        assertTrue(found.candidates.single().stopPointIds.isEmpty())
    }

    @Test
    fun `manual text candidate does not fabricate stopGroupNames`() {
        val result = resolver.fromManualText("Rakvere bussijaam")
        val found = assertIs<OriginCandidateResult.Found>(result)
        assertTrue(found.candidates.single().stopGroupNames.isEmpty())
    }

    @Test
    fun `manual text candidate carries no coordinate and conservative coordinate confidence`() {
        val result = resolver.fromManualText("Rakvere bussijaam")
        val found = assertIs<OriginCandidateResult.Found>(result)
        val candidate = found.candidates.single()

        assertNull(candidate.coordinate)
        assertEquals(OriginCoordinateConfidence.MANUAL_OR_UNKNOWN, candidate.coordinateConfidence)
    }

    @Test
    fun `null current location returns MISSING_LOCATION`() {
        val result = resolver.fromCurrentLocation(null)
        val notFound = assertIs<OriginCandidateResult.NotFound>(result)
        assertEquals(OriginCandidateNotFoundReason.MISSING_LOCATION, notFound.reason)
    }

    @Test
    fun `current location returns one unresolved coordinate candidate`() {
        val location = GeoPoint(latitude = 59.3467, longitude = 26.3558)

        val result = resolver.fromCurrentLocation(location)
        val found = assertIs<OriginCandidateResult.Found>(result)

        assertEquals(1, found.candidates.size)
        val candidate = found.candidates.single()
        assertEquals("Current location", candidate.displayName)
        assertEquals(OriginCandidateSource.CURRENT_LOCATION, candidate.source)
        assertEquals(OriginCandidateConfidence.COORDINATE_ONLY_UNRESOLVED, candidate.confidence)
        assertEquals(OriginCoordinateConfidence.PROVIDED_BY_USER_LOCATION, candidate.coordinateConfidence)
    }

    @Test
    fun `current location candidate preserves GeoPoint and keeps stopPointIds empty`() {
        val location = GeoPoint(latitude = 59.3467, longitude = 26.3558)

        val result = resolver.fromCurrentLocation(location)
        val found = assertIs<OriginCandidateResult.Found>(result)
        val candidate = found.candidates.single()

        assertEquals(location, candidate.coordinate)
        assertTrue(candidate.stopPointIds.isEmpty())
    }

    @Test
    fun `resolver remains android free and no nearest route result types are emitted`() {
        val manual = resolver.fromManualText("Rakvere bussijaam")
        val location = resolver.fromCurrentLocation(GeoPoint(latitude = 59.3467, longitude = 26.3558))

        val manualFound = assertIs<OriginCandidateResult.Found>(manual)
        val locationFound = assertIs<OriginCandidateResult.Found>(location)

        assertFalse(manualFound.candidates.single().source == OriginCandidateSource.FUTURE_GEOSPATIAL)
        assertFalse(locationFound.candidates.single().source == OriginCandidateSource.FUTURE_GEOSPATIAL)
        assertTrue(manualFound.candidates.single().stopPointIds.isEmpty())
        assertTrue(locationFound.candidates.single().stopPointIds.isEmpty())

        listOf(
            OriginCandidateResolver::class.java,
            OriginCandidate::class.java,
            OriginCandidateResult::class.java,
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
}

