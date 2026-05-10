package ee.androbus.feature.search.destination

import kotlin.test.Test
import kotlin.test.assertEquals

class DestinationQueryNormalizerTest {
    private val normalizer = DestinationQueryNormalizer()

    @Test
    fun `strict normalization trims and lowercases without collapsing internal spaces`() {
        assertEquals("rakvere   bussijaam", normalizer.normalizeForStrictMatch("  Rakvere   BUSSIJAAM  "))
    }

    @Test
    fun `flexible normalization collapses spaces and punctuation`() {
        assertEquals("rakvere bussijaam", normalizer.normalizeForFlexibleMatch("  Rakvere - bussijaam  "))
    }

    @Test
    fun `flexible normalization keeps estonian letters`() {
        assertEquals("pohjakeskus", normalizer.normalizeForFlexibleMatch("Pohjakeskus"))
        assertEquals("põhjakeskus", normalizer.normalizeForFlexibleMatch("Põhjakeskus"))
    }
}
