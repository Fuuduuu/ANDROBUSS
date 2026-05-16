package ee.androbus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ee.androbus.data.local.entity.FeedMetadataEntity

@Dao
interface FeedMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: FeedMetadataEntity)

    @Query("SELECT * FROM feed_metadata WHERE cityId = :cityId AND feedId = :feedId")
    suspend fun getByScope(
        cityId: String,
        feedId: String,
    ): FeedMetadataEntity?

    @Query("SELECT * FROM feed_metadata WHERE cityId = :cityId AND isActive = 1 LIMIT 1")
    suspend fun getActiveFeed(cityId: String): FeedMetadataEntity?

    @Transaction
    suspend fun activateFeed(
        cityId: String,
        feedId: String,
    ): Boolean {
        val target = getByScope(cityId, feedId) ?: return false
        deactivateAllForCity(cityId)
        return markActive(cityId, target.feedId) == 1
    }

    @Query("UPDATE feed_metadata SET isActive = 0 WHERE cityId = :cityId")
    suspend fun deactivateAllForCity(cityId: String): Int

    @Query("UPDATE feed_metadata SET isActive = 1 WHERE cityId = :cityId AND feedId = :feedId")
    suspend fun markActive(
        cityId: String,
        feedId: String,
    ): Int
}
