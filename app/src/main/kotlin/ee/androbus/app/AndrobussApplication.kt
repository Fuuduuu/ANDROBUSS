package ee.androbus.app

import android.app.Application
import ee.androbus.app.bootstrap.FeedBootstrapLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AndrobussApplication : Application() {
    @Inject
    lateinit var bootstrapLoader: FeedBootstrapLoader

    private val bootstrapScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        bootstrapScope.launch {
            bootstrapLoader.bootstrapIfNeeded()
        }
    }

    override fun onTerminate() {
        bootstrapScope.cancel()
        super.onTerminate()
    }
}
