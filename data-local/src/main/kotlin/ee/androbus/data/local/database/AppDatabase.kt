package ee.androbus.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ee.androbus.data.local.dao.FeedSnapshotDao
import ee.androbus.data.local.entity.PatternStopEntity
import ee.androbus.data.local.entity.RoutePatternEntity
import ee.androbus.data.local.entity.StopPointEntity

@Database(
    entities = [
        StopPointEntity::class,
        RoutePatternEntity::class,
        PatternStopEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedSnapshotDao(): FeedSnapshotDao

    companion object {
        fun create(context: android.content.Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "androbus-feed.db",
            ).build()
    }
}
