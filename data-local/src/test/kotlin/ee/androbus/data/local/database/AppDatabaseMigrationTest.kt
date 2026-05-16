package ee.androbus.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            instrumentation = InstrumentationRegistry.getInstrumentation(),
            databaseClass = AppDatabase::class.java,
            specs = emptyList(),
            openFactory = FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun `migration 1 to 2 creates feed metadata table and index`() {
        helper.createDatabase(TEST_DB, 1).close()

        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val migrated =
            Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
                .addMigrations(MIGRATION_1_2)
                .build()

        try {
            val dao = migrated.feedMetadataDao()
            runBlocking {
                val before = dao.getActiveFeed(cityId = "rakvere")
                assertEquals(null, before)
                dao.upsert(
                    ee.androbus.data.local.entity.FeedMetadataEntity(
                        cityId = "rakvere",
                        feedId = "rakvere-v20260428",
                        downloadedAt = 1_714_518_400_000L,
                        sourceUrl = "https://example.test/rakvere.zip",
                        feedVersion = "v-test",
                        isActive = 1,
                    ),
                )
                val after = dao.getActiveFeed(cityId = "rakvere")
                assertNotNull(after)
                assertEquals("rakvere-v20260428", after.feedId)
            }
        } finally {
            migrated.close()
            context.deleteDatabase(TEST_DB)
        }
    }

    private companion object {
        const val TEST_DB = "feed-migration-test.db"
    }
}
