package ee.androbus.core.domain

data class Trip(
    val id: TripId,
    val routePatternId: RoutePatternId,
    val service: ServiceRef,
    val headsign: String? = null,
) {
    init {
        if (headsign != null) {
            require(headsign.isNotBlank()) { "Trip headsign cannot be blank when provided." }
        }
    }
}
