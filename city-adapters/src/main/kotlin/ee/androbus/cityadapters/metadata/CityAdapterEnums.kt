package ee.androbus.cityadapters.metadata

enum class CityWave {
    WAVE_0,
    WAVE_1,
    WAVE_2,
    LATER,
    FUTURE_ONLY,
}

enum class FeedScope {
    CITY,
    COUNTY,
    REGION,
    NATIONAL,
    MIXED,
}

enum class MappingConfidence {
    CONFIRMED,
    PARTIAL,
    UNCLEAR,
    NOT_FOUND,
}

enum class LegalStatus {
    OFFICIAL_AUTHORITY_CONFIRMED,
    HOSTING_VERIFIED_LEGAL_UNCLEAR,
    THIRD_PARTY_INDEXED_CC0_SIGNAL,
    UNCLEAR,
}

enum class PlaceCategory {
    CENTER,
    BUS_STATION,
    TRAIN_STATION,
    SHOPPING,
    HEALTHCARE,
    EDUCATION,
    CULTURE,
    SPORTS,
    TOURISM,
    GOVERNMENT,
    OTHER,
}

enum class CoordinateConfidence {
    VERIFIED,
    APPROXIMATE,
    UNKNOWN,
}
