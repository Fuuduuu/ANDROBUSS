package ee.androbus.feature.search.destination

import ee.androbus.cityadapters.rakvere.RakvereCityAdapterMetadata
import ee.androbus.core.domain.CityId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PlaceToStopCandidateResolverTest {
    private val destinationResolver = DestinationTargetResolver()
    private val candidateResolver = PlaceToStopCandidateResolver()

    @Test
    fun `destination target with no preferred stop groups returns NO_PREFERRED_STOP_GROUPS`() {
        val target = resolveFirstTarget("Kesklinn")

        val result = candidateResolver.resolveCandidates(target)

        val notFound = assertIs<PlaceToStopCandidateResult.NotFound>(result)
        assertEquals(PlaceToStopCandidateNotFoundReason.NO_PREFERRED_STOP_GROUPS, notFound.reason)
    }

    @Test
    fun `city place metadata target with one preferred stop group returns one candidate`() {
        val target =
            resolveFirstTarget("Rakvere bussijaam").copy(
                preferredStopGroupNames = listOf("Rakvere bussijaam"),
            )

        val result = candidateResolver.resolveCandidates(target)

        val found = assertIs<PlaceToStopCandidateResult.Found>(result)
        assertEquals(1, found.candidates.size)
        val candidate = found.candidates.single()
        assertEquals("Rakvere bussijaam", candidate.stopGroupName)
        assertEquals(StopCandidateSource.CITY_PLACE_PREFERRED_STOP_GROUP_NAME, candidate.source)
    }

    @Test
    fun `multiple preferred stop group names preserve metadata order`() {
        val target =
            resolveFirstTarget("Rakvere bussijaam").copy(
                preferredStopGroupNames = listOf("Rakvere bussijaam", "Bussijaam"),
            )

        val result = candidateResolver.resolveCandidates(target)

        val found = assertIs<PlaceToStopCandidateResult.Found>(result)
        assertEquals(listOf("Rakvere bussijaam", "Bussijaam"), found.candidates.map { it.stopGroupName })
    }

    @Test
    fun `candidate does not invent stop point ids`() {
        val target =
            resolveFirstTarget("Rakvere bussijaam").copy(
                preferredStopGroupNames = listOf("Rakvere bussijaam"),
            )

        val result = candidateResolver.resolveCandidates(target)

        val found = assertIs<PlaceToStopCandidateResult.Found>(result)
        assertTrue(found.candidates.single().stopPointIds.isEmpty())
    }

    @Test
    fun `candidate confidence is conservative explicit metadata`() {
        val target =
            resolveFirstTarget("Rakvere bussijaam").copy(
                preferredStopGroupNames = listOf("Rakvere bussijaam"),
            )

        val result = candidateResolver.resolveCandidates(target)

        val found = assertIs<PlaceToStopCandidateResult.Found>(result)
        assertEquals(StopCandidateConfidence.EXPLICIT_METADATA, found.candidates.single().confidence)
    }

    @Test
    fun `unsupported target source returns UNSUPPORTED_TARGET_SOURCE`() {
        val unsupportedTarget =
            resolveFirstTarget("Rakvere bussijaam").copy(
                source = DestinationTargetSource.MANUAL_TEXT,
            )

        val result = candidateResolver.resolveCandidates(unsupportedTarget)

        val notFound = assertIs<PlaceToStopCandidateResult.NotFound>(result)
        assertEquals(PlaceToStopCandidateNotFoundReason.UNSUPPORTED_TARGET_SOURCE, notFound.reason)
    }

    @Test
    fun `rakvere bussijaam target resolves when preferred stop group name exists`() {
        val target =
            resolveFirstTarget("Rakvere bussijaam").copy(
                preferredStopGroupNames = listOf("Rakvere bussijaam"),
            )

        val result = candidateResolver.resolveCandidates(target)

        val found = assertIs<PlaceToStopCandidateResult.Found>(result)
        assertEquals("rakvere:rakvere-bussijaam", found.candidates.single().targetId)
    }

    @Test
    fun `uncertain rakvere place with no preferred stop groups returns NO_PREFERRED_STOP_GROUPS`() {
        val target = resolveFirstTarget("Rakvere linnus")

        val result = candidateResolver.resolveCandidates(target)

        val notFound = assertIs<PlaceToStopCandidateResult.NotFound>(result)
        assertEquals(PlaceToStopCandidateNotFoundReason.NO_PREFERRED_STOP_GROUPS, notFound.reason)
    }

    @Test
    fun `resolver remains android free and produces no route or geospatial results`() {
        val target =
            resolveFirstTarget("Rakvere bussijaam").copy(
                preferredStopGroupNames = listOf("Rakvere bussijaam"),
            )

        val result = candidateResolver.resolveCandidates(target)

        val found = assertIs<PlaceToStopCandidateResult.Found>(result)
        val candidate = found.candidates.single()
        assertFalse(candidate.source == StopCandidateSource.FUTURE_GEOSPATIAL)
        assertTrue(candidate.stopPointIds.isEmpty())

        listOf(
            PlaceToStopCandidateResolver::class.java,
            StopCandidate::class.java,
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

    private fun resolveFirstTarget(query: String): DestinationTarget {
        val result = destinationResolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, query)
        val found = assertIs<DestinationResolutionResult.Found>(result)
        return found.matches.first().target.copy(cityId = CityId("rakvere"))
    }
}
