package ee.androbus.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "stop_points",
    primaryKeys = ["cityId", "feedId", "stopId"],
    indices = [
        Index(value = ["cityId", "feedId"]),
    ],
)
data class StopPointEntity(
    val cityId: String,
    val feedId: String,
    val stopId: String,
    val stopGroupId: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val platformCode: String?,
)
