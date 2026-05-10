package ee.androbus.feature.search.destination

import ee.androbus.cityadapters.metadata.CityAdapterMetadata
import ee.androbus.cityadapters.metadata.CityFeedMapping
import ee.androbus.cityadapters.metadata.CityWave
import ee.androbus.cityadapters.metadata.FeedScope
import ee.androbus.cityadapters.metadata.LegalStatus
import ee.androbus.cityadapters.metadata.MappingConfidence
import ee.androbus.cityadapters.rakvere.RakvereCityAdapterMetadata
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DestinationTargetResolverTest {
    private val resolver = DestinationTargetResolver()

    @Test
    fun `blank query returns BLANK_QUERY`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "   ")
        val notFound = assertIs<DestinationResolutionResult.NotFound>(result)
        assertEquals(DestinationNotFoundReason.BLANK_QUERY, notFound.reason)
    }

    @Test
    fun `city with no places returns NO_CITY_PLACES`() {
        val emptyCity =
            CityAdapterMetadata(
                cityId = CityId("empty-city"),
                displayName = "Empty City",
                wave = CityWave.LATER,
                aliases = listOf("Empty City"),
                feedMappings =
                    listOf(
                        CityFeedMapping(
                            feedId = FeedId("empty-feed"),
                            feedName = "empty-feed.zip",
                            sourceUrl = "https://example.org/empty-feed.zip",
                            authorityName = "Example Authority",
                            feedScope = FeedScope.CITY,
                            mappingConfidence = MappingConfidence.UNCLEAR,
                            legalStatus = LegalStatus.UNCLEAR,
                        ),
                    ),
                places = emptyList(),
                mappingConfidence = MappingConfidence.UNCLEAR,
            )

        val result = resolver.resolvePlaceQuery(emptyCity, "anything")
        val notFound = assertIs<DestinationResolutionResult.NotFound>(result)
        assertEquals(DestinationNotFoundReason.NO_CITY_PLACES, notFound.reason)
    }

    @Test
    fun `unknown query returns NO_MATCH`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "Xyz no such place")
        val notFound = assertIs<DestinationResolutionResult.NotFound>(result)
        assertEquals(DestinationNotFoundReason.NO_MATCH, notFound.reason)
    }

    @Test
    fun `exact displayName match resolves Rakvere bussijaam`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "Rakvere bussijaam")
        val found = assertIs<DestinationResolutionResult.Found>(result)
        val best = found.matches.first()

        assertEquals("Rakvere bussijaam", best.target.displayName)
        assertEquals(DestinationTargetSource.CITY_PLACE_METADATA, best.target.source)
        assertEquals(DestinationTargetConfidence.EXACT_ALIAS, best.confidence)
    }

    @Test
    fun `alias match resolves bussijaam`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "bussijaam")
        val found = assertIs<DestinationResolutionResult.Found>(result)
        val best = found.matches.first()

        assertEquals("Rakvere bussijaam", best.target.displayName)
        assertEquals(DestinationTargetConfidence.EXACT_ALIAS, best.confidence)
        assertEquals("Bussijaam", best.matchedAlias)
    }

    @Test
    fun `case insensitive query resolves`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "rAkVeRe bUsSiJaAm")
        val found = assertIs<DestinationResolutionResult.Found>(result)
        assertEquals("Rakvere bussijaam", found.matches.first().target.displayName)
        assertEquals(DestinationTargetConfidence.EXACT_ALIAS, found.matches.first().confidence)
    }

    @Test
    fun `extra whitespace query resolves using normalized confidence`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "  Rakvere   bussijaam  ")
        val found = assertIs<DestinationResolutionResult.Found>(result)

        assertEquals("Rakvere bussijaam", found.matches.first().target.displayName)
        assertEquals(DestinationTargetConfidence.NORMALIZED_ALIAS, found.matches.first().confidence)
    }

    @Test
    fun `vaala keskus resolves`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "Vaala keskus")
        val found = assertIs<DestinationResolutionResult.Found>(result)

        assertEquals("Vaala keskus", found.matches.first().target.displayName)
    }

    @Test
    fun `kesklinn resolves`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "Kesklinn")
        val found = assertIs<DestinationResolutionResult.Found>(result)

        assertEquals("Kesklinn", found.matches.first().target.displayName)
    }

    @Test
    fun `partial query behavior is deterministic`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "keskus")
        val found = assertIs<DestinationResolutionResult.Found>(result)

        assertTrue(found.matches.size >= 2)
        assertEquals("Põhjakeskus", found.matches[0].target.displayName)
        assertEquals("Vaala keskus", found.matches[1].target.displayName)
        assertTrue(found.matches.all { it.confidence == DestinationTargetConfidence.PARTIAL_ALIAS })
    }

    @Test
    fun `coordinates remain null and unknown when source metadata is unknown`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "Vallimägi")
        val found = assertIs<DestinationResolutionResult.Found>(result)
        val target = found.matches.first().target

        assertNull(target.coordinate)
        assertEquals(ee.androbus.cityadapters.metadata.CoordinateConfidence.UNKNOWN, target.coordinateConfidence)
    }

    @Test
    fun `resolver does not produce route or nearest stop outputs`() {
        val result = resolver.resolvePlaceQuery(RakvereCityAdapterMetadata.metadata, "Kesklinn")
        val found = assertIs<DestinationResolutionResult.Found>(result)
        val target = found.matches.first().target

        assertTrue(target.preferredStopGroupNames.isEmpty())
        assertTrue(target.id.startsWith("rakvere:"))
    }

    @Test
    fun `resolver logic remains android free`() {
        listOf(
            DestinationTargetResolver::class.java,
            DestinationTarget::class.java,
            DestinationMatch::class.java,
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
