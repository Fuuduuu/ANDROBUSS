package ee.androbus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ee.androbus.data.local.entity.PatternStopEntity
import ee.androbus.data.local.entity.RoutePatternEntity
import ee.androbus.data.local.entity.StopPointEntity

@Dao
interface FeedSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopPoints(stopPoints: List<StopPointEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePatterns(patterns: List<RoutePatternEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatternStops(stops: List<PatternStopEntity>)

    @Query(
        """
        SELECT * FROM stop_points
        WHERE cityId = :cityId AND feedId = :feedId
        """,
    )
    suspend fun getStopPoints(
        cityId: String,
        feedId: String,
    ): List<StopPointEntity>

    @Query(
        """
        SELECT * FROM route_patterns
        WHERE cityId = :cityId AND feedId = :feedId
        """,
    )
    suspend fun getRoutePatterns(
        cityId: String,
        feedId: String,
    ): List<RoutePatternEntity>

    @Query(
        """
        SELECT * FROM pattern_stops
        WHERE cityId = :cityId AND feedId = :feedId AND patternId = :patternId
        ORDER BY sequence ASC
        """,
    )
    suspend fun getPatternStops(
        cityId: String,
        feedId: String,
        patternId: String,
    ): List<PatternStopEntity>

    @Query(
        """
        DELETE FROM stop_points
        WHERE cityId = :cityId AND feedId = :feedId
        """,
    )
    suspend fun deleteStopPoints(
        cityId: String,
        feedId: String,
    )

    @Query(
        """
        DELETE FROM route_patterns
        WHERE cityId = :cityId AND feedId = :feedId
        """,
    )
    suspend fun deleteRoutePatterns(
        cityId: String,
        feedId: String,
    )

    @Transaction
    suspend fun replaceSnapshot(
        cityId: String,
        feedId: String,
        stops: List<StopPointEntity>,
        patterns: List<RoutePatternEntity>,
        patternStops: List<PatternStopEntity>,
    ) {
        deleteRoutePatterns(cityId = cityId, feedId = feedId)
        deleteStopPoints(cityId = cityId, feedId = feedId)
        insertStopPoints(stops)
        insertRoutePatterns(patterns)
        insertPatternStops(patternStops)
    }
}
