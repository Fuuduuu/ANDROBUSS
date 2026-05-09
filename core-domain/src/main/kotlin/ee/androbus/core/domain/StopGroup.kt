package ee.androbus.core.domain

data class StopGroup(
    val id: StopGroupId,
    val displayName: String,
    val cityId: CityId,
    val centroid: GeoPoint? = null,
) {
    init {
        require(displayName.isNotBlank()) { "StopGroup displayName cannot be blank." }
    }
}
