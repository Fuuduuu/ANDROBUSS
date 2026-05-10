package ee.androbus.feature.search.bridge

import ee.androbus.core.domain.RoutePattern
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.routing.DirectRouteSearch
import ee.androbus.core.routing.DirectRouteSearchResult
import ee.androbus.feature.search.destination.StopCandidate
import ee.androbus.feature.search.origin.OriginCandidate

fun interface DirectRouteSearchPort {
    fun findDirectRoutes(
        origin: StopPointId,
        destination: StopPointId,
        patterns: List<RoutePattern>,
    ): DirectRouteSearchResult
}

class DirectRouteQueryBridge(
    private val routeSearch: DirectRouteSearchPort,
) {
    constructor(directRouteSearch: DirectRouteSearch) : this(
        routeSearch =
            DirectRouteSearchPort { origin, destination, patterns ->
                directRouteSearch.findDirectRoutes(origin, destination, patterns)
            },
    )

    fun query(
        originCandidates: List<OriginCandidate>,
        destinationCandidates: List<StopCandidate>,
        patterns: List<RoutePattern>,
    ): DirectRouteQueryBridgeResult {
        val resolvedOriginIds = originCandidates.flatMap { it.stopPointIds }
        val resolvedDestinationIds = destinationCandidates.flatMap { it.stopPointIds }

        if (resolvedOriginIds.isEmpty() && resolvedDestinationIds.isEmpty()) {
            return DirectRouteQueryBridgeResult.NotReady.BothUnresolved
        }
        if (resolvedOriginIds.isEmpty()) {
            return DirectRouteQueryBridgeResult.NotReady.OriginUnresolved
        }
        if (resolvedDestinationIds.isEmpty()) {
            return DirectRouteQueryBridgeResult.NotReady.DestinationUnresolved
        }
        if (patterns.isEmpty()) {
            return DirectRouteQueryBridgeResult.NotReady.NoPatternsAvailable
        }

        // Deterministic minimal policy: pick first explicitly resolved IDs only.
        val origin = resolvedOriginIds.first()
        val destination = resolvedDestinationIds.first()
        val result = routeSearch.findDirectRoutes(origin = origin, destination = destination, patterns = patterns)

        return when (result) {
            is DirectRouteSearchResult.Found -> DirectRouteQueryBridgeResult.RouteFound(result)
            is DirectRouteSearchResult.NotFound -> DirectRouteQueryBridgeResult.RouteNotFound(result)
        }
    }
}

