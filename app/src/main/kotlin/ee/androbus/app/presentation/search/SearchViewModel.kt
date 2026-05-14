package ee.androbus.app.presentation.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.core.domain.StopPointId
import ee.androbus.core.routing.DirectRouteNotFoundReason
import ee.androbus.feature.search.bridge.DirectRouteQueryBridgeResult
import ee.androbus.feature.search.destination.DestinationTarget
import ee.androbus.feature.search.destination.DestinationTargetConfidence
import ee.androbus.feature.search.destination.DestinationTargetSource
import ee.androbus.feature.search.destination.PlaceToStopCandidateResolver
import ee.androbus.feature.search.destination.PlaceToStopCandidateResult
import ee.androbus.feature.search.orchestration.DestinationEnrichmentOrchestrator
import ee.androbus.feature.search.orchestration.DestinationEnrichmentResult
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationResult
import ee.androbus.feature.search.orchestration.DirectRouteQueryPreparationUseCase
import ee.androbus.feature.search.resolution.InMemoryStopPointIndex
import ee.androbus.feature.search.resolution.StopCandidateEnricher
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val snapshotProvider: DomainFeedSnapshotProvider,
        private val routeQueryPreparationUseCase: DirectRouteQueryPreparationUseCase,
    ) : ViewModel() {
        private val cityId = CityId("rakvere")
        private val placeToStopCandidateResolver = PlaceToStopCandidateResolver()

        private var latestDestinationEnrichment: DestinationEnrichmentResult = DestinationEnrichmentResult.NoCandidates
        private var selectedDestinationEnrichment: DestinationEnrichmentResult = DestinationEnrichmentResult.NoCandidates

        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        init {
            refreshFeedState()
        }

        fun refreshFeedState() {
            val feedState = if (snapshotProvider.getSnapshot(cityId) == null) FeedState.NotReady else FeedState.Ready
            _uiState.update { it.copy(feedState = feedState) }
        }

        fun onOriginStopPointChanged(originStopPointId: StopPointId?) {
            _uiState.update {
                it.copy(
                    originStopPointId = originStopPointId,
                    routeQueryState = RouteQueryState.Idle,
                )
            }
        }

        fun onDestinationChanged(text: String) {
            val query = text.trim()
            if (query.isBlank()) {
                latestDestinationEnrichment = DestinationEnrichmentResult.NoCandidates
                selectedDestinationEnrichment = DestinationEnrichmentResult.NoCandidates
                _uiState.update {
                    it.copy(
                        destinationInput = DestinationInputState.Empty,
                        routeQueryState = RouteQueryState.Idle,
                    )
                }
                return
            }

            latestDestinationEnrichment = DestinationEnrichmentResult.NoCandidates
            selectedDestinationEnrichment = DestinationEnrichmentResult.NoCandidates
            _uiState.update {
                it.copy(
                    destinationInput = DestinationInputState.Typed(query),
                    routeQueryState = RouteQueryState.Idle,
                )
            }
            resolveDestination(query)
        }

        fun onAmbiguousOptionSelected(option: ResolvedDestinationOption) {
            val selected = buildSelectedEnrichmentFromAmbiguous(option.stopPointId) ?: return
            selectedDestinationEnrichment = selected

            _uiState.update {
                it.copy(
                    destinationInput =
                        DestinationInputState.Resolved(
                            displayName = option.stopGroupName,
                            candidates = listOf(option),
                        ),
                    routeQueryState = RouteQueryState.Idle,
                )
            }
        }

        fun searchRoute() {
            _uiState.update { it.copy(routeQueryState = RouteQueryState.Searching) }

            val snapshot = snapshotProvider.getSnapshot(cityId)
            if (snapshot == null) {
                _uiState.update {
                    it.copy(
                        feedState = FeedState.NotReady,
                        routeQueryState = RouteQueryState.FeedNotAvailable,
                    )
                }
                return
            }

            _uiState.update { it.copy(feedState = FeedState.Ready) }

            val destinationEnrichment = routeReadyDestinationEnrichmentOrNull()
            if (destinationEnrichment == null) {
                _uiState.update { it.copy(routeQueryState = RouteQueryState.DestinationNotReady) }
                return
            }

            val originStopPointId = uiState.value.originStopPointId
            if (originStopPointId == null) {
                _uiState.update { it.copy(routeQueryState = RouteQueryState.OriginNotProvided) }
                return
            }

            if (snapshot.routePatterns.isEmpty()) {
                _uiState.update { it.copy(routeQueryState = RouteQueryState.NoPatternsAvailable) }
                return
            }

            val preparationResult =
                routeQueryPreparationUseCase.prepare(
                    destinationEnrichment = destinationEnrichment,
                    originStopPointId = originStopPointId,
                    patterns = snapshot.routePatterns,
                )

            _uiState.update { it.copy(routeQueryState = mapPreparationResultToRouteState(preparationResult)) }
        }

        private fun resolveDestination(query: String) {
            val snapshot = snapshotProvider.getSnapshot(cityId)
            if (snapshot == null) {
                _uiState.update { it.copy(feedState = FeedState.NotReady) }
                return
            }

            _uiState.update { it.copy(feedState = FeedState.Ready, destinationInput = DestinationInputState.Resolving) }

            val target =
                DestinationTarget(
                    id = "typed:${cityId.value}:$query",
                    displayName = query,
                    aliases = listOf(query),
                    source = DestinationTargetSource.CITY_PLACE_METADATA,
                    cityId = cityId,
                    preferredStopGroupNames = listOf(query),
                    confidence = DestinationTargetConfidence.UNCLEAR,
                    notes = "PASS 28A typed destination input.",
                )

            val candidateResult = placeToStopCandidateResolver.resolveCandidates(target)
            val candidates =
                when (candidateResult) {
                    is PlaceToStopCandidateResult.Found -> candidateResult.candidates
                    is PlaceToStopCandidateResult.NotFound -> emptyList()
                }

            val stopPointEnricher = StopCandidateEnricher(InMemoryStopPointIndex(snapshot.stopPoints))
            val enrichment =
                DestinationEnrichmentOrchestrator(stopCandidateEnricher = stopPointEnricher)
                    .enrichCandidates(candidates = candidates, cityId = cityId)

            latestDestinationEnrichment = enrichment
            selectedDestinationEnrichment =
                if (enrichment is DestinationEnrichmentResult.Enriched && !enrichment.isAmbiguous) {
                    enrichment
                } else {
                    DestinationEnrichmentResult.NoCandidates
                }

            val destinationInput = mapEnrichmentToState(enrichment)
            _uiState.update { it.copy(destinationInput = destinationInput) }
        }

        private fun mapEnrichmentToState(enrichment: DestinationEnrichmentResult): DestinationInputState {
            return when (enrichment) {
                DestinationEnrichmentResult.NoCandidates -> DestinationInputState.NotFound
                is DestinationEnrichmentResult.NoneEnriched -> DestinationInputState.NotFound
                is DestinationEnrichmentResult.Enriched -> {
                    val options =
                        enrichment.enrichedCandidates
                            .flatMap { enriched -> enriched.verifiedCandidates }
                            .map { verified ->
                                ResolvedDestinationOption(
                                    stopGroupName = verified.displayName,
                                    stopPointId = verified.stopPointId,
                                )
                            }
                            .distinctBy { option -> option.stopPointId }

                    when {
                        options.isEmpty() -> DestinationInputState.NotFound
                        enrichment.isAmbiguous || options.size > 1 -> DestinationInputState.Ambiguous(options)
                        else -> DestinationInputState.Resolved(displayName = options.single().stopGroupName, candidates = options)
                    }
                }
            }
        }

        private fun buildSelectedEnrichmentFromAmbiguous(stopPointId: StopPointId): DestinationEnrichmentResult.Enriched? {
            val enrichment = latestDestinationEnrichment as? DestinationEnrichmentResult.Enriched ?: return null
            if (!enrichment.isAmbiguous) return null

            val owningCandidate =
                enrichment.enrichedCandidates.firstOrNull { enriched ->
                    enriched.verifiedCandidates.any { verified -> verified.stopPointId == stopPointId }
                } ?: return null

            val selectedVerified = owningCandidate.verifiedCandidates.firstOrNull { it.stopPointId == stopPointId } ?: return null
            val selectedEnrichedCandidate = owningCandidate.enrichedCandidate.copy(stopPointIds = listOf(selectedVerified.stopPointId))
            val selectedEnriched =
                owningCandidate.copy(
                    enrichedCandidate = selectedEnrichedCandidate,
                    verifiedCandidates = listOf(selectedVerified),
                )

            return DestinationEnrichmentResult.Enriched(
                enrichedCandidates = listOf(selectedEnriched),
                failedCandidates = enrichment.failedCandidates,
                isAmbiguous = false,
            )
        }

        private fun routeReadyDestinationEnrichmentOrNull(): DestinationEnrichmentResult.Enriched? {
            val enrichment = selectedDestinationEnrichment as? DestinationEnrichmentResult.Enriched ?: return null
            val verifiedCount = enrichment.enrichedCandidates.sumOf { it.verifiedCandidates.size }
            if (enrichment.isAmbiguous || verifiedCount != 1) {
                return null
            }
            return enrichment
        }

        private fun mapPreparationResultToRouteState(result: DirectRouteQueryPreparationResult): RouteQueryState {
            return when (result) {
                is DirectRouteQueryPreparationResult.Executed -> mapBridgeResultToRouteState(result.bridgeResult)
                is DirectRouteQueryPreparationResult.DestinationAmbiguous -> RouteQueryState.DestinationNotReady
                DirectRouteQueryPreparationResult.DestinationUnresolved -> RouteQueryState.DestinationNotReady
                DirectRouteQueryPreparationResult.NoCandidates -> RouteQueryState.DestinationNotReady
                DirectRouteQueryPreparationResult.OriginNotProvided -> RouteQueryState.OriginNotProvided
                DirectRouteQueryPreparationResult.NoPatternsAvailable -> RouteQueryState.NoPatternsAvailable
            }
        }

        private fun mapBridgeResultToRouteState(result: DirectRouteQueryBridgeResult): RouteQueryState {
            return when (result) {
                is DirectRouteQueryBridgeResult.RouteFound -> {
                    val first = result.result.candidates.firstOrNull()
                        ?: return RouteQueryState.Error("RouteFound was returned without candidates.")

                    RouteQueryState.RouteFound(
                        route =
                            RouteFoundSummary(
                                routePatternId = first.routePatternId,
                                originStopPointId = first.originStopPointId,
                                destinationStopPointId = first.destinationStopPointId,
                                originSequence = first.originSequence,
                                destinationSequence = first.destinationSequence,
                                segmentStopCount = first.segmentStopCount,
                                segmentStopPointIds = first.segmentStopPointIds,
                                candidateCount = result.result.candidates.size,
                            ),
                    )
                }

                is DirectRouteQueryBridgeResult.RouteNotFound -> {
                    RouteQueryState.RouteNotFound(
                        reason = mapNotFoundReason(result.result.reason),
                    )
                }

                DirectRouteQueryBridgeResult.NotReady.OriginUnresolved -> RouteQueryState.OriginNotProvided
                DirectRouteQueryBridgeResult.NotReady.DestinationUnresolved -> RouteQueryState.DestinationNotReady
                DirectRouteQueryBridgeResult.NotReady.BothUnresolved -> RouteQueryState.DestinationNotReady
                DirectRouteQueryBridgeResult.NotReady.NoPatternsAvailable -> RouteQueryState.NoPatternsAvailable
            }
        }

        private fun mapNotFoundReason(reason: DirectRouteNotFoundReason): RouteNotFoundDisplayReason {
            return when (reason) {
                DirectRouteNotFoundReason.ORIGIN_NOT_FOUND -> RouteNotFoundDisplayReason.ORIGIN_NOT_FOUND
                DirectRouteNotFoundReason.DESTINATION_NOT_FOUND -> RouteNotFoundDisplayReason.DESTINATION_NOT_FOUND
                DirectRouteNotFoundReason.SAME_STOP -> RouteNotFoundDisplayReason.SAME_STOP
                DirectRouteNotFoundReason.NO_DIRECT_PATTERN -> RouteNotFoundDisplayReason.NO_DIRECT_PATTERN
                DirectRouteNotFoundReason.DESTINATION_NOT_AFTER_ORIGIN -> RouteNotFoundDisplayReason.DESTINATION_NOT_AFTER_ORIGIN
            }
        }

    }
