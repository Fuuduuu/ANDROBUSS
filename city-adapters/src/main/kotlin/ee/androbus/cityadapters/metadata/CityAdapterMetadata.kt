package ee.androbus.cityadapters.metadata

import ee.androbus.core.domain.CityId

data class CityAdapterMetadata(
    val cityId: CityId,
    val displayName: String,
    val wave: CityWave,
    val aliases: List<String>,
    val feedMappings: List<CityFeedMapping>,
    val places: List<CityPlaceMetadata>,
    val mappingConfidence: MappingConfidence,
    val notes: List<String> = emptyList(),
) {
    init {
        require(displayName.isNotBlank()) { "CityAdapterMetadata displayName cannot be blank." }
        require(aliases.isNotEmpty()) { "CityAdapterMetadata aliases cannot be empty." }
        require(aliases.all { it.isNotBlank() }) { "CityAdapterMetadata aliases cannot contain blank values." }
        require(feedMappings.isNotEmpty()) { "CityAdapterMetadata feedMappings cannot be empty." }
        require(notes.all { it.isNotBlank() }) { "CityAdapterMetadata notes cannot contain blank values." }
    }
}
