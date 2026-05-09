package ee.androbus.core.domain

data class RouteLine(
    val id: RouteLineId,
    val displayName: String,
    val cityId: CityId,
    val agencyId: AgencyId? = null,
    val feedId: FeedId? = null,
) {
    init {
        require(displayName.isNotBlank()) { "RouteLine displayName cannot be blank." }
    }
}
