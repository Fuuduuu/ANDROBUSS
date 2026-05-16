package ee.androbus.app.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ee.androbus.core.domain.StopPointId

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchContent(
        uiState = uiState,
        onRefreshFeed = viewModel::refreshFeedState,
        onDestinationSelect = viewModel::onDestinationChanged,
        onOriginSelected = viewModel::onOriginStopPointChanged,
        onAmbiguousOptionSelected = viewModel::onAmbiguousOptionSelected,
        onSearch = viewModel::searchRoute,
    )
}

@Composable
fun SearchContent(
    uiState: SearchUiState,
    onRefreshFeed: () -> Unit,
    onDestinationSelect: (String) -> Unit,
    onOriginSelected: (StopPointId?) -> Unit,
    onAmbiguousOptionSelected: (ResolvedDestinationOption) -> Unit,
    onSearch: () -> Unit,
) {
    var destinationText by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "ANDROBUSS",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Vali sihtkoht ja päritolu ning otsi marsruuti sõiduplaani järgi.",
            style = MaterialTheme.typography.bodyMedium,
        )

        FeedStatusBanner(
            feedState = uiState.feedState,
            onRefreshFeed = onRefreshFeed,
        )

        QuickDestinationSection(
            onQuickDestinationSelected = { label, queryText ->
                handleQuickDestinationSelection(
                    label = label,
                    queryText = queryText,
                    setDestinationText = { destinationText = it },
                    onDestinationSelect = onDestinationSelect,
                )
            },
        )

        DestinationSection(
            destinationText = destinationText,
            onDestinationTextChanged = { destinationText = it },
            onDestinationSelect = { onDestinationSelect(destinationText) },
            destinationInputState = uiState.destinationInput,
            onAmbiguousOptionSelected = onAmbiguousOptionSelected,
        )

        OriginSection(
            originCandidates = uiState.originCandidates,
            selectedOrigin = uiState.originStopPointId,
            onOriginSelected = onOriginSelected,
        )

        Button(
            onClick = onSearch,
            enabled = isSearchButtonEnabled(uiState),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Text("Otsi")
        }

        RouteResultSection(
            routeQueryState = uiState.routeQueryState,
        )

        Text(
            text = "Bussiandmed: Ühistranspordiregister / Regionaal- ja Põllumajandusministeerium",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun QuickDestinationSection(
    onQuickDestinationSelected: (label: String, queryText: String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(QUICK_DESTINATION_SECTION_TITLE, style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickDestinationOptions().forEach { option ->
                    FilterChip(
                        selected = false,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onQuickDestinationSelected(option.label, option.queryText) },
                        label = { Text(option.label) },
                    )
                }
            }
        }
    }
}

@Composable
fun FeedStatusBanner(
    feedState: FeedState,
    onRefreshFeed: () -> Unit,
) {
    if (!shouldShowFeedStatusBanner(feedState)) {
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = feedStateTitle(feedState),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = feedStateMessage(feedState),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (feedState is FeedState.NotReady || feedState is FeedState.Error) {
                Button(onClick = onRefreshFeed) {
                    Text("Kontrolli uuesti")
                }
            }
        }
    }
}

@Composable
fun DestinationSection(
    destinationText: String,
    onDestinationTextChanged: (String) -> Unit,
    onDestinationSelect: () -> Unit,
    destinationInputState: DestinationInputState,
    onAmbiguousOptionSelected: (ResolvedDestinationOption) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Sihtkoht", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = destinationText,
                onValueChange = onDestinationTextChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Sisesta sihtkoht") },
                singleLine = true,
            )
            Button(
                onClick = onDestinationSelect,
                enabled = destinationText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Vali sihtkoht")
            }
            Text(
                text = destinationStateMessage(destinationInputState),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (destinationInputState is DestinationInputState.Ambiguous) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    destinationInputState.options.forEachIndexed { index, option ->
                        Button(
                            onClick = { onAmbiguousOptionSelected(option) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("${index + 1}. ${option.stopGroupName}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OriginSection(
    originCandidates: List<OriginCandidateGroup>,
    selectedOrigin: StopPointId?,
    onOriginSelected: (StopPointId?) -> Unit,
) {
    var expandedGroupId by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val orderedGroups = orderedOriginCandidateGroups(originCandidates)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Päritolu", style = MaterialTheme.typography.titleMedium)
            // TODO PASS 33: full origin search dialog for all stop groups.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (orderedGroups.isEmpty()) {
                    Text(
                        text = "Päritolu valikuid ei leitud aktiivsest andmestikust.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                orderedGroups.forEach { group ->
                    val isSelected = group.options.any { option -> option.stopPointId == selectedOrigin }
                    FilterChip(
                        selected = isSelected,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val outcome = resolveOriginGroupTap(group = group, currentlyExpandedGroupId = expandedGroupId)
                            expandedGroupId = outcome.expandedGroupId
                            outcome.selectedStopPointId?.let { onOriginSelected(it) }
                        },
                        label = {
                            if (group.options.size == 1) {
                                Text(group.displayName)
                            } else {
                                Text("${group.displayName} (${group.options.size})")
                            }
                        },
                    )

                    if (group.options.size > 1 && expandedGroupId == group.groupId) {
                        Column(
                            modifier = Modifier.padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            group.options.forEach { option ->
                                FilterChip(
                                    selected = selectedOrigin == option.stopPointId,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onOriginSelected(option.stopPointId) },
                                    label = { Text(originOptionLabel(option)) },
                                )
                            }
                        }
                    }
                }
            }
            if (selectedOrigin != null) {
                Button(onClick = { onOriginSelected(null) }) {
                    Text("Eemalda päritolu valik")
                }
            }
        }
    }
}

@Composable
fun RouteResultSection(
    routeQueryState: RouteQueryState,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Tulemus", style = MaterialTheme.typography.titleMedium)
            Text(
                text = routeStateMessage(routeQueryState),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (routeQueryState is RouteQueryState.RouteFound) {
                val lines = routeFoundSummaryLines(routeQueryState.route)
                Text(lines.first(), style = MaterialTheme.typography.bodyLarge)
                lines.drop(1).forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

internal data class QuickDestinationOption(
    val label: String,
    val queryText: String,
)

internal data class OriginGroupTapOutcome(
    val selectedStopPointId: StopPointId?,
    val expandedGroupId: String?,
)

internal const val QUICK_DESTINATION_SECTION_TITLE = "Kiirvalikud"

internal fun feedStateTitle(feedState: FeedState): String =
    when (feedState) {
        FeedState.NotReady -> "Bussiandmeid laaditakse"
        FeedState.Ready -> "Bussiandmed valmis"
        is FeedState.Error -> "Bussiandmeid ei saanud hetkel ette valmistada"
    }

internal fun feedStateMessage(feedState: FeedState): String =
    when (feedState) {
        FeedState.NotReady -> "Andmed valmistatakse ette. Otsi marsruuti sõiduplaani järgi pärast andmete valmimist."
        FeedState.Ready -> "Saad otsida marsruuti sõiduplaani järgi."
        is FeedState.Error -> "Andmete ettevalmistus ebaõnnestus: ${feedState.message}"
    }

internal fun destinationStateMessage(state: DestinationInputState): String =
    when (state) {
        DestinationInputState.Empty -> "Sisesta sihtkoht ja vajuta \"Vali sihtkoht\"."
        is DestinationInputState.Typed -> "Valitud tekst: ${state.text}"
        DestinationInputState.Resolving -> "Sihtkoha vasteid kontrollitakse."
        is DestinationInputState.Resolved -> "Sihtkoht valitud: ${state.displayName}"
        is DestinationInputState.Ambiguous -> "Leiti mitu varianti. Vali sobiv peatus."
        DestinationInputState.NotFound -> "Sihtkohta ei leitud."
    }

internal fun routeStateMessage(state: RouteQueryState): String =
    when (state) {
        RouteQueryState.Idle -> "Vali sihtkoht ja päritolu, seejärel vajuta \"Otsi\"."
        RouteQueryState.Searching -> "Marsruuti otsitakse sõiduplaani järgi."
        RouteQueryState.FeedNotAvailable -> "Bussiandmed pole veel valmis."
        RouteQueryState.DestinationNotReady -> "Vali esmalt sihtkoht."
        RouteQueryState.OriginNotProvided -> "Määra päritolu enne otsingut."
        RouteQueryState.NoPatternsAvailable -> "Sõiduplaani mustrid puuduvad."
        is RouteQueryState.RouteFound -> "marsruut leitud"
        is RouteQueryState.RouteNotFound -> "Marsruuti ei leitud antud valikuga."
        is RouteQueryState.Error -> "Viga marsruudi otsingul: ${state.message}"
    }

internal fun isSearchButtonEnabled(uiState: SearchUiState): Boolean =
    uiState.destinationInput is DestinationInputState.Resolved && uiState.originStopPointId != null

internal fun shouldShowFeedStatusBanner(feedState: FeedState): Boolean =
    feedState != FeedState.Ready

internal fun routeFoundSummaryLines(route: RouteFoundSummary): List<String> =
    listOf(
        "✓ Marsruut leitud",
        "Vahepeatusi: ${route.segmentStopCount}",
        "(Täielikud peatusenimed ja sõiduajad on tulemas)",
    )

internal fun quickDestinationOptions(): List<QuickDestinationOption> =
    listOf(
        QuickDestinationOption(label = "Rakvere bussijaam", queryText = "Rakvere bussijaam"),
        QuickDestinationOption(label = "Polikliinik", queryText = "Polikliinik"),
        QuickDestinationOption(label = "Näpi", queryText = "Näpi"),
        QuickDestinationOption(label = "Keskväljak", queryText = "Keskväljak"),
        // Põhjakeskus chip resolves via current runtime stop displayName "Põhja".
        QuickDestinationOption(label = "Põhjakeskus", queryText = "Põhja"),
    )

internal fun handleQuickDestinationSelection(
    label: String,
    queryText: String,
    setDestinationText: (String) -> Unit,
    onDestinationSelect: (String) -> Unit,
) {
    setDestinationText(label)
    onDestinationSelect(queryText)
}

internal fun orderedOriginCandidateGroups(originCandidates: List<OriginCandidateGroup>): List<OriginCandidateGroup> {
    val preferredRankByDisplayName = preferredOriginDisplayOrder().withIndex().associate { (index, name) -> name to index }
    return originCandidates.sortedWith(
        compareBy<OriginCandidateGroup>(
            { group -> preferredRankByDisplayName[group.displayName] ?: Int.MAX_VALUE },
            { group -> group.displayName.lowercase() },
        ),
    )
}

internal fun resolveOriginGroupTap(
    group: OriginCandidateGroup,
    currentlyExpandedGroupId: String?,
): OriginGroupTapOutcome {
    if (group.options.size == 1) {
        return OriginGroupTapOutcome(
            selectedStopPointId = group.options.single().stopPointId,
            expandedGroupId = null,
        )
    }

    val nextExpandedGroupId = if (currentlyExpandedGroupId == group.groupId) null else group.groupId
    return OriginGroupTapOutcome(
        selectedStopPointId = null,
        expandedGroupId = nextExpandedGroupId,
    )
}

internal fun originOptionLabel(option: OriginCandidateOption): String {
    return if (option.routePatternCount > 0) {
        "${option.label} (${option.routePatternCount})"
    } else {
        option.label
    }
}

internal fun preferredOriginDisplayOrder(): List<String> =
    listOf(
        "Rakvere bussijaam",
        "Polikliinik",
        "Näpi",
        "Põhja",
        "Arkna tee",
    )
