package ee.androbus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "pattern_stops",
    primaryKeys = ["cityId", "feedId", "patternId", "sequence"],
    foreignKeys = [
        ForeignKey(
            entity = RoutePatternEntity::class,
            parentColumns = ["cityId", "feedId", "patternId"],
            childColumns = ["cityId", "feedId", "patternId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["cityId", "feedId", "patternId"]),
    ],
)
data class PatternStopEntity(
    val cityId: String,
    val feedId: String,
    val patternId: String,
    val sequence: Int,
    val stopId: String,
)
