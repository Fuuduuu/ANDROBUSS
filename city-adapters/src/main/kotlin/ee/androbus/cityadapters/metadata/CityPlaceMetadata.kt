package ee.androbus.cityadapters.metadata

import ee.androbus.core.domain.GeoPoint

data class CityPlaceMetadata(
    val placeId: String,
    val displayName: String,
    val aliases: List<String>,
    val category: PlaceCategory,
    val coordinate: GeoPoint? = null,
    val coordinateConfidence: CoordinateConfidence = CoordinateConfidence.UNKNOWN,
    val preferredStopGroupNames: List<String> = emptyList(),
    val notes: String? = null,
) {
    init {
        require(placeId.isNotBlank()) { "CityPlaceMetadata placeId cannot be blank." }
        require(displayName.isNotBlank()) { "CityPlaceMetadata displayName cannot be blank." }
        require(aliases.all { it.isNotBlank() }) { "CityPlaceMetadata aliases cannot contain blank values." }
        require(preferredStopGroupNames.all { it.isNotBlank() }) {
            "CityPlaceMetadata preferredStopGroupNames cannot contain blank values."
        }
        if (notes != null) {
            require(notes.isNotBlank()) { "CityPlaceMetadata notes cannot be blank when provided." }
        }

        if (coordinate == null) {
            require(coordinateConfidence == CoordinateConfidence.UNKNOWN) {
                "CityPlaceMetadata coordinateConfidence must be UNKNOWN when coordinate is null."
            }
        } else {
            require(coordinateConfidence != CoordinateConfidence.UNKNOWN) {
                "CityPlaceMetadata coordinateConfidence must not be UNKNOWN when coordinate is provided."
            }
        }
    }
}
