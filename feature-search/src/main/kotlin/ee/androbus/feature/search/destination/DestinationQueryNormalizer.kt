package ee.androbus.feature.search.destination

import java.util.Locale

class DestinationQueryNormalizer {
    private val estonianLocale = Locale.forLanguageTag("et-EE")

    fun normalizeForStrictMatch(input: String): String =
        input
            .trim()
            .lowercase(estonianLocale)

    fun normalizeForFlexibleMatch(input: String): String =
        collapseWhitespace(removeSimplePunctuation(normalizeForStrictMatch(input)))

    private fun removeSimplePunctuation(value: String): String =
        value.replace(Regex("[\\p{Punct}]+"), " ")

    private fun collapseWhitespace(value: String): String =
        value.replace(Regex("\\s+"), " ").trim()
}
