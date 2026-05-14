# PASS_28A_APP_SEARCH_VIEWMODEL_FEED_AND_DESTINATION_STATE

## Objective

Implement the first app-layer search state baseline by adding `SearchViewModel` + `SearchUiState`, while keeping feature-search Android-free and keeping UI/navigation/route-query wiring out of scope.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `ff2a88efff0ca4dc258e9fba39fd7dc6dde071b4`
- Working tree before edits: clean
- `py -3 tools/validate_project_state.py`: passed

## Files Read

- Governance/docs context:
  - `docs/PROJECT_STATE.yml`
  - `docs/CURRENT_STATE.md`
  - `docs/ROADMAP.md`
  - `docs/TRUTH_INDEX.md`
  - `docs/INVARIANTS.md`
  - `docs/PROTECTED_SURFACES.md`
  - `docs/ANDROID_ARCHITECTURE.md`
  - `docs/ROUTING_LOGIC.md`
  - `docs/TESTING_STRATEGY.md`
  - `docs/CODEBASE_IMPACT_MAP.md`
  - `docs/AUDIT_INDEX.md`
  - `docs/audit/PASS_27_HILT_DI_BASELINE.md`
  - `docs/audit/PASS_AUTO_03_DRIFT_AND_BOUNDARY_CHECK.md`
- App/data-local/core-feature files used for implementation alignment:
  - `app/build.gradle.kts`
  - `app/src/main/kotlin/ee/androbus/app/di/FeedModule.kt`
  - `app/src/main/kotlin/ee/androbus/app/AndrobussApplication.kt`
  - `core-domain/src/main/kotlin/ee/androbus/core/domain/DomainFeedSnapshotProvider.kt`
  - `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotProvider.kt`
  - `feature-search/src/main/kotlin/ee/androbus/feature/search/**` (destination/resolution/orchestration APIs)

## Implementation Summary

- Added app presentation state models:
  - `SearchUiState`
  - `FeedState`
  - `DestinationInputState`
  - `ResolvedDestinationOption`
- Added app `SearchViewModel` (`@HiltViewModel`) with:
  - cache-only feed readiness refresh (`DomainFeedSnapshotProvider.getSnapshot`)
  - destination typed input handling
  - destination enrichment pipeline using existing feature-search pure Kotlin components:
    - `PlaceToStopCandidateResolver`
    - `InMemoryStopPointIndex`
    - `StopCandidateEnricher`
    - `DestinationEnrichmentOrchestrator`
  - explicit ambiguous option selection API (`onAmbiguousOptionSelected`)
- Added Hilt interface binding in app module:
  - `RoomDomainFeedSnapshotProvider -> DomainFeedSnapshotProvider`

## Behavior Notes

- No `prepare()` call from ViewModel.
- No importer/bootstrap trigger from ViewModel.
- No route-query call (`DirectRouteQueryPreparationUseCase` / `DirectRouteQueryBridge`).
- No UI/navigation code.
- No origin/GPS/nearest-stop behavior.
- No ID fabrication:
  - resolved IDs come from verified resolver candidates (`VerifiedStopPointCandidate.stopPointId`).

## Tests Added

- `SearchViewModelTest` covers:
  - initial feed/destination state
  - `refreshFeedState` transitions
  - empty destination input
  - resolved destination (`Jaam -> RKV_C`)
  - ambiguous destination (`Keskpeatus`)
  - ambiguous-option selection
  - feed-not-ready safety
  - anti-fabrication assertions
  - guard check that ViewModel source does not reference route-query bridge/use-case

## Validation Result

Commands run and passed:

- `py -3 tools/validate_project_state.py`
- `./gradlew.bat :app:test`
- `./gradlew.bat :feature-search:test :core-domain:test :data-local:test`
- `./gradlew.bat detekt`
- `./gradlew.bat build`
- `./gradlew.bat test`
- `git diff --check`

Boundary checks run (expected zero matches, got zero matches):

- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main`
- `rg -n "@Serializable" core-domain/src`
- `rg -n "allowMainThreadQueries" data-local/src`
- `rg -n "@HiltAndroidApp|@Module|@InstallIn|@Inject" core-domain core-gtfs core-routing feature-search`
- `rg -n "DirectRouteQueryPreparationUseCase|DirectRouteQueryBridge" app/src/main/kotlin/ee/androbus/app/presentation/search`

## Files Changed

- `app/src/main/kotlin/ee/androbus/app/di/FeedModule.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchUiState.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchViewModel.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchViewModelTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_28A_APP_SEARCH_VIEWMODEL_FEED_AND_DESTINATION_STATE.md`

## Scope Confirmation

- No feature-search production code changes.
- No core-domain/core-gtfs/core-routing production code changes.
- No Room schema/entity/DAO changes.
- No parser/routing/search algorithm changes.
- No Compose/UI/navigation changes.
- No WorkManager/network/realtime changes.

## Risks / Unknowns

- PASS 28A uses a conservative typed-input-to-candidate bridge in app layer; richer metadata/POI-driven input strategy may need refinement in later passes.
- Route query + explicit origin state is intentionally deferred.

## Recommended Next Pass

- `PASS_28B_ROUTE_QUERY_AND_EXPLICIT_ORIGIN_BASELINE`
