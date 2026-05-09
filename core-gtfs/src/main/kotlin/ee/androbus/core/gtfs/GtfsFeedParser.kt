package ee.androbus.core.gtfs

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class GtfsFeedParser(
    private val csvTableReader: CsvTableReader = CsvTableReader(),
) {
    fun parseDirectory(directory: Path): ParsedGtfsFeed {
        if (!Files.isDirectory(directory)) {
            throw GtfsParseException("GTFS directory does not exist: $directory")
        }

        val requiredFiles = listOf(
            AGENCY_FILE,
            STOPS_FILE,
            ROUTES_FILE,
            TRIPS_FILE,
            STOP_TIMES_FILE,
        )
        requiredFiles.forEach { fileName ->
            if (!Files.exists(directory.resolve(fileName))) {
                throw GtfsParseException("Missing required GTFS file: $fileName")
            }
        }

        val calendarPath = directory.resolve(CALENDAR_FILE)
        val calendarDatesPath = directory.resolve(CALENDAR_DATES_FILE)
        val hasCalendar = Files.exists(calendarPath)
        val hasCalendarDates = Files.exists(calendarDatesPath)
        if (!hasCalendar && !hasCalendarDates) {
            throw GtfsParseException(
                "Missing calendar files: at least one of $CALENDAR_FILE or $CALENDAR_DATES_FILE must exist.",
            )
        }

        val agencies = parseAgencies(readTable(directory, AGENCY_FILE), AGENCY_FILE)
        val stops = parseStops(readTable(directory, STOPS_FILE), STOPS_FILE)
        val routes = parseRoutes(readTable(directory, ROUTES_FILE), ROUTES_FILE)
        val trips = parseTrips(readTable(directory, TRIPS_FILE), TRIPS_FILE)
        val stopTimes = parseStopTimes(readTable(directory, STOP_TIMES_FILE), STOP_TIMES_FILE)
        val calendars = if (hasCalendar) {
            parseCalendars(readTable(directory, CALENDAR_FILE), CALENDAR_FILE)
        } else {
            emptyList()
        }
        val calendarDates = if (hasCalendarDates) {
            parseCalendarDates(readTable(directory, CALENDAR_DATES_FILE), CALENDAR_DATES_FILE)
        } else {
            emptyList()
        }

        return ParsedGtfsFeed(
            agencies = agencies,
            stops = stops,
            routes = routes,
            trips = trips,
            stopTimes = stopTimes,
            calendars = calendars,
            calendarDates = calendarDates,
        )
    }

    private fun readTable(directory: Path, fileName: String): CsvTable =
        csvTableReader.read(directory.resolve(fileName))

    private fun parseAgencies(table: CsvTable, fileName: String): List<GtfsAgency> {
        requireColumns(table, fileName, "agency_name")
        return table.rows.mapIndexed { index, row ->
            GtfsAgency(
                agencyId = row.optional("agency_id") ?: "generated-agency-${index + 1}",
                agencyName = row.required("agency_name"),
            )
        }
    }

    private fun parseStops(table: CsvTable, fileName: String): List<GtfsStop> {
        requireColumns(table, fileName, "stop_id", "stop_name", "stop_lat", "stop_lon")
        return table.rows.map { row ->
            GtfsStop(
                stopId = row.required("stop_id"),
                stopName = row.required("stop_name"),
                stopLat = parseDouble(row.required("stop_lat"), fileName, "stop_lat"),
                stopLon = parseDouble(row.required("stop_lon"), fileName, "stop_lon"),
            )
        }
    }

    private fun parseRoutes(table: CsvTable, fileName: String): List<GtfsRoute> {
        requireColumns(table, fileName, "route_id")
        return table.rows.map { row ->
            GtfsRoute(
                routeId = row.required("route_id"),
                agencyId = row.optional("agency_id"),
                routeShortName = row.optional("route_short_name"),
                routeLongName = row.optional("route_long_name"),
            )
        }
    }

    private fun parseTrips(table: CsvTable, fileName: String): List<GtfsTrip> {
        requireColumns(table, fileName, "route_id", "service_id", "trip_id")
        return table.rows.map { row ->
            GtfsTrip(
                routeId = row.required("route_id"),
                serviceId = row.required("service_id"),
                tripId = row.required("trip_id"),
                tripHeadsign = row.optional("trip_headsign"),
            )
        }
    }

    private fun parseStopTimes(table: CsvTable, fileName: String): List<GtfsStopTime> {
        requireColumns(table, fileName, "trip_id", "arrival_time", "departure_time", "stop_id", "stop_sequence")
        return table.rows.map { row ->
            GtfsStopTime(
                tripId = row.required("trip_id"),
                arrivalTime = row.required("arrival_time"),
                departureTime = row.required("departure_time"),
                stopId = row.required("stop_id"),
                stopSequence = parseInt(row.required("stop_sequence"), fileName, "stop_sequence"),
            )
        }
    }

    private fun parseCalendars(table: CsvTable, fileName: String): List<GtfsCalendar> {
        requireColumns(
            table,
            fileName,
            "service_id",
            "monday",
            "tuesday",
            "wednesday",
            "thursday",
            "friday",
            "saturday",
            "sunday",
            "start_date",
            "end_date",
        )
        return table.rows.map { row ->
            GtfsCalendar(
                serviceId = row.required("service_id"),
                monday = parseDayFlag(row.required("monday"), fileName, "monday"),
                tuesday = parseDayFlag(row.required("tuesday"), fileName, "tuesday"),
                wednesday = parseDayFlag(row.required("wednesday"), fileName, "wednesday"),
                thursday = parseDayFlag(row.required("thursday"), fileName, "thursday"),
                friday = parseDayFlag(row.required("friday"), fileName, "friday"),
                saturday = parseDayFlag(row.required("saturday"), fileName, "saturday"),
                sunday = parseDayFlag(row.required("sunday"), fileName, "sunday"),
                startDate = parseDate(row.required("start_date"), fileName, "start_date"),
                endDate = parseDate(row.required("end_date"), fileName, "end_date"),
            )
        }
    }

    private fun parseCalendarDates(table: CsvTable, fileName: String): List<GtfsCalendarDate> {
        requireColumns(table, fileName, "service_id", "date", "exception_type")
        return table.rows.map { row ->
            GtfsCalendarDate(
                serviceId = row.required("service_id"),
                date = parseDate(row.required("date"), fileName, "date"),
                exceptionType = parseInt(row.required("exception_type"), fileName, "exception_type"),
            )
        }
    }

    private fun requireColumns(table: CsvTable, fileName: String, vararg columns: String) {
        val missing = columns.filterNot(table.headers::contains)
        if (missing.isNotEmpty()) {
            throw GtfsParseException(
                "Missing required column(s) in $fileName: ${missing.joinToString(", ")}",
            )
        }
    }

    private fun parseInt(value: String, fileName: String, column: String): Int =
        value.toIntOrNull()
            ?: throw GtfsParseException("Invalid integer value '$value' for $column in $fileName.")

    private fun parseDouble(value: String, fileName: String, column: String): Double =
        value.toDoubleOrNull()
            ?: throw GtfsParseException("Invalid numeric value '$value' for $column in $fileName.")

    private fun parseDayFlag(value: String, fileName: String, column: String): Boolean =
        when (value) {
            "0" -> false
            "1" -> true
            else -> throw GtfsParseException("Invalid day flag '$value' for $column in $fileName. Expected 0 or 1.")
        }

    private fun parseDate(value: String, fileName: String, column: String): LocalDate =
        try {
            LocalDate.parse(value, GTFS_DATE_FORMAT)
        } catch (error: DateTimeParseException) {
            throw GtfsParseException("Invalid GTFS date '$value' for $column in $fileName. Expected YYYYMMDD.", error)
        }

    private companion object {
        val GTFS_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE

        const val AGENCY_FILE = "agency.txt"
        const val STOPS_FILE = "stops.txt"
        const val ROUTES_FILE = "routes.txt"
        const val TRIPS_FILE = "trips.txt"
        const val STOP_TIMES_FILE = "stop_times.txt"
        const val CALENDAR_FILE = "calendar.txt"
        const val CALENDAR_DATES_FILE = "calendar_dates.txt"
    }
}
