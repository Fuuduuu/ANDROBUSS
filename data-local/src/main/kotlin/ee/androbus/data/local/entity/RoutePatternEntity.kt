package ee.androbus.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "route_patterns",
    primaryKeys = ["cityId", "feedId", "patternId"],
    indices = [
        Index(value = ["cityId", "feedId"]),
    ],
)
data class RoutePatternEntity(
    val cityId: String,
    val feedId: String,
    val patternId: String,
    val routeLineId: String,
    val displayName: String,
)
