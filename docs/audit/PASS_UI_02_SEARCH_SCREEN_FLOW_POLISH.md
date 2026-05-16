# PASS_UI_02_SEARCH_SCREEN_FLOW_POLISH

## Objective

Polish the existing Compose `SearchScreen` flow/copy/state behavior without changing ViewModel/domain/data/runtime logic.

## Stale Destination Gating Fix

- Added UI-local destination gating state in `SearchScreen`:
  - submitted destination text tracking,
  - resolved destination text tracking.
- `Otsi` is now enabled only when:
  - destination is `Resolved`,
  - origin is selected,
  - current destination text still matches the last resolved/submitted text.
- Editing destination text after a successful resolve now disables `Otsi` until user re-runs `Vali sihtkoht`.
- No `SearchViewModel` or `SearchUiState` changes were made.

## Flow And Clutter Polish

- Reordered sections to keep manual destination input visually primary:
  1. destination section,
  2. quick destinations,
  3. origin section,
  4. `Otsi`,
  5. result.
- Quick-destination helper text now explicitly states route search is separate.
- Clarified alias behavior for `Põhjakeskus` by surfacing `Põhjakeskus (Põhja)` plus helper copy.
- Inline origin chips now show preferred groups only.
- Full runtime origin catalog remains available via the existing origin search dialog.

## Copy Polish

- Replaced technical rider text:
  - `Sõiduplaani mustrid puuduvad.` -> `Marsruudiandmed pole saadaval.`
  - generic route miss -> `Otsemarsruuti ei leitud valitud peatuste vahel.`
- Improved multi-option origin labels:
  - group label format: `Nimi — X valikut`,
  - option label format: `Valik N — X marsruuti`.
- No raw IDs are rendered in the route-found summary path.

## Tests Updated

- Updated `SearchScreenStateTextTest` coverage for:
  - stale destination edit disables search until re-selection,
  - destination-before-quick section ordering helper,
  - quick helper/alias copy behavior,
  - quick path does not trigger direct route search callback path,
  - inline-preferred vs full-dialog origin coverage split,
  - rider-friendly origin and route text wording,
  - no live/realtime wording in SearchScreen copy helpers.

## Validation Result

Commands run:

- `py -3 tools/validate_project_state.py` -> passed
- `.\gradlew.bat :app:test` -> passed
- `.\gradlew.bat detekt` -> passed
- `.\gradlew.bat build` -> passed
- `git diff --check` -> passed
- `git status --short --untracked-files=all` -> expected allowed-file-only changes
- `git diff --name-only` -> expected allowed-file-only changes

Boundary checks:

- `rg -n "StopPointId\\(\"" app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt` -> no matches
- `rg -n "LocationManager|FusedLocation|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION|WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main` -> no matches
- `rg -n "live|realtime|pärisajas|reaalajas" app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt` -> no matches
- `rg -n "NavHost|NavController|rememberNavController" app/src/main` -> no matches

## Forbidden Scope Confirmation

- No changes in:
  - `SearchViewModel.kt`
  - `SearchUiState.kt`
  - `feature-search/**`, `core-*/**`, `data-local/**`, `city-adapters/**`, `data-remote/**`
  - runtime assets
  - build files / lockfiles
- No GPS/network/realtime/navigation/WorkManager scope opened.

## Files Changed

- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_UI_02_SEARCH_SCREEN_FLOW_POLISH.md`

## Next Recommended Pass

- `PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT`
- Alternative: `PASS_34_ORIGIN_SELECTION_POLISH_SCOPE_AUDIT`
