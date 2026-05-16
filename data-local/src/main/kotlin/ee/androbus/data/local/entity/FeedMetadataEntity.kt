package ee.androbus.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "feed_metadata",
    primaryKeys = ["cityId", "feedId"],
    indices = [
        Index(value = ["cityId", "isActive"]),
    ],
)
data class FeedMetadataEntity(
    val cityId: String,
    val feedId: String,
    val downloadedAt: Long,
    val sourceUrl: String,
    val feedVersion: String,
    val isActive: Int,
)
