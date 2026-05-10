package ee.androbus.feature.search.resolution

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.GeoPoint

data class StopPointResolutionInput(
    val stopGroupName: String,
    val cityId: CityId,
    val coordinateHint: GeoPoint? = null,
)

