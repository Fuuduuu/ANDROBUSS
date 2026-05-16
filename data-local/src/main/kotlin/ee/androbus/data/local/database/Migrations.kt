package ee.androbus.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `feed_metadata` (
                    `cityId` TEXT NOT NULL,
                    `feedId` TEXT NOT NULL,
                    `downloadedAt` INTEGER NOT NULL,
                    `sourceUrl` TEXT NOT NULL,
                    `feedVersion` TEXT NOT NULL,
                    `isActive` INTEGER NOT NULL,
                    PRIMARY KEY(`cityId`, `feedId`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS `index_feed_metadata_cityId_isActive`
                ON `feed_metadata` (`cityId`, `isActive`)
                """.trimIndent(),
            )
        }
    }
