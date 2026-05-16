package ee.androbus.app.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    var lastSubmittedDestinationText by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var lastResolvedDestinationText by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var showOriginDialog by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val normalizedDestinationText = destinationText.trim()

    LaunchedEffect(uiState.destinationInput) {
        if (uiState.destinationInput is DestinationInputState.Resolved) {
            lastResolvedDestinationText = lastSubmittedDestinationText ?: normalizedDestinationText
        }
    }

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

        searchContentSectionOrder().forEach { section ->
            when (section) {
                SearchContentSection.Destination -> {
                    DestinationSection(
                        destinationText = destinationText,
                        onDestinationTextChanged = { destinationText = it },
                        onDestinationSelect = {
                            lastSubmittedDestinationText = normalizedDestinationText
                            lastResolvedDestinationText = null
                            onDestinationSelect(normalizedDestinationText)
                        },
                        destinationInputState = uiState.destinationInput,
                        onAmbiguousOptionSelected = onAmbiguousOptionSelected,
                    )
                }

                SearchContentSection.QuickDestinations -> {
                    QuickDestinationSection(
                        onQuickDestinationSelected = { label, queryText ->
                            handleQuickDestinationSelection(
                                label = label,
                                queryText = queryText,
                                setDestinationText = {
                                    destinationText = it
                                    lastSubmittedDestinationText = it.trim()
                                    lastResolvedDestinationText = null
                                },
                                onDestinationSelect = onDestinationSelect,
                            )
                        },
                    )
                }

                SearchContentSection.Origin -> {
                    OriginSection(
                        originCandidates = inlinePreferredOriginCandidateGroups(uiState.originCandidates),
                        selectedOrigin = uiState.originStopPointId,
                        onOriginSelected = onOriginSelected,
                        onOpenOriginSearchDialog = { showOriginDialog = true },
                    )
                }

                SearchContentSection.SearchButton -> {
                    Button(
                        onClick = onSearch,
                        enabled =
                            isSearchButtonEnabled(
                                uiState = uiState,
                                destinationText = normalizedDestinationText,
                                lastResolvedDestinationText = lastResolvedDestinationText,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        Text("Otsi")
                    }
                }

                SearchContentSection.Result -> {
                    RouteResultSection(
                        routeQueryState = uiState.routeQueryState,
                    )
                }
            }
        }

        Text(
            text = "Bussiandmed: Ühistranspordiregister / Regionaal- ja Põllumajandusministeerium",
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (showOriginDialog) {
        OriginSearchDialog(
            candidates = uiState.originCandidates,
            selectedOrigin = uiState.originStopPointId,
            onOriginSelected = { selected ->
                onOriginSelected(selected)
                showOriginDialog = false
            },
            onDismiss = { showOriginDialog = false },
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
            Text(
                text = QUICK_DESTINATION_HELPER_TEXT,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = QUICK_DESTINATION_ALIAS_HELPER_TEXT,
                style = MaterialTheme.typography.bodySmall,
            )
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
    onOpenOriginSearchDialog: () -> Unit,
) {
    var expandedGroupId by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val inlineGroups = orderedOriginCandidateGroups(originCandidates)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Päritolu", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = onOpenOriginSearchDialog,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(ORIGIN_SEARCH_ACTION_TEXT)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (inlineGroups.isEmpty()) {
                    Text(
                        text = "Päritolu valikuid ei leitud aktiivsest andmestikust.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                inlineGroups.forEach { group ->
                    val isSelected = group.options.any { option -> option.stopPointId == selectedOrigin }
                    FilterChip(
                        selected = isSelected,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val outcome = resolveOriginGroupTap(group = group, currentlyExpandedGroupId = expandedGroupId)
                            expandedGroupId = outcome.expandedGroupId
                            outcome.selectedStopPointId?.let { onOriginSelected(it) }
                        },
                        label = { Text(originGroupLabel(group)) },
                    )

                    if (group.options.size > 1 && expandedGroupId == group.groupId) {
                        Column(
                            modifier = Modifier.padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            group.options.forEachIndexed { index, option ->
                                FilterChip(
                                    selected = selectedOrigin == option.stopPointId,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onOriginSelected(option.stopPointId) },
                                    label = { Text(originOptionLabel(option, index)) },
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
fun OriginSearchDialog(
    candidates: List<OriginCandidateGroup>,
    selectedOrigin: StopPointId?,
    onOriginSelected: (StopPointId) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchText by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var expandedGroupId by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val filteredGroups = filterOriginCandidateGroups(candidates = candidates, searchText = searchText)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = ORIGIN_DIALOG_TITLE,
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        expandedGroupId = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(ORIGIN_SEARCH_ACTION_TEXT) },
                    singleLine = true,
                )

                if (filteredGroups.isEmpty()) {
                    Text(
                        text = ORIGIN_DIALOG_NO_RESULTS_TEXT,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filteredGroups) { group ->
                            val isSelected = group.options.any { option -> option.stopPointId == selectedOrigin }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val outcome = resolveOriginGroupTap(group = group, currentlyExpandedGroupId = expandedGroupId)
                                    expandedGroupId =
                                        handleOriginDialogGroupSelection(
                                            outcome = outcome,
                                            onOriginSelected = onOriginSelected,
                                            onDismiss = onDismiss,
                                        )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(originGroupLabel(group)) },
                            )

                            if (group.options.size > 1 && expandedGroupId == group.groupId) {
                                Column(
                                    modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    group.options.forEachIndexed { index, option ->
                                        FilterChip(
                                            selected = option.stopPointId == selectedOrigin,
                                            onClick = { handleOriginDialogOptionSelection(option, onOriginSelected, onDismiss) },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text(originDialogOptionLabel(option, index)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = onDismiss) {
                        Text(ORIGIN_DIALOG_CANCEL_TEXT)
                    }
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
internal const val QUICK_DESTINATION_HELPER_TEXT = "Kiirvalik valib sihtkoha. Marsruudi otsimiseks vali ka lähtepeatus ja vajuta „Otsi“."
internal const val QUICK_DESTINATION_ALIAS_HELPER_TEXT = "Põhjakeskuse valik kasutab Põhja peatust."
internal const val ORIGIN_SEARCH_ACTION_TEXT = "Otsi peatus..."
internal const val ORIGIN_DIALOG_TITLE = "Vali lähtepeatus"
internal const val ORIGIN_DIALOG_NO_RESULTS_TEXT = "Tulemusi ei leitud"
internal const val ORIGIN_DIALOG_CANCEL_TEXT = "Tühista"

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
        RouteQueryState.NoPatternsAvailable -> "Marsruudiandmed pole saadaval."
        is RouteQueryState.RouteFound -> "marsruut leitud"
        is RouteQueryState.RouteNotFound -> "Otsemarsruuti ei leitud valitud peatuste vahel."
        is RouteQueryState.Error -> "Viga marsruudi otsingul: ${state.message}"
    }

internal fun isSearchButtonEnabled(
    uiState: SearchUiState,
    destinationText: String,
    lastResolvedDestinationText: String?,
): Boolean {
    val normalizedCurrent = destinationText.trim()
    val normalizedResolved = lastResolvedDestinationText?.trim()
    return uiState.destinationInput is DestinationInputState.Resolved &&
        uiState.originStopPointId != null &&
        normalizedCurrent.isNotBlank() &&
        normalizedResolved != null &&
        normalizedCurrent == normalizedResolved
}

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
        QuickDestinationOption(label = "Põhjakeskus (Põhja)", queryText = "Põhja"),
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

internal fun inlinePreferredOriginCandidateGroups(originCandidates: List<OriginCandidateGroup>): List<OriginCandidateGroup> {
    val preferredDisplayNames = preferredOriginDisplayOrder().toSet()
    return orderedOriginCandidateGroups(originCandidates).filter { group ->
        group.displayName in preferredDisplayNames
    }
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

internal fun originGroupLabel(group: OriginCandidateGroup): String {
    return if (group.options.size > 1) {
        "${group.displayName} — ${group.options.size} valikut"
    } else {
        group.displayName
    }
}

internal fun originOptionLabel(
    option: OriginCandidateOption,
    index: Int,
): String {
    return if (option.routePatternCount > 0) {
        "Valik ${index + 1} — ${option.routePatternCount} marsruuti"
    } else {
        "Valik ${index + 1}"
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

internal fun filterOriginCandidateGroups(
    candidates: List<OriginCandidateGroup>,
    searchText: String,
): List<OriginCandidateGroup> {
    val query = searchText.trim()
    val ordered = orderedOriginCandidateGroups(candidates)
    if (query.isBlank()) return ordered
    return ordered.filter { group ->
        group.displayName.contains(query, ignoreCase = true)
    }
}

internal fun originDialogOptionLabel(
    option: OriginCandidateOption,
    index: Int,
): String {
    return originOptionLabel(option, index)
}

internal enum class SearchContentSection {
    Destination,
    QuickDestinations,
    Origin,
    SearchButton,
    Result,
}

internal fun searchContentSectionOrder(): List<SearchContentSection> =
    listOf(
        SearchContentSection.Destination,
        SearchContentSection.QuickDestinations,
        SearchContentSection.Origin,
        SearchContentSection.SearchButton,
        SearchContentSection.Result,
    )

internal fun handleOriginDialogOptionSelection(
    option: OriginCandidateOption,
    onOriginSelected: (StopPointId) -> Unit,
    onDismiss: () -> Unit,
) {
    onOriginSelected(option.stopPointId)
    onDismiss()
}

internal fun handleOriginDialogGroupSelection(
    outcome: OriginGroupTapOutcome,
    onOriginSelected: (StopPointId) -> Unit,
    onDismiss: () -> Unit,
): String? {
    outcome.selectedStopPointId?.let { selected ->
        onOriginSelected(selected)
        onDismiss()
        return null
    }
    return outcome.expandedGroupId
}
