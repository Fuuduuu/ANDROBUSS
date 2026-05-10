package ee.androbus.feature.search.resolution

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.GeoPoint
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InMemoryStopPointIndexTest {
    @Test
    fun `exact name match returns candidates with GTFS sourced ids`() {
        val stops =
            listOf(
                stopPoint(
                    id = "RKV_A_OUT",
                    groupId = "group:keskpeatus:out",
                    displayName = "Keskpeatus",
                    latitude = 59.346100,
                    longitude = 26.355200,
                ),
            )
        val resolver = InMemoryStopPointIndex(stops)

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Keskpeatus",
                    cityId = CityId("rakvere"),
                ),
            )

        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)
        assertEquals(1, resolved.candidates.size)
        val candidate = resolved.candidates.single()
        val sourceStop = stops.single()

        assertEquals(sourceStop.id, candidate.stopPointId)
        assertEquals(sourceStop.stopGroupId, candidate.stopGroupId)
        assertEquals(sourceStop.displayName, candidate.displayName)
        assertEquals(sourceStop.location, candidate.location)
        assertEquals(StopPointResolutionSource.GTFS_STOP_ID, candidate.source)
        assertEquals(StopPointResolutionConfidence.EXACT_NAME_MATCH, candidate.confidence)
    }

    @Test
    fun `same name returns all matching stop points in input order`() {
        val first =
            stopPoint(
                id = "RKV_A_OUT",
                groupId = "group:keskpeatus:out",
                displayName = "Keskpeatus",
                latitude = 59.346100,
                longitude = 26.355200,
            )
        val second =
            stopPoint(
                id = "RKV_A_IN",
                groupId = "group:keskpeatus:in",
                displayName = "Keskpeatus",
                latitude = 59.346300,
                longitude = 26.355500,
            )
        val resolver = InMemoryStopPointIndex(listOf(first, second))

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Keskpeatus",
                    cityId = CityId("rakvere"),
                ),
            )

        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)
        assertEquals(2, resolved.candidates.size)
        assertEquals(listOf(first.id, second.id), resolved.candidates.map { it.stopPointId })
    }

    @Test
    fun `lowercase normalized query returns normalized match confidence`() {
        val resolver =
            InMemoryStopPointIndex(
                listOf(
                    stopPoint(
                        id = "RKV_A_OUT",
                        groupId = "group:keskpeatus:out",
                        displayName = "Keskpeatus",
                        latitude = 59.346100,
                        longitude = 26.355200,
                    ),
                ),
            )

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "  keskpeatus ",
                    cityId = CityId("rakvere"),
                ),
            )

        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)
        assertEquals(StopPointResolutionConfidence.NORMALIZED_NAME_MATCH, resolved.candidates.single().confidence)
    }

    @Test
    fun `unknown name returns NoStopGroupMatch`() {
        val resolver =
            InMemoryStopPointIndex(
                listOf(
                    stopPoint(
                        id = "RKV_A_OUT",
                        groupId = "group:keskpeatus:out",
                        displayName = "Keskpeatus",
                        latitude = 59.346100,
                        longitude = 26.355200,
                    ),
                ),
            )

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "DoesNotExist",
                    cityId = CityId("rakvere"),
                ),
            )

        assertTrue(result === StopPointResolutionResult.NotResolved.NoStopGroupMatch)
    }

    @Test
    fun `blank name returns EmptyStopGroupName`() {
        val resolver =
            InMemoryStopPointIndex(
                listOf(
                    stopPoint(
                        id = "RKV_A_OUT",
                        groupId = "group:keskpeatus:out",
                        displayName = "Keskpeatus",
                        latitude = 59.346100,
                        longitude = 26.355200,
                    ),
                ),
            )

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "   ",
                    cityId = CityId("rakvere"),
                ),
            )

        assertTrue(result === StopPointResolutionResult.NotResolved.EmptyStopGroupName)
    }

    @Test
    fun `empty index returns NoIndexAvailable`() {
        val resolver = InMemoryStopPointIndex(emptyList())

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Keskpeatus",
                    cityId = CityId("rakvere"),
                ),
            )

        assertTrue(result === StopPointResolutionResult.NotResolved.NoIndexAvailable)
    }

    @Test
    fun `future source and confidence values are never emitted`() {
        val resolver =
            InMemoryStopPointIndex(
                listOf(
                    stopPoint(
                        id = "RKV_A_OUT",
                        groupId = "group:keskpeatus:out",
                        displayName = "Keskpeatus",
                        latitude = 59.346100,
                        longitude = 26.355200,
                    ),
                    stopPoint(
                        id = "RKV_A_IN",
                        groupId = "group:keskpeatus:in",
                        displayName = "Keskpeatus",
                        latitude = 59.346300,
                        longitude = 26.355500,
                    ),
                ),
            )

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Keskpeatus",
                    cityId = CityId("rakvere"),
                ),
            )

        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)
        assertTrue(resolved.candidates.all { it.source == StopPointResolutionSource.GTFS_STOP_ID })
        assertTrue(
            resolved.candidates.all {
                it.confidence != StopPointResolutionConfidence.FUTURE_COORDINATE_NEAREST
            },
        )
        assertTrue(resolved.candidates.none { it.source == StopPointResolutionSource.FUTURE_GEOSPATIAL })
    }

    @Test
    fun `coordinateHint does not change result`() {
        val resolver =
            InMemoryStopPointIndex(
                listOf(
                    stopPoint(
                        id = "RKV_A_OUT",
                        groupId = "group:keskpeatus:out",
                        displayName = "Keskpeatus",
                        latitude = 59.346100,
                        longitude = 26.355200,
                    ),
                    stopPoint(
                        id = "RKV_A_IN",
                        groupId = "group:keskpeatus:in",
                        displayName = "Keskpeatus",
                        latitude = 59.346300,
                        longitude = 26.355500,
                    ),
                ),
            )

        val withoutHint =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Keskpeatus",
                    cityId = CityId("rakvere"),
                ),
            )
        val withHint =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Keskpeatus",
                    cityId = CityId("rakvere"),
                    coordinateHint = GeoPoint(latitude = 59.0, longitude = 26.0),
                ),
            )

        assertEquals(withoutHint, withHint)
    }

    @Test
    fun `city boundary is respected by name index`() {
        val resolver =
            InMemoryStopPointIndex(
                listOf(
                    stopPoint(
                        id = "RKV_A_OUT",
                        groupId = "group:keskpeatus:rakvere",
                        displayName = "Keskpeatus",
                        latitude = 59.346100,
                        longitude = 26.355200,
                        cityId = "rakvere",
                    ),
                    stopPoint(
                        id = "PAR_A",
                        groupId = "group:keskpeatus:parnu",
                        displayName = "Keskpeatus",
                        latitude = 58.385900,
                        longitude = 24.497100,
                        cityId = "parnu",
                    ),
                ),
            )

        val result =
            resolver.resolve(
                StopPointResolutionInput(
                    stopGroupName = "Keskpeatus",
                    cityId = CityId("rakvere"),
                ),
            )

        val resolved = assertIs<StopPointResolutionResult.Resolved>(result)
        assertEquals(listOf(StopPointId("RKV_A_OUT")), resolved.candidates.map { it.stopPointId })
    }

    @Test
    fun `resolver remains android free`() {
        listOf(
            InMemoryStopPointIndex::class.java,
            StopPointResolver::class.java,
            StopPointResolutionInput::class.java,
            StopPointResolutionResult::class.java,
            VerifiedStopPointCandidate::class.java,
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

    private fun stopPoint(
        id: String,
        groupId: String,
        displayName: String,
        latitude: Double,
        longitude: Double,
        cityId: String = "rakvere",
    ): StopPoint =
        StopPoint(
            id = StopPointId(id),
            stopGroupId = StopGroupId(groupId),
            displayName = displayName,
            location = GeoPoint(latitude = latitude, longitude = longitude),
            cityId = CityId(cityId),
            feedId = FeedId("fixture-feed"),
        )
}

