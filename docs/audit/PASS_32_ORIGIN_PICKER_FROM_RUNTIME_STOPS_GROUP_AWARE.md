# PASS_32_ORIGIN_PICKER_FROM_RUNTIME_STOPS_GROUP_AWARE

## Objective

Replace synthetic hardcoded origin chips in app search UI with runtime snapshot-backed origin candidates grouped by `stopGroupId`, while preserving explicit concrete `StopPointId` selection and keeping GPS/network/navigation/realtime out of scope.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `d067c9d545d21520ce06fb6eaa6b7a3df123c62c`
- Working tree at pass start: clean
- Gate validator: `py -3 tools/validate_project_state.py` passed (warning-only stale commit metadata before pass sync)
- Expected HEAD note: pass prompt used placeholder (`<PUT_CURRENT_CLEAN_HEAD_AFTER_AUTO_07_COMMIT_HERE>`), so execution proceeded on the clean current `main` HEAD.

## Scope Applied

Touched only PASS-allowed files:
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchUiState.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchViewModel.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchViewModelTest.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_32_ORIGIN_PICKER_FROM_RUNTIME_STOPS_GROUP_AWARE.md`

## What Changed

### 1. Origin candidate model in app state

- Added `OriginCandidateGroup` and `OriginCandidateOption` models in `SearchUiState.kt`.
- Extended `SearchUiState` with:
  - `originCandidates: List<OriginCandidateGroup> = emptyList()`.

### 2. Runtime-backed origin candidate building in ViewModel

- `SearchViewModel.refreshFeedState()` now:
  - reads snapshot from provider,
  - sets `feedState`,
  - builds `originCandidates` from active snapshot when available,
  - clears invalid selected origin if it no longer exists in candidate set.
- Added candidate builder logic:
  - includes only stop points that are present in at least one route pattern,
  - groups by `stopGroupId`,
  - computes `routePatternCount` per stop option,
  - emits deterministic labels (`"{displayName} variant {n}"` for multi-option groups),
  - sorts groups deterministically.
- No ID fabrication logic was added; all origin options use existing `StopPoint.id` values only.

### 3. SearchScreen origin section now group-aware and runtime-backed

- Removed hardcoded synthetic origin IDs from UI (`RKV_A_OUT`, `RKV_A_IN`, `RKV_B`, `RKV_C`).
- `OriginSection` now consumes `uiState.originCandidates`.
- Group behavior:
  - single-option group: tap selects concrete `StopPointId` immediately,
  - multi-option group: tap expands options, user must select a concrete option.
- Added explicit follow-up note:
  - `TODO PASS 33: full origin search dialog for all stop groups.`
- Quick-destination behavior is unchanged.

## Test Coverage Added/Updated

### `SearchViewModelTest`

Added coverage for PASS 32 requirements:
- origin candidates empty when feed not ready,
- origin candidates non-empty with runtime-like snapshot,
- candidate stop IDs all exist in snapshot and are referenced by route patterns,
- old synthetic IDs absent from origin candidates,
- `Rakvere bussijaam` and `Polikliinik` groups present,
- selecting a runtime origin option stores `originStopPointId`,
- route query with runtime origin does not return `ORIGIN_NOT_FOUND`.

### `SearchScreenStateTextTest`

Added coverage for origin-section behavior:
- runtime-backed preferred labels present,
- synthetic legacy labels absent from preferred ordering,
- multi-option group tap requires explicit option selection (no implicit first-stop selection),
- single-option group tap resolves to a concrete `StopPointId` selection event,
- option label rendering includes route-pattern count where available.

## Validation Result

Commands run:
- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `rg -n "RKV_A_OUT|RKV_A_IN|RKV_B|RKV_C" app/src/main`
- `rg -n "RKV_A_OUT|RKV_A_IN|RKV_B|RKV_C" app/src/main/kotlin`
- `rg -n "StopPointId\\(\"RKV" app/src/main`
- `rg -n "StopPointId\\(\"RKV" app/src/main/kotlin`
- `rg -n "LocationManager|FusedLocation|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION|WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main`
- `rg -n "NavHost|NavController|rememberNavController" app/src/main`

Result summary:
- `py -3 tools/validate_project_state.py` passed.
- `.\gradlew.bat :app:test` passed.
- `.\gradlew.bat detekt` passed.
- `.\gradlew.bat build` passed.
- `git diff --check` passed.
- `rg -n "RKV_A_OUT|RKV_A_IN|RKV_B|RKV_C" app/src/main` matches only legacy fallback asset `app/src/main/assets/bootstrap/rakvere_bootstrap.json` (expected historical synthetic fallback data).
- `rg -n "RKV_A_OUT|RKV_A_IN|RKV_B|RKV_C" app/src/main/kotlin` returned no matches.
- `rg -n "StopPointId\\(\"RKV" app/src/main` and `app/src/main/kotlin` returned no matches.
- No scope expansion to GPS/network/realtime/navigation.

## Forbidden-Scope Confirmation

- No changes in `feature-search/src/main/**`.
- No changes in `core-domain/src/main/**`, `core-gtfs/src/main/**`, `core-routing/src/main/**`, `data-local/src/main/**`, `city-adapters/src/main/**`.
- No Room schema/entity/DAO changes.
- No runtime asset file changes.
- No build or lockfile changes.

## Remaining Risks

- This pass improves origin selection source quality but does not implement nearest-stop/GPS or full origin search UX.
- Ambiguity in multi-stop groups is now explicit but still chip-driven; broader origin discovery UX remains future work.

## Next Recommended Pass

- `PASS_33_ORIGIN_SEARCH_DIALOG_SCOPE_AUDIT`
- Alternative: `PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT`
