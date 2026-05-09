package ee.androbus.cityadapters.rakvere

import ee.androbus.cityadapters.metadata.CityAdapterMetadata
import ee.androbus.cityadapters.metadata.CityFeedMapping
import ee.androbus.cityadapters.metadata.CityPlaceMetadata
import ee.androbus.cityadapters.metadata.CityWave
import ee.androbus.cityadapters.metadata.CoordinateConfidence
import ee.androbus.cityadapters.metadata.FeedScope
import ee.androbus.cityadapters.metadata.LegalStatus
import ee.androbus.cityadapters.metadata.MappingConfidence
import ee.androbus.cityadapters.metadata.PlaceCategory
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId

object RakvereCityAdapterMetadata {
    private const val AUTHORITY = "Regionaal- ja Pollumajandusministeerium / Uhistranspordiregistri avaandmed"
    private const val HOSTING = "https://eu-gtfs.remix.com/"
    private const val PRIMARY_FEED_URL = "https://eu-gtfs.remix.com/rakvere.zip"
    private const val CONTEXT_FEED_URL = "https://eu-gtfs.remix.com/laane_virumaa.zip"
    private const val SEED_PLACE_NOTE = "Seed metadata; stop mapping to be verified in later pass."

    val metadata: CityAdapterMetadata =
        CityAdapterMetadata(
            cityId = CityId("rakvere"),
            displayName = "Rakvere",
            wave = CityWave.WAVE_0,
            aliases = listOf("Rakvere", "rakvere", "Rakvere linn"),
            feedMappings =
                listOf(
                    CityFeedMapping(
                        feedId = FeedId("rakvere"),
                        feedName = "rakvere.zip",
                        sourceUrl = PRIMARY_FEED_URL,
                        authorityName = AUTHORITY,
                        hostingUrl = HOSTING,
                        feedScope = FeedScope.CITY,
                        mappingConfidence = MappingConfidence.CONFIRMED,
                        legalStatus = LegalStatus.HOSTING_VERIFIED_LEGAL_UNCLEAR,
                        notes = "PASS 03 verified live hosting; PASS 04 mapped Rakvere as Wave 0 primary feed.",
                    ),
                    CityFeedMapping(
                        feedId = FeedId("laane_virumaa"),
                        feedName = "laane_virumaa.zip",
                        sourceUrl = CONTEXT_FEED_URL,
                        authorityName = AUTHORITY,
                        hostingUrl = HOSTING,
                        feedScope = FeedScope.COUNTY,
                        mappingConfidence = MappingConfidence.PARTIAL,
                        legalStatus = LegalStatus.HOSTING_VERIFIED_LEGAL_UNCLEAR,
                        notes = "County context feed for broader regional coverage and fallback validation.",
                    ),
                ),
            places =
                listOf(
                    place("kesklinn", "Kesklinn", PlaceCategory.CENTER),
                    place("rakvere-bussijaam", "Rakvere bussijaam", PlaceCategory.BUS_STATION, aliases = listOf("Bussijaam")),
                    place("rakvere-raudteejaam", "Rakvere raudteejaam", PlaceCategory.TRAIN_STATION, aliases = listOf("Raudteejaam")),
                    place("pohjakeskus", "Põhjakeskus", PlaceCategory.SHOPPING, aliases = listOf("Pohjakeskus")),
                    place("vaala-keskus", "Vaala keskus", PlaceCategory.SHOPPING),
                    place("rakvere-haigla", "Rakvere haigla", PlaceCategory.HEALTHCARE),
                    place("rakvere-polikliinik", "Polikliinik", PlaceCategory.HEALTHCARE),
                    place("rakvere-teater", "Rakvere teater", PlaceCategory.CULTURE),
                    place("aqva", "Aqva", PlaceCategory.TOURISM),
                    place("rakvere-linnus", "Rakvere linnus", PlaceCategory.TOURISM),
                    place("vallimagi", "Vallimägi", PlaceCategory.TOURISM, aliases = listOf("Vallimagi")),
                ),
            mappingConfidence = MappingConfidence.CONFIRMED,
            notes =
                listOf(
                    "Source and feed mapping derived from PASS 03 and PASS 04 audits.",
                    "Legal/license certainty remains conservative until official confirmation is documented.",
                ),
        )

    private fun place(
        id: String,
        name: String,
        category: PlaceCategory,
        aliases: List<String> = emptyList(),
    ): CityPlaceMetadata =
        CityPlaceMetadata(
            placeId = id,
            displayName = name,
            aliases = aliases,
            category = category,
            coordinate = null,
            coordinateConfidence = CoordinateConfidence.UNKNOWN,
            preferredStopGroupNames = emptyList(),
            notes = SEED_PLACE_NOTE,
        )
}
