package ee.androbus.feature.search.bridge

import ee.androbus.core.routing.DirectRouteSearchResult

sealed interface DirectRouteQueryBridgeResult {
    data class RouteFound(
        val result: DirectRouteSearchResult.Found,
    ) : DirectRouteQueryBridgeResult

    data class RouteNotFound(
        val result: DirectRouteSearchResult.NotFound,
    ) : DirectRouteQueryBridgeResult

    sealed interface NotReady : DirectRouteQueryBridgeResult {
        object OriginUnresolved : NotReady

        object DestinationUnresolved : NotReady

        object BothUnresolved : NotReady

        object NoPatternsAvailable : NotReady
    }
}

