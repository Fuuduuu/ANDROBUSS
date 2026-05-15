# PASS_AUTO_05_EXTEND_DETEKT_BOUNDARY_COVERAGE

## Objective

Extend boundary-only Detekt coverage from core modules to `app`, `data-local`, `feature-search`, and `city-adapters` without changing Kotlin source or runtime behavior.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `108f747045256876d2334d99f061726530436bf9`
- Working tree at start: clean
- `py -3 tools/validate_project_state.py`: passed (stale-commit warning allowed)

## Modules Covered

- `app`
- `data-local`
- `feature-search`
- `city-adapters`

## Forbidden Import Policies

- `app`:
  - forbids parser/runtime generation imports (`GtfsFeedParser`, `GtfsDomainMapper`, `MappedGtfsFeed`)
  - forbids downloader/realtime library imports (`androidx.work.*`, `retrofit2.*`, `okhttp3.*`, `com.google.transit.realtime.*`)
- `data-local`:
  - forbids app/feature-search imports
  - forbids parser/runtime generation imports
  - forbids downloader/realtime library imports
- `feature-search`:
  - forbids Android/AndroidX/Hilt/DI imports
  - forbids app/data-local imports
  - forbids parser/runtime generation imports
  - forbids downloader/realtime library imports
- `city-adapters`:
  - forbids Android/AndroidX/Hilt/DI imports
  - forbids app/data-local/feature-search imports
  - forbids downloader/realtime library imports

## Build Wiring

- Added Detekt plugin/config wiring to:
  - `app/build.gradle.kts`
  - `data-local/build.gradle.kts`
  - `feature-search/build.gradle.kts`
  - `city-adapters/build.gradle.kts`
- Kept Detekt scope boundary-only:
  - `buildUponDefaultConfig = false`
  - module config + shared base config only
  - detekt task source limited to `src/main` for boundary checks

## Validation Result

Commands run in this pass:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:detekt`
- `.\gradlew.bat :data-local:detekt`
- `.\gradlew.bat :feature-search:detekt`
- `.\gradlew.bat :city-adapters:detekt`
- `.\gradlew.bat detekt`
- `.\gradlew.bat test`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main`
- `rg -n "android\.|androidx\.|dagger\.|javax\.inject" feature-search/src/main`
- `rg -n "android\.|androidx\.|dagger\.|javax\.inject" city-adapters/src/main`
- `rg -n "ee\.androbus\.app\.|ee\.androbus\.feature\.search\." data-local/src/main`
- `rg -n "WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main data-local/src/main feature-search/src/main city-adapters/src/main`

## Source-Code Untouched Confirmation

- No `*.kt` source file edits were made in this pass.
- Changes are limited to Detekt config/build wiring and docs/audit sync files.

## Files Changed

- `config/detekt/detekt-app.yml`
- `config/detekt/detekt-data-local.yml`
- `config/detekt/detekt-feature-search.yml`
- `config/detekt/detekt-city-adapters.yml`
- `app/build.gradle.kts`
- `data-local/build.gradle.kts`
- `feature-search/build.gradle.kts`
- `city-adapters/build.gradle.kts`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_AUTO_05_EXTEND_DETEKT_BOUNDARY_COVERAGE.md`

## Remaining Risks

- AUTO-05 guardrails are static import boundaries only; they do not replace runtime lifecycle integration tests.
- Additional module-specific boundary checks may still be added in future tooling passes.

## Next Recommended Pass

- `PASS_28C_COMPOSE_SEARCH_SCREEN_SCOPE_AUDIT`
