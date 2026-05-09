package ee.androbus.core.gtfs

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GtfsFeedParserTest {
    private val parser = GtfsFeedParser()

    @Test
    fun `parser loads rakvere smoke fixture`() {
        val feed = parser.parseDirectory(resourcePath("gtfs/rakvere-smoke"))

        assertEquals(1, feed.agencies.size)
        assertEquals(4, feed.stops.size)
        assertEquals(2, feed.routes.size)
        assertEquals(3, feed.trips.size)
        assertEquals(9, feed.stopTimes.size)
        assertEquals(2, feed.calendars.size)
        assertEquals(4, feed.calendarDates.size)
    }

    @Test
    fun `parser fails when mandatory file is missing`() {
        val files = minimalFeedFiles().toMutableMap()
        files.remove("stops.txt")
        val directory = writeFeed(files)

        val error = assertFailsWith<GtfsParseException> {
            parser.parseDirectory(directory)
        }

        assertTrue(error.message.orEmpty().contains("stops.txt"))
    }

    @Test
    fun `parser accepts calendar_dates only feed when calendar missing`() {
        val files = minimalFeedFiles(includeCalendar = false, includeCalendarDates = true)
        val directory = writeFeed(files)

        val feed = parser.parseDirectory(directory)

        assertTrue(feed.calendars.isEmpty())
        assertEquals(1, feed.calendarDates.size)
    }

    @Test
    fun `parser fails when both calendar files are absent`() {
        val files = minimalFeedFiles(includeCalendar = false, includeCalendarDates = false)
        val directory = writeFeed(files)

        val error = assertFailsWith<GtfsParseException> {
            parser.parseDirectory(directory)
        }

        val message = error.message.orEmpty()
        assertTrue(message.contains("calendar.txt"))
        assertTrue(message.contains("calendar_dates.txt"))
    }

    private fun resourcePath(relativePath: String): Path {
        val resource = checkNotNull(javaClass.classLoader.getResource(relativePath)) {
            "Missing test resource: $relativePath"
        }
        return Path.of(resource.toURI())
    }

    private fun writeFeed(files: Map<String, String>): Path {
        val directory = Files.createTempDirectory("androbuss-gtfs-parser-test-")
        files.forEach { (fileName, content) ->
            Files.writeString(directory.resolve(fileName), content)
        }
        return directory
    }

    private fun minimalFeedFiles(
        includeCalendar: Boolean = true,
        includeCalendarDates: Boolean = true,
    ): Map<String, String> {
        val files = mutableMapOf(
            "agency.txt" to """
                agency_id,agency_name
                a1,Test Agency
            """.trimIndent(),
            "stops.txt" to """
                stop_id,stop_name,stop_lat,stop_lon
                S1,Stop One,59.30,26.30
                S2,Stop Two,59.31,26.31
            """.trimIndent(),
            "routes.txt" to """
                route_id,agency_id,route_short_name,route_long_name
                R1,a1,1,Line 1
            """.trimIndent(),
            "trips.txt" to """
                route_id,service_id,trip_id,trip_headsign
                R1,WKD,T1,Center
            """.trimIndent(),
            "stop_times.txt" to """
                trip_id,arrival_time,departure_time,stop_id,stop_sequence
                T1,08:00:00,08:00:00,S1,1
                T1,08:05:00,08:05:00,S2,2
            """.trimIndent(),
        )

        if (includeCalendar) {
            files["calendar.txt"] = """
                service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
                WKD,1,1,1,1,1,0,0,20260501,20260531
            """.trimIndent()
        }
        if (includeCalendarDates) {
            files["calendar_dates.txt"] = """
                service_id,date,exception_type
                WKD,20260510,1
            """.trimIndent()
        }
        return files
    }
}
