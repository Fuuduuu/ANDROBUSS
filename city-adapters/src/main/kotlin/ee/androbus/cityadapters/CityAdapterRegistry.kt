package ee.androbus.cityadapters

import ee.androbus.cityadapters.metadata.CityAdapterMetadata
import ee.androbus.cityadapters.rakvere.RakvereCityAdapterMetadata
import ee.androbus.core.domain.CityId

object CityAdapterRegistry {
    val all: List<CityAdapterMetadata> = listOf(RakvereCityAdapterMetadata.metadata)

    private val byCityId: Map<CityId, CityAdapterMetadata> =
        all.associateBy { it.cityId }.also { index ->
            require(index.size == all.size) { "CityAdapterRegistry contains duplicate cityId values." }
        }

    fun findByCityId(cityId: CityId): CityAdapterMetadata? = byCityId[cityId]
}
