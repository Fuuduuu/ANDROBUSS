# PASS_33_ORIGIN_SEARCH_DIALOG

## Objective

Add a searchable origin dialog to the Compose search screen using existing runtime-backed `originCandidates`, while preserving explicit concrete `StopPointId` selection and keeping GPS/network/navigation/realtime scope closed.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `cf7eb798f4655748c83cbf5b7437c03bb8d97a26`
- Working tree at pass start: clean
- Gate validator: `py -3 tools/validate_project_state.py` passed (warning-only stale commit metadata before docs sync)
- Expected HEAD note: pass prompt used placeholder (`<PUT_CURRENT_CLEAN_HEAD_AFTER_PASS_32_COMMIT_HERE>`), execution proceeded on clean current `main` HEAD.

## Files Read

- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_32_ORIGIN_PICKER_FROM_RUNTIME_STOPS_GROUP_AWARE.md`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchUiState.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchViewModel.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchViewModelTest.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
- `app/src/main/assets/bootstrap/rakvere_feed_20260428.json`

## Origin Candidate Model Verification

PASS 33 prerequisite was satisfied without model changes:

- `SearchUiState.originCandidates: List<OriginCandidateGroup>` exists.
- `OriginCandidateGroup.options: List<OriginCandidateOption>` exists.
- `OriginCandidateOption.stopPointId` exists and remains concrete selection identity.

No `SearchUiState` or `SearchViewModel` model changes were required.

## Dialog UI Added

`SearchScreen.kt` now includes an origin-search dialog flow:

- Added an `Otsi peatus...` action in origin section.
- Added `OriginSearchDialog(...)` with:
  - title `Vali lähtepeatus`,
  - local `searchText` filtering over existing `originCandidates`,
  - `Tulemusi ei leitud` empty-filter result,
  - `Tühista` dismiss action.
- Dialog state is local Compose state (`rememberSaveable`), not ViewModel state.

## Multi-Option Group Behavior

- Single-option group:
  - selecting group chooses that concrete `OriginCandidateOption.stopPointId`.
- Multi-option group:
  - first group tap expands/collapses,
  - no silent first-stop auto-selection,
  - user must tap concrete option row to select.
- Option-row selection:
  - calls `onOriginSelected(option.stopPointId)`,
  - dismisses dialog.

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
- `rg -n "StopPointId\\(\"" app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `rg -n "LocationManager|FusedLocation|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION|WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main`
- `rg -n "NavHost|NavController|rememberNavController" app/src/main`

Result summary:

- All required validation commands passed.
- `rg -n "RKV_A_OUT|RKV_A_IN|RKV_B|RKV_C" app/src/main` matches only legacy synthetic fallback asset `app/src/main/assets/bootstrap/rakvere_bootstrap.json`.
- `rg -n "RKV_A_OUT|RKV_A_IN|RKV_B|RKV_C" app/src/main/kotlin` returned no matches.
- No `StopPointId("...")` constructor usage in `SearchScreen.kt`.
- No GPS/network/realtime/navigation scope expansion.

## Scope and Forbidden-Surface Confirmation

- No changes in:
  - `feature-search/src/main/**`
  - `core-domain/src/main/**`
  - `core-gtfs/src/main/**`
  - `core-routing/src/main/**`
  - `data-local/src/main/**`
  - `city-adapters/src/main/**`
  - build files / lockfiles / runtime assets
- No Room schema changes.
- No GPS/nearest/map/navigation/network/realtime additions.

## Files Changed

- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_33_ORIGIN_SEARCH_DIALOG.md`

## Next Recommended Pass

- `PASS_34_ORIGIN_SELECTION_POLISH_SCOPE_AUDIT`
- Alternative: `PASS_UI_02_SEARCH_SCREEN_POLISH_SCOPE_AUDIT`
- Alternative: `PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT`
