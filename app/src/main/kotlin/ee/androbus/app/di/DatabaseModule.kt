package ee.androbus.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.androbus.data.local.dao.FeedSnapshotDao
import ee.androbus.data.local.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "androbus-feed.db",
        ).build()

    @Provides
    @Singleton
    fun provideFeedSnapshotDao(
        database: AppDatabase,
    ): FeedSnapshotDao = database.feedSnapshotDao()
}
