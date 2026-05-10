package ee.androbus.feature.search.destination

import ee.androbus.cityadapters.metadata.CityPlaceMetadata
import ee.androbus.cityadapters.metadata.CoordinateConfidence
import ee.androbus.cityadapters.metadata.PlaceCategory
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.GeoPoint

enum class DestinationTargetSource {
    CITY_PLACE_METADATA,
    STOP_GROUP,
    MAP_PIN,
    SAVED_PLACE,
    MANUAL_TEXT,
}

enum class DestinationTargetConfidence {
    EXACT_ALIAS,
    NORMALIZED_ALIAS,
    PARTIAL_ALIAS,
    UNCLEAR,
}

data class DestinationTarget(
    val id: String,
    val displayName: String,
    val aliases: List<String>,
    val source: DestinationTargetSource,
    val cityId: CityId,
    val placeCategory: PlaceCategory? = null,
    val coordinate: GeoPoint? = null,
    val coordinateConfidence: CoordinateConfidence = CoordinateConfidence.UNKNOWN,
    val preferredStopGroupNames: List<String> = emptyList(),
    val confidence: DestinationTargetConfidence,
    val notes: String? = null,
) {
    init {
        require(id.isNotBlank()) { "DestinationTarget id cannot be blank." }
        require(displayName.isNotBlank()) { "DestinationTarget displayName cannot be blank." }
        require(aliases.all { it.isNotBlank() }) { "DestinationTarget aliases cannot contain blank values." }
        require(preferredStopGroupNames.all { it.isNotBlank() }) {
            "DestinationTarget preferredStopGroupNames cannot contain blank values."
        }
        if (notes != null) {
            require(notes.isNotBlank()) { "DestinationTarget notes cannot be blank when provided." }
        }
        if (coordinate == null) {
            require(coordinateConfidence == CoordinateConfidence.UNKNOWN) {
                "DestinationTarget coordinateConfidence must be UNKNOWN when coordinate is null."
            }
        }
    }
}

internal fun CityPlaceMetadata.toDestinationTarget(
    cityId: CityId,
    confidence: DestinationTargetConfidence,
): DestinationTarget =
    DestinationTarget(
        id = "${cityId.value}:$placeId",
        displayName = displayName,
        aliases = aliases,
        source = DestinationTargetSource.CITY_PLACE_METADATA,
        cityId = cityId,
        placeCategory = category,
        coordinate = coordinate,
        coordinateConfidence = coordinateConfidence,
        preferredStopGroupNames = preferredStopGroupNames,
        confidence = confidence,
        notes = notes,
    )
