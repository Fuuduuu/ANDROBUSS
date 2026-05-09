package ee.fuuduu.androbuss.core.domain

data class StopPoint(
    val id: StopPointId,
    val stopGroupId: StopGroupId,
    val displayName: String,
    val location: GeoPoint,
    val cityId: CityId,
    val feedId: FeedId? = null,
    val platformCode: String? = null,
) {
    init {
        require(displayName.isNotBlank()) { "StopPoint displayName cannot be blank." }
        if (platformCode != null) {
            require(platformCode.isNotBlank()) { "StopPoint platformCode cannot be blank when provided." }
        }
    }
}
