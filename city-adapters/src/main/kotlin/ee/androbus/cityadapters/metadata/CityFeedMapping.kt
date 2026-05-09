package ee.androbus.cityadapters.metadata

import ee.androbus.core.domain.FeedId

data class CityFeedMapping(
    val feedId: FeedId,
    val feedName: String,
    val sourceUrl: String,
    val authorityName: String,
    val hostingUrl: String? = null,
    val feedScope: FeedScope,
    val mappingConfidence: MappingConfidence,
    val legalStatus: LegalStatus,
    val notes: String? = null,
) {
    init {
        require(feedName.isNotBlank()) { "CityFeedMapping feedName cannot be blank." }
        require(sourceUrl.isNotBlank()) { "CityFeedMapping sourceUrl cannot be blank." }
        require(authorityName.isNotBlank()) { "CityFeedMapping authorityName cannot be blank." }
        if (hostingUrl != null) {
            require(hostingUrl.isNotBlank()) { "CityFeedMapping hostingUrl cannot be blank when provided." }
        }
        if (notes != null) {
            require(notes.isNotBlank()) { "CityFeedMapping notes cannot be blank when provided." }
        }
    }
}
