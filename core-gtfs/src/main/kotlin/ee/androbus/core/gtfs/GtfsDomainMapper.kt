package ee.androbus.core.gtfs

import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.FeedId
import ee.androbus.core.domain.AgencyId
import ee.androbus.core.domain.PatternStop
import ee.androbus.core.domain.RouteLine
import ee.androbus.core.domain.RouteLineId
import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.RoutePatternId
import ee.androbus.core.domain.ServiceCalendar
import ee.androbus.core.domain.ServiceCalendarException
import ee.androbus.core.domain.ServiceExceptionType
import ee.androbus.core.domain.ServiceId
import ee.androbus.core.domain.ServiceRef
import ee.androbus.core.domain.StopGroup
import ee.androbus.core.domain.StopGroupId
import ee.androbus.core.domain.StopPoint
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.domain.Trip
import ee.androbus.core.domain.TripId
import ee.androbus.core.domain.GeoPoint
import java.time.DayOfWeek

data class MappedGtfsFeed(
    val stopGroups: List<StopGroup>,
    val stopPoints: List<StopPoint>,
    val routeLines: List<RouteLine>,
    val routePatterns: List<RoutePattern>,
    val trips: List<Trip>,
    val serviceCalendars: List<ServiceCalendar>,
    val serviceCalendarExceptions: List<ServiceCalendarException>,
)

class GtfsDomainMapper {
    fun map(
        parsedFeed: ParsedGtfsFeed,
        cityId: CityId,
        feedId: FeedId,
    ): MappedGtfsFeed {
        val stopPointsById = linkedMapOf<String, GtfsStop>()
        parsedFeed.stops.forEach { stop ->
            val previous = stopPointsById.putIfAbsent(stop.stopId, stop)
            if (previous != null) {
                throw GtfsParseException("Duplicate stop_id '${stop.stopId}' is not supported.")
            }
        }

        val stopGroups = parsedFeed.stops.map { stop ->
            StopGroup(
                id = StopGroupId("group:${stop.stopId}"),
                displayName = stop.stopName,
                cityId = cityId,
                centroid = GeoPoint(stop.stopLat, stop.stopLon),
            )
        }

        val stopPoints = parsedFeed.stops.map { stop ->
            StopPoint(
                id = StopPointId(stop.stopId),
                stopGroupId = StopGroupId("group:${stop.stopId}"),
                displayName = stop.stopName,
                location = GeoPoint(stop.stopLat, stop.stopLon),
                cityId = cityId,
                feedId = feedId,
            )
        }

        val routeLinesById = linkedMapOf<String, RouteLine>()
        parsedFeed.routes.forEach { route ->
            val displayName = route.routeShortName ?: route.routeLongName ?: route.routeId
            val routeLine = RouteLine(
                id = RouteLineId(route.routeId),
                displayName = displayName,
                cityId = cityId,
                agencyId = route.agencyId?.let(::AgencyId),
                feedId = feedId,
            )
            val previous = routeLinesById.putIfAbsent(route.routeId, routeLine)
            if (previous != null) {
                throw GtfsParseException("Duplicate route_id '${route.routeId}' is not supported.")
            }
        }

        val stopTimesByTripId = parsedFeed.stopTimes.groupBy { it.tripId }
        val routePatterns = mutableListOf<RoutePattern>()
        val trips = mutableListOf<Trip>()

        parsedFeed.trips.forEach { trip ->
            val routeLine = routeLinesById[trip.routeId]
                ?: throw GtfsParseException("Trip '${trip.tripId}' references unknown route_id '${trip.routeId}'.")

            val orderedStopTimes = stopTimesByTripId[trip.tripId]
                .orEmpty()
                .sortedBy { it.stopSequence }

            if (orderedStopTimes.size < 2) {
                throw GtfsParseException("Trip '${trip.tripId}' must contain at least two stop_times rows.")
            }

            val patternStops = orderedStopTimes.map { stopTime ->
                if (!stopPointsById.containsKey(stopTime.stopId)) {
                    throw GtfsParseException(
                        "Trip '${trip.tripId}' references unknown stop_id '${stopTime.stopId}' in stop_times.",
                    )
                }
                PatternStop(
                    sequence = stopTime.stopSequence,
                    stopPointId = StopPointId(stopTime.stopId),
                )
            }

            val patternId = RoutePatternId("pattern:${trip.tripId}")
            val patternDisplayName = trip.tripHeadsign ?: routeLine.displayName
            routePatterns += RoutePattern(
                id = patternId,
                routeLineId = routeLine.id,
                displayName = patternDisplayName,
                cityId = cityId,
                stops = patternStops,
                feedId = feedId,
            )

            trips += Trip(
                id = TripId(trip.tripId),
                routePatternId = patternId,
                service = ServiceRef(ServiceId(trip.serviceId)),
                headsign = trip.tripHeadsign,
            )
        }

        val serviceCalendars = parsedFeed.calendars.map { calendar ->
            ServiceCalendar(
                serviceId = ServiceId(calendar.serviceId),
                activeDays = buildSet {
                    if (calendar.monday) add(DayOfWeek.MONDAY)
                    if (calendar.tuesday) add(DayOfWeek.TUESDAY)
                    if (calendar.wednesday) add(DayOfWeek.WEDNESDAY)
                    if (calendar.thursday) add(DayOfWeek.THURSDAY)
                    if (calendar.friday) add(DayOfWeek.FRIDAY)
                    if (calendar.saturday) add(DayOfWeek.SATURDAY)
                    if (calendar.sunday) add(DayOfWeek.SUNDAY)
                },
                startDate = calendar.startDate,
                endDate = calendar.endDate,
            )
        }

        val serviceCalendarExceptions = parsedFeed.calendarDates.map { calendarDate ->
            val mappedExceptionType = when (calendarDate.exceptionType) {
                1 -> ServiceExceptionType.ADD_SERVICE
                2 -> ServiceExceptionType.REMOVE_SERVICE
                else -> {
                    throw GtfsParseException(
                        "Unsupported exception_type '${calendarDate.exceptionType}' for service_id " +
                            "'${calendarDate.serviceId}' on ${calendarDate.date}.",
                    )
                }
            }

            ServiceCalendarException(
                serviceId = ServiceId(calendarDate.serviceId),
                date = calendarDate.date,
                exceptionType = mappedExceptionType,
            )
        }

        return MappedGtfsFeed(
            stopGroups = stopGroups,
            stopPoints = stopPoints,
            routeLines = routeLinesById.values.toList(),
            routePatterns = routePatterns,
            trips = trips,
            serviceCalendars = serviceCalendars,
            serviceCalendarExceptions = serviceCalendarExceptions,
        )
    }
}
