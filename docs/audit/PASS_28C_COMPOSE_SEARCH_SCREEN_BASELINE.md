# PASS_28C_COMPOSE_SEARCH_SCREEN_BASELINE

## Objective

Create the first app Compose search screen baseline on top of existing `SearchViewModel` and `SearchUiState`, while keeping scope limited to a single screen and avoiding navigation/GPS/network/realtime additions.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `ff007174c6a20addb3b0bcf2bbff15179d084f51`
- Working tree at start: clean
- `py -3 tools/validate_project_state.py`: passed (stale-commit warning allowed)

## Implemented UI Baseline

- `MainActivity` now:
  - is annotated with `@AndroidEntryPoint`
  - sets Compose content
  - hosts `SearchScreen()` directly (no navigation graph)
- `SearchScreen` created and wired to existing `SearchViewModel` via `hiltViewModel()`.
- `SearchContent` and sections created:
  - `FeedStatusBanner`
  - `DestinationSection`
  - `OriginSection`
  - `RouteResultSection`

## Required Corrections Applied

1. Hilt Activity entry point:
   - `MainActivity` annotated with `@AndroidEntryPoint`.
2. `hiltViewModel` dependency:
   - added `androidx.hilt:hilt-navigation-compose` via version catalog and app dependency.
3. Destination resolution trigger:
   - TextField updates local `destinationText` only.
   - resolution runs only from explicit "Vali sihtkoht" button.
4. Local UI state:
   - `destinationText` kept in `SearchContent` via `rememberSaveable`.
5. Route query trigger:
   - explicit separate "Otsi" button calls `searchRoute()`.
6. Origin selector:
   - hardcoded dev/MVP stop chips only.
   - includes `TODO PASS 29+` comment for proper origin resolver/nearest-stop flow.
7. Wording:
   - static-schedule-safe wording only.
   - no realtime/live phrasing.
8. RouteFound rendering:
   - uses actual existing `RouteFoundSummary` fields from `SearchUiState`.
   - no extra provider/snapshot calls from UI for label resolution.

## Scope Confirmation

- No changes to `SearchViewModel` or `SearchUiState`.
- No navigation graph or multi-screen setup.
- No GPS/location permission logic.
- No network/downloader/WorkManager/realtime logic.
- No feature-search/core-domain/core-routing/data-local production logic changes.
- `app/gradle.lockfile` was updated to lock new Compose/Hilt-navigation dependencies.

## Tests Added

- `SearchScreenStateTextTest` (unit tests):
  - static schedule-safe feed wording
  - route-found headline text
  - destination prompt includes explicit "Vali sihtkoht" action

## Validation Result

Commands run in this pass:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :data-local:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `.\gradlew.bat test`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main`
- `rg -n "@Serializable" core-domain/src`
- `rg -n "allowMainThreadQueries" data-local/src`
- `rg -n "WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main data-local/src/main feature-search/src/main data-remote/src/main`
- `rg -n "hiltViewModel\\(" app/src/main`

Result:
- All listed validation commands passed.
- `git diff --check` has no whitespace errors (only LF/CRLF warnings).

## Files Changed

- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/java/ee/androbus/app/MainActivity.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_28C_COMPOSE_SEARCH_SCREEN_BASELINE.md`

## Next Recommended Pass

- `PASS_29_ORIGIN_RESOLUTION_OR_SEARCH_UI_REFINEMENT`
