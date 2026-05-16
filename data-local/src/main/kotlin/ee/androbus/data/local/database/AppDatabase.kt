package ee.androbus.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ee.androbus.data.local.dao.FeedMetadataDao
import ee.androbus.data.local.dao.FeedSnapshotDao
import ee.androbus.data.local.entity.FeedMetadataEntity
import ee.androbus.data.local.entity.PatternStopEntity
import ee.androbus.data.local.entity.RoutePatternEntity
import ee.androbus.data.local.entity.StopPointEntity

@Database(
    entities = [
        StopPointEntity::class,
        RoutePatternEntity::class,
        PatternStopEntity::class,
        FeedMetadataEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedSnapshotDao(): FeedSnapshotDao
    abstract fun feedMetadataDao(): FeedMetadataDao

    companion object {
        fun create(context: android.content.Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "androbus-feed.db",
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
