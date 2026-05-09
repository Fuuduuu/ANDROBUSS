package ee.androbus.core.gtfs

import kotlin.test.Test
import kotlin.test.assertEquals

class CsvTableReaderTest {
    private val reader = CsvTableReader()

    @Test
    fun `parses quoted comma field`() {
        val table = reader.read(
            """
            col_a,col_b
            "Rakvere, test",value
            """.trimIndent(),
        )

        assertEquals("Rakvere, test", table.rows.single().required("col_a"))
        assertEquals("value", table.rows.single().required("col_b"))
    }

    @Test
    fun `parses escaped quotes in quoted field`() {
        val table = reader.read(
            """
            message
            "He said ""tere"" to rider"
            """.trimIndent(),
        )

        assertEquals("""He said "tere" to rider""", table.rows.single().required("message"))
    }

    @Test
    fun `handles CRLF and LF line endings`() {
        val table = reader.read("a,b\r\n1,2\r\n3,4\n")

        assertEquals(2, table.rows.size)
        assertEquals("1", table.rows[0].required("a"))
        assertEquals("2", table.rows[0].required("b"))
        assertEquals("3", table.rows[1].required("a"))
        assertEquals("4", table.rows[1].required("b"))
    }
}
