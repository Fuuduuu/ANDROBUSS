package ee.androbus.cityadapters

import ee.androbus.cityadapters.metadata.CityWave
import ee.androbus.cityadapters.metadata.CoordinateConfidence
import ee.androbus.cityadapters.metadata.LegalStatus
import ee.androbus.cityadapters.metadata.MappingConfidence
import ee.androbus.cityadapters.rakvere.RakvereCityAdapterMetadata
import ee.androbus.core.domain.CityId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CityAdapterRegistryTest {
    @Test
    fun `rakvere metadata exists`() {
        val metadata = RakvereCityAdapterMetadata.metadata
        assertEquals(CityId("rakvere"), metadata.cityId)
        assertEquals("Rakvere", metadata.displayName)
    }

    @Test
    fun `rakvere wave is wave 0`() {
        assertEquals(CityWave.WAVE_0, RakvereCityAdapterMetadata.metadata.wave)
    }

    @Test
    fun `rakvere has primary and context feed mappings`() {
        val mappings = RakvereCityAdapterMetadata.metadata.feedMappings

        val primary = mappings.firstOrNull { it.feedId.value == "rakvere" }
        val context = mappings.firstOrNull { it.feedId.value == "laane_virumaa" }

        assertNotNull(primary)
        assertEquals("rakvere.zip", primary.feedName)
        assertEquals(MappingConfidence.CONFIRMED, primary.mappingConfidence)

        assertNotNull(context)
        assertEquals("laane_virumaa.zip", context.feedName)
    }

    @Test
    fun `legal status stays conservative and does not overclaim official confirmation`() {
        val primary = RakvereCityAdapterMetadata.metadata.feedMappings.first { it.feedId.value == "rakvere" }
        assertTrue(primary.legalStatus == LegalStatus.HOSTING_VERIFIED_LEGAL_UNCLEAR || primary.legalStatus == LegalStatus.UNCLEAR)
        assertFalse(primary.legalStatus == LegalStatus.OFFICIAL_AUTHORITY_CONFIRMED)
    }

    @Test
    fun `tallinn and tartu are not active adapters in registry`() {
        val cityIds = CityAdapterRegistry.all.map { it.cityId.value }
        assertFalse("tallinn" in cityIds)
        assertFalse("tartu" in cityIds)
    }

    @Test
    fun `rakvere poi seed list is non empty and conservative on coordinates`() {
        val places = RakvereCityAdapterMetadata.metadata.places
        assertTrue(places.isNotEmpty())

        places.forEach { place ->
            assertNull(place.coordinate)
            assertEquals(CoordinateConfidence.UNKNOWN, place.coordinateConfidence)
        }
    }

    @Test
    fun `rakvere has at least one verified preferred stop group name`() {
        val places = RakvereCityAdapterMetadata.metadata.places
        val resolved = places.filter { it.preferredStopGroupNames.isNotEmpty() }
        assertTrue(resolved.isNotEmpty(), "Expected at least one POI with verified preferredStopGroupNames.")
    }

    @Test
    fun `preferred stop group names stay within discovered real rakvere stop names`() {
        val discoveredStopNames =
            setOf(
                "Bussijaam",
                "Rakvere bussijaam",
                "Raudteejaam",
                "Viru-Kabala raudteejaam",
                "Haigla",
                "Polikliinik",
                "Teater",
                "Põhjakeskus",
                "Vaala",
                "Vallimäe",
                "Põhja",
            )

        RakvereCityAdapterMetadata.metadata.places
            .flatMap { it.preferredStopGroupNames }
            .forEach { stopName ->
                assertTrue(
                    stopName in discoveredStopNames,
                    "Preferred stop-group name '$stopName' was not in PASS 17 discovered stops.txt values.",
                )
            }
    }

    @Test
    fun `uncertain rakvere pois remain unresolved`() {
        val uncertainPlaceIds =
            setOf(
                "kesklinn",
                "rakvere-raudteejaam",
                "vaala-keskus",
                "rakvere-haigla",
                "rakvere-teater",
                "aqva",
                "rakvere-linnus",
                "vallimagi",
            )

        val byId = RakvereCityAdapterMetadata.metadata.places.associateBy { it.placeId }
        uncertainPlaceIds.forEach { placeId ->
            val place = byId[placeId]
            assertNotNull(place, "Missing expected POI '$placeId'.")
            assertTrue(place.preferredStopGroupNames.isEmpty(), "POI '$placeId' should remain unresolved in PASS 17.")
        }
    }

    @Test
    fun `rakvere aliases include basic variants`() {
        val aliases = RakvereCityAdapterMetadata.metadata.aliases
        assertTrue("Rakvere" in aliases)
        assertTrue("rakvere" in aliases)
        assertTrue("Rakvere linn" in aliases)
    }

    @Test
    fun `registry can find rakvere by city id`() {
        val metadata = CityAdapterRegistry.findByCityId(CityId("rakvere"))
        assertNotNull(metadata)
        assertEquals("Rakvere", metadata.displayName)
    }

    @Test
    fun `registry does not contain duplicate city ids`() {
        val ids = CityAdapterRegistry.all.map { it.cityId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `city adapter metadata remains android free`() {
        listOf(
            CityAdapterRegistry::class.java,
            RakvereCityAdapterMetadata::class.java,
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
