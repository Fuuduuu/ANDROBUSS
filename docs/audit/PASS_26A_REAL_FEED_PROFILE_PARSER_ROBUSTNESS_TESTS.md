# PASS_26A_REAL_FEED_PROFILE_PARSER_ROBUSTNESS_TESTS

## Objective

Add executable parser robustness tests that mimic real Rakvere feed characteristics before any real bundled asset generation.

## Repo Guard

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `b22e4054eac89c1e828f97e05145a92f26f7f4ab`
- Working tree before edits: clean
- `py -3 tools/validate_project_state.py`: passed (warning-only stale-commit warning before pass updates)

## Fixture Added

- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/` with valid CSV-only files:
  - `agency.txt`
  - `stops.txt`
  - `routes.txt`
  - `trips.txt`
  - `stop_times.txt`
  - `calendar.txt`
  - `calendar_dates.txt`
- Optional files intentionally absent:
  - `frequencies.txt`
  - `transfers.txt`
  - `attributions.txt`

## Tests Added

- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/RakvereProfileSmokeTest.kt`
- Coverage includes:
  - quoted `service_id` with commas preserved,
  - unknown/extra columns tolerated,
  - optional file absence tolerated,
  - `calendar_dates` add/remove behavior through `ServiceCalendarResolver`,
  - `block_id` presence tolerated without model changes,
  - duplicate stop in loop pattern preserved,
  - `StopPointId` anti-fabrication checks,
  - explicit assertion that parser reads all stops (no `stop_area` filtering at parser layer).

## Parser Production Code Untouched

- No files under `core-gtfs/src/main/**` were changed.
- No parser/domain/routing/app production behavior changes were made.

## Stop Area Filtering Decision

- `stop_area` filtering remains outside parser/domain-mapper responsibilities.
- Filtering belongs to future real-asset generation tooling policy.

## Validation Result

Executed:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :core-gtfs:test`
- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `git diff --name-only | findstr /R "\.kt$"`

## Files Changed

- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/agency.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/stops.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/routes.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/trips.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/stop_times.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/calendar.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/calendar_dates.txt`
- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/RakvereProfileSmokeTest.kt`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_26A_REAL_FEED_PROFILE_PARSER_ROBUSTNESS_TESTS.md`

## Next Pass

- `PASS 26B — REAL_RAKVERE_BUNDLED_FEED_ASSET_GENERATION` only after PASS 26A validation passes and legal/source/freshness policy remains acceptable.
