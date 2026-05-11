package ee.androbus.app

import android.app.Application
import ee.androbus.app.bootstrap.FeedBootstrapLoader
import ee.androbus.data.local.database.AppDatabase
import ee.androbus.data.local.importer.FeedSnapshotImporter
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotLoader
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AndrobussApplication : Application() {
    lateinit var db: AppDatabase
        private set

    lateinit var feedSnapshotProvider: RoomDomainFeedSnapshotProvider
        private set

    private val bootstrapScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        db = AppDatabase.create(this)

        val dao = db.feedSnapshotDao()
        val importer = FeedSnapshotImporter(dao)
        val loader = RoomDomainFeedSnapshotLoader(dao)

        feedSnapshotProvider = RoomDomainFeedSnapshotProvider(loader)

        bootstrapScope.launch {
            FeedBootstrapLoader(
                context = this@AndrobussApplication,
                importer = importer,
                provider = feedSnapshotProvider,
            ).bootstrapIfNeeded()
        }
    }

    override fun onTerminate() {
        bootstrapScope.cancel()
        super.onTerminate()
    }
}
