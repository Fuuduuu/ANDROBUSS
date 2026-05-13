package ee.androbus.app.bootstrap

import ee.androbus.core.domain.StopPointId
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RakvereDevProfileAssetTest {
    private val json = Json

    @Test
    fun `loads real-derived dev profile from test resources`() {
        val dto = loadDto()
        val snapshot = dto.toDomainFeedSnapshot()

        assertNotNull(snapshot)
    }

    @Test
    fun `dev profile has expected identity markers`() {
        val dto = loadDto()

        assertEquals("rakvere", dto.cityId)
        assertTrue(dto.feedId.contains("dev", ignoreCase = true) || dto.feedId.contains("profile", ignoreCase = true))
    }

    @Test
    fun `dev profile has expected size`() {
        val snapshot = loadDto().toDomainFeedSnapshot()

        assertEquals(98, snapshot.stopPoints.size)
        assertEquals(7, snapshot.routePatterns.size)
    }

    @Test
    fun `anti-fabrication for stop ids`() {
        val dto = loadDto()
        val snapshot = dto.toDomainFeedSnapshot()

        val dtoIds = dto.stopPoints.map { it.id }.toSet()
        val snapshotIds = snapshot.stopPoints.map { it.id.value }.toSet()

        assertEquals(dtoIds, snapshotIds)
        assertFalse(snapshotIds.contains("Jaam"))
        assertFalse(snapshotIds.contains("Rakvere bussijaam"))
        assertTrue(snapshotIds.contains("152898"))
        assertTrue(snapshot.stopPoints.any { it.id == StopPointId("152898") })
    }

    @Test
    fun `routePattern ids are not synthetic`() {
        val snapshot = loadDto().toDomainFeedSnapshot()
        val ids = snapshot.routePatterns.map { it.id.value }

        assertTrue(ids.none { it.startsWith("rakvere-dev-pattern") })
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.isNotBlank() })
        assertTrue(ids.any { it.contains("Liinid_") })
    }

    @Test
    fun `routePattern stop references are valid`() {
        val snapshot = loadDto().toDomainFeedSnapshot()
        val stopIds = snapshot.stopPoints.map { it.id }.toSet()

        snapshot.routePatterns.forEach { pattern ->
            pattern.orderedStopPointIds().forEach { stopId ->
                assertTrue(stopIds.contains(stopId), "Pattern ${pattern.id.value} references unknown stop id ${stopId.value}")
            }
        }
    }

    @Test
    fun `synthetic runtime asset remains main default`() {
        val syntheticAssetExists =
            Files.exists(Path.of("src", "main", "assets", "bootstrap", "rakvere_bootstrap.json")) ||
                Files.exists(Path.of("app", "src", "main", "assets", "bootstrap", "rakvere_bootstrap.json"))
        assertTrue(
            syntheticAssetExists,
            "Synthetic runtime asset must stay in app/src/main/assets/bootstrap/rakvere_bootstrap.json",
        )

        val loaderSourceCandidates =
            listOf(
                Path.of("src", "main", "kotlin", "ee", "androbus", "app", "bootstrap", "FeedBootstrapLoader.kt"),
                Path.of("app", "src", "main", "kotlin", "ee", "androbus", "app", "bootstrap", "FeedBootstrapLoader.kt"),
            )
        val loaderSource = loaderSourceCandidates.firstOrNull { Files.exists(it) }
        assertNotNull(loaderSource, "FeedBootstrapLoader source file must exist in app module")
        val source = Files.newBufferedReader(loaderSource).use { it.readText() }
        assertTrue(source.contains("assetPath: String = \"bootstrap/rakvere_bootstrap.json\""))
    }

    private fun loadDto(): BootstrapFeedDto {
        val classLoader = checkNotNull(javaClass.classLoader)
        val stream = checkNotNull(classLoader.getResourceAsStream("bootstrap/rakvere_dev_profile_v1.json")) {
            "Missing test resource bootstrap/rakvere_dev_profile_v1.json"
        }
        val content = stream.bufferedReader().use { it.readText() }
        return json.decodeFromString(content)
    }
}
