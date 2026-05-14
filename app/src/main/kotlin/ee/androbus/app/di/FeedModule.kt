package ee.androbus.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.androbus.app.bootstrap.FeedBootstrapLoader
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.data.local.dao.FeedSnapshotDao
import ee.androbus.data.local.importer.FeedSnapshotImporter
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotLoader
import ee.androbus.data.local.provider.RoomDomainFeedSnapshotProvider
import ee.androbus.feature.search.bridge.DirectRouteQueryBridge
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {
    @Provides
    @Singleton
    fun provideFeedSnapshotImporter(
        dao: FeedSnapshotDao,
    ): FeedSnapshotImporter = FeedSnapshotImporter(dao)

    @Provides
    @Singleton
    fun provideRoomDomainFeedSnapshotLoader(
        dao: FeedSnapshotDao,
    ): RoomDomainFeedSnapshotLoader = RoomDomainFeedSnapshotLoader(dao)

    @Provides
    @Singleton
    fun provideRoomDomainFeedSnapshotProvider(
        loader: RoomDomainFeedSnapshotLoader,
    ): RoomDomainFeedSnapshotProvider = RoomDomainFeedSnapshotProvider(loader)

    @Provides
    @Singleton
    fun provideDomainFeedSnapshotProvider(
        provider: RoomDomainFeedSnapshotProvider,
    ): DomainFeedSnapshotProvider = provider

    @Provides
    @Singleton
    fun provideDirectRouteQueryPreparationUseCase(): DirectRouteQueryPreparationUseCase =
        DirectRouteQueryPreparationUseCase(
            bridge = DirectRouteQueryBridge(DirectRouteSearch()),
        )

    @Provides
    @Singleton
    fun provideFeedBootstrapLoader(
        @ApplicationContext context: Context,
        importer: FeedSnapshotImporter,
        provider: RoomDomainFeedSnapshotProvider,
    ): FeedBootstrapLoader =
        FeedBootstrapLoader(
            context = context,
            importer = importer,
            provider = provider,
        )
}
