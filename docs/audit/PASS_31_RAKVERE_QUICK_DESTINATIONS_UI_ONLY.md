# PASS_31_RAKVERE_QUICK_DESTINATIONS_UI_ONLY

## Objective

Add Rakvere quick-destination UI chips to the existing Compose search screen, using only label/queryText mapping through the existing destination resolution path.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `aa2a8e73c30f883a727bd24f42a65839f80b438e`
- Working tree at start: clean
- Gate validator: `py -3 tools/validate_project_state.py` passed

## Quick Destination Labels and Query Text

| Visible label | queryText sent to resolver |
|---|---|
| Rakvere bussijaam | Rakvere bussijaam |
| Polikliinik | Polikliinik |
| Näpi | Näpi |
| Keskväljak | Keskväljak |
| Põhjakeskus | Põhja |

Notes:
- `Põhjakeskus` uses query text `Põhja` because current runtime GTFS stop displayName is `Põhja`.
- `Tõrma` is excluded in this pass because it is not a runtime stop displayName in current active profile and needs a later metadata/alias pass.

## UI Behavior Implemented

- Added `Kiirvalikud` section in `SearchScreen`.
- Quick chip click behavior:
  1. updates local destination text to the visible label,
  2. calls destination selection callback with `queryText`,
  3. does **not** call search route,
  4. does **not** set origin,
  5. does **not** pass `StopPointId`.
- Existing ambiguity handling and explicit route-search flow remain unchanged.

## Scope Compliance

- No changes to `SearchViewModel.kt`.
- No changes to `SearchUiState.kt`.
- No changes to feature/core/data/city-adapters modules.
- No GPS/network/realtime/WorkManager/navigation additions.

## Tests Updated

- `SearchScreenStateTextTest`:
  - quick labels visible and `Tõrma` excluded,
  - `Põhjakeskus` maps to queryText `Põhja`,
  - `Rakvere bussijaam` maps to same query text,
  - quick selection path does not trigger search callback.
- `RakvereQuickDestinationReadinessTest`:
  - runtime real profile contains `Rakvere bussijaam`, `Polikliinik`, `Näpi`, `Keskväljak`, `Põhja`,
  - runtime real profile does not contain `Tõrma`,
  - anti-StopPointId fabrication checks remain.

## Validation Result

Commands run:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`

## Files Changed

- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/RakvereQuickDestinationReadinessTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_31_RAKVERE_QUICK_DESTINATIONS_UI_ONLY.md`

## Next Recommended Pass

- `PASS_AUTO_07_DRIFT_AND_UI_BOUNDARY_CHECK`
- Alternative: `PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT`
