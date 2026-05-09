package ee.androbus.core.gtfs

import java.time.LocalDate

data class GtfsAgency(
    val agencyId: String,
    val agencyName: String,
)

data class GtfsStop(
    val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
)

data class GtfsRoute(
    val routeId: String,
    val agencyId: String?,
    val routeShortName: String?,
    val routeLongName: String?,
)

data class GtfsTrip(
    val routeId: String,
    val serviceId: String,
    val tripId: String,
    val tripHeadsign: String?,
)

data class GtfsStopTime(
    val tripId: String,
    val arrivalTime: String,
    val departureTime: String,
    val stopId: String,
    val stopSequence: Int,
)

data class GtfsCalendar(
    val serviceId: String,
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

data class GtfsCalendarDate(
    val serviceId: String,
    val date: LocalDate,
    val exceptionType: Int,
)

data class ParsedGtfsFeed(
    val agencies: List<GtfsAgency>,
    val stops: List<GtfsStop>,
    val routes: List<GtfsRoute>,
    val trips: List<GtfsTrip>,
    val stopTimes: List<GtfsStopTime>,
    val calendars: List<GtfsCalendar>,
    val calendarDates: List<GtfsCalendarDate>,
)
