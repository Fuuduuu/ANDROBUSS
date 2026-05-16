package ee.androbus.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ee.androbus.data.local.database.AppDatabase
import ee.androbus.data.local.entity.FeedMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class FeedMetadataDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: FeedMetadataDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.feedMetadataDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `upsert then getByScope returns metadata`() =
        runDb {
            val metadata = metadata(cityId = "rakvere", feedId = "rakvere-v20260428", isActive = 0)
            dao.upsert(metadata)

            val loaded = dao.getByScope(cityId = "rakvere", feedId = "rakvere-v20260428")
            assertEquals(metadata, loaded)
        }

    @Test
    fun `getActiveFeed returns null when none is active`() =
        runDb {
            dao.upsert(metadata(cityId = "rakvere", feedId = "rakvere-v20260428", isActive = 0))

            assertNull(dao.getActiveFeed(cityId = "rakvere"))
        }

    @Test
    fun `activateFeed existing feed makes it active`() =
        runDb {
            dao.upsert(metadata(cityId = "rakvere", feedId = "rakvere-v20260428", isActive = 0))

            val activated = dao.activateFeed(cityId = "rakvere", feedId = "rakvere-v20260428")
            val active = dao.getActiveFeed(cityId = "rakvere")
            assertTrue(activated)
            assertNotNull(active)
            assertEquals("rakvere-v20260428", active.feedId)
            assertEquals(1, active.isActive)
        }

    @Test
    fun `activateFeed deactivates previous active feed for same city`() =
        runDb {
            dao.upsert(metadata(cityId = "rakvere", feedId = "rakvere-old", isActive = 1))
            dao.upsert(metadata(cityId = "rakvere", feedId = "rakvere-new", isActive = 0))

            val activated = dao.activateFeed(cityId = "rakvere", feedId = "rakvere-new")
            val active = dao.getActiveFeed(cityId = "rakvere")
            val old = dao.getByScope(cityId = "rakvere", feedId = "rakvere-old")
            val new = dao.getByScope(cityId = "rakvere", feedId = "rakvere-new")

            assertTrue(activated)
            assertNotNull(active)
            assertNotNull(old)
            assertNotNull(new)
            assertEquals("rakvere-new", active.feedId)
            assertEquals(0, old.isActive)
            assertEquals(1, new.isActive)
        }

    @Test
    fun `activateFeed missing feed returns false and preserves existing active`() =
        runDb {
            dao.upsert(metadata(cityId = "rakvere", feedId = "rakvere-old", isActive = 1))

            val activated = dao.activateFeed(cityId = "rakvere", feedId = "rakvere-missing")
            val active = dao.getActiveFeed(cityId = "rakvere")
            val old = dao.getByScope(cityId = "rakvere", feedId = "rakvere-old")

            assertFalse(activated)
            assertNotNull(active)
            assertNotNull(old)
            assertEquals("rakvere-old", active.feedId)
            assertEquals(1, old.isActive)
        }

    @Test
    fun `activateFeed different cities are independent`() =
        runDb {
            dao.upsert(metadata(cityId = "rakvere", feedId = "rakvere-a", isActive = 1))
            dao.upsert(metadata(cityId = "rakvere", feedId = "rakvere-b", isActive = 0))
            dao.upsert(metadata(cityId = "tartu", feedId = "tartu-a", isActive = 1))
            dao.upsert(metadata(cityId = "tartu", feedId = "tartu-b", isActive = 0))

            val rakvereActivated = dao.activateFeed(cityId = "rakvere", feedId = "rakvere-b")
            val rakvereActive = dao.getActiveFeed(cityId = "rakvere")
            val tartuActive = dao.getActiveFeed(cityId = "tartu")

            assertTrue(rakvereActivated)
            assertNotNull(rakvereActive)
            assertNotNull(tartuActive)
            assertEquals("rakvere-b", rakvereActive.feedId)
            assertEquals("tartu-a", tartuActive.feedId)
        }

    private fun metadata(
        cityId: String,
        feedId: String,
        isActive: Int,
    ): FeedMetadataEntity =
        FeedMetadataEntity(
            cityId = cityId,
            feedId = feedId,
            downloadedAt = 1_714_518_400_000L,
            sourceUrl = "https://example.test/$cityId/$feedId.zip",
            feedVersion = "v-test",
            isActive = isActive,
        )

    private fun runDb(block: suspend () -> Unit) {
        runBlocking(Dispatchers.IO) { block() }
    }
}
