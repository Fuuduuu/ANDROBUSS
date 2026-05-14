# PASS_28B_ROUTE_QUERY_WITH_EXPLICIT_ORIGIN

## Objective

Sync governance/docs after PASS 28B source implementation so project state reflects explicit route-query origin gating and route-query precondition state handling.

## Scope For This Sync

- Docs/audit sync only.
- No source-code edits in this sync step.
- PASS 28B source implementation is accepted and was validated before this sync.

## Files Updated

- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_28B_ROUTE_QUERY_WITH_EXPLICIT_ORIGIN.md`

## Recorded PASS 28B Decisions

- `RouteQueryState` includes distinct precondition states:
  - `FeedNotAvailable`
  - `DestinationNotReady`
  - `OriginNotProvided`
  - `NoPatternsAvailable`
  - and separate `RouteNotFound`.
- `searchRoute()` is explicit trigger only.
- Route query requires explicit origin.
- Route query execution path uses `DirectRouteQueryPreparationUseCase`.
- Verified destination IDs are used; no display-name-based ID fabrication.

## Out Of Scope Confirmed

- No UI/Compose/navigation implementation.
- No GPS/nearest-stop origin automation.
- No network/downloader/realtime/WorkManager.
- No Room schema/entity/DAO changes.
- No parser/routing core algorithm changes.

## Validation Results

Implementation-phase validation (already completed before this docs sync) was green:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :data-local:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `.\gradlew.bat test`

Docs-sync validation run:

- `py -3 tools/validate_project_state.py` -> passed (stale commit warning allowed)
- `git diff --check` -> passed (only LF/CRLF warnings)
- `git status --short --untracked-files=all` -> expected pending PASS 28B source+docs changes

## Next Recommended Pass

- `PASS_AUTO_04_BOOTSTRAP_ROOM_FIRST_CHECK` (recommended)
- Alternative: `PASS_28C_COMPOSE_SEARCH_SCREEN_SCOPE_AUDIT`
