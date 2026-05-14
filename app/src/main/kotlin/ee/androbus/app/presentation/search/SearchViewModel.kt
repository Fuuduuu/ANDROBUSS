package ee.androbus.app.presentation.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.androbus.core.domain.CityId
import ee.androbus.core.domain.DomainFeedSnapshotProvider
import ee.androbus.feature.search.destination.DestinationTarget
import ee.androbus.feature.search.destination.DestinationTargetConfidence
import ee.androbus.feature.search.destination.DestinationTargetSource
import ee.androbus.feature.search.destination.PlaceToStopCandidateResolver
import ee.androbus.feature.search.destination.PlaceToStopCandidateResult
import ee.androbus.feature.search.orchestration.DestinationEnrichmentOrchestrator
import ee.androbus.feature.search.orchestration.DestinationEnrichmentResult
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
    ) : ViewModel() {
        private val cityId = CityId("rakvere")
        private val placeToStopCandidateResolver = PlaceToStopCandidateResolver()

        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        init {
            refreshFeedState()
        }

        fun refreshFeedState() {
            val feedState = if (snapshotProvider.getSnapshot(cityId) == null) FeedState.NotReady else FeedState.Ready
            _uiState.update { it.copy(feedState = feedState) }
        }

        fun onDestinationChanged(text: String) {
            val query = text.trim()
            if (query.isBlank()) {
                _uiState.update { it.copy(destinationInput = DestinationInputState.Empty) }
                return
            }

            _uiState.update { it.copy(destinationInput = DestinationInputState.Typed(query)) }
            resolveDestination(query)
        }

        fun onAmbiguousOptionSelected(option: ResolvedDestinationOption) {
            _uiState.update {
                it.copy(
                    destinationInput =
                        DestinationInputState.Resolved(
                            displayName = option.stopGroupName,
                            candidates = listOf(option),
                        ),
                )
            }
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
    }
