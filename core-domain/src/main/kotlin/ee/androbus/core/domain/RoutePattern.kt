package ee.androbus.core.domain

data class RoutePattern(
    val id: RoutePatternId,
    val routeLineId: RouteLineId,
    val displayName: String,
    val cityId: CityId,
    val stops: List<PatternStop>,
    val feedId: FeedId? = null,
) {
    init {
        require(displayName.isNotBlank()) { "RoutePattern displayName cannot be blank." }
        require(stops.size >= 2) { "RoutePattern must contain at least two PatternStops." }
        require(stops.zipWithNext().all { (left, right) -> left.sequence < right.sequence }) {
            "RoutePattern stops must be ordered by strictly increasing sequence."
        }
    }

    fun orderedStopPointIds(): List<StopPointId> = stops.map { it.stopPointId }

    fun firstPatternIndexOf(stopPointId: StopPointId): Int? {
        val index = stops.indexOfFirst { it.stopPointId == stopPointId }
        return if (index == -1) null else index
    }
}
