# PASS_12_ORIGIN_STOPPOINT_CANDIDATE_RESOLVER_SPEC

## Objective

Add a pure Kotlin origin candidate model and resolver in `feature-search` that supports conservative manual-text and current-location origin seeds without nearest-stop lookup, routing integration, UI, or network/runtime expansion.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `280f8a083b91bc86e33478c68ec9b55d89142607`
- working tree at pass start: clean

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROADMAP.md`
- `docs/CITY_ADAPTERS.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_10_DESTINATION_TARGET_MODEL_AND_PLACE_RESOLVER_SPEC.md`
- `docs/audit/PASS_11_PLACE_TO_STOP_CANDIDATE_MAPPING_SPEC.md`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/build.gradle.kts`

## Build/Dependency Changes

No build or dependency changes were required for PASS 12.

## Origin Candidate Model

Added in `ee.androbus.feature.search.origin`:

- `OriginCandidate`
- `OriginCandidateSource`
- `OriginCandidateConfidence`
- `OriginCoordinateConfidence`
- `OriginCandidateResult`
- `OriginCandidateNotFoundReason`
- `OriginCandidateResolver`

Policy highlights:

- manual text and current-location candidates are unresolved seeds,
- `StopPointId` values are not fabricated,
- stop-group names are not fabricated from free text or coordinates,
- current-location input uses `GeoPoint` only.

## Resolver Behavior

`OriginCandidateResolver` provides:

- `fromManualText(query: String)`
- `fromCurrentLocation(location: GeoPoint?)`

Behavior:

- blank/whitespace manual input -> `NotFound(BLANK_QUERY)`
- nonblank manual input -> one candidate:
  - source: `MANUAL_TEXT`
  - confidence: `MANUAL_TEXT_UNRESOLVED`
  - displayName: trimmed + whitespace-collapsed input
  - `stopPointIds`: empty
  - `stopGroupNames`: empty
- null location -> `NotFound(MISSING_LOCATION)`
- non-null location -> one candidate:
  - source: `CURRENT_LOCATION`
  - confidence: `COORDINATE_ONLY_UNRESOLVED`
  - coordinate preserved from input
  - coordinateConfidence: `PROVIDED_BY_USER_LOCATION`
  - `stopPointIds`: empty

## Confidence Policy

- `MANUAL_TEXT_UNRESOLVED`: free-text origin seed with no stop mapping.
- `COORDINATE_ONLY_UNRESOLVED`: coordinate seed with no nearest-stop resolution.
- `EXPLICIT_METADATA` and `UNCLEAR`: reserved for future origin pipelines; not used by current resolver.

## Why No Nearest-Stop Logic Yet

PASS 12 intentionally locks conservative origin seed contracts first. Nearest-stop geospatial lookup, stop-group/stop-point linking, and route-bridge integration remain future scoped work.

## Tests Added

Added:

- `feature-search/src/test/kotlin/ee/androbus/feature/search/origin/OriginCandidateResolverTest.kt`

Coverage includes:

- blank/whitespace manual query handling
- manual candidate shape and unresolved confidence
- no fabricated stop IDs/group names
- null/non-null current location behavior
- coordinate preservation for current location
- Android-free reflection guard and non-geospatial-future-source checks

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected PASS 12 scoped changes only

## Files Changed

- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/OriginCandidate.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/OriginCandidateResult.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/OriginCandidateResolver.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/origin/OriginCandidateResolverTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/TRUTH_INDEX.md`
- `docs/audit/PASS_12_ORIGIN_STOPPOINT_CANDIDATE_RESOLVER_SPEC.md`

## No Out-of-Scope Changes Confirmation

- No UI/Compose/ViewModel changes.
- No Room changes.
- No network/downloader/realtime changes.
- No routing engine behavior changes.
- No GTFS parser behavior changes.
- No nearest-stop geospatial implementation.

## Risks / Unknowns

- Origin candidates remain unresolved and cannot yet be mapped to concrete `StopPointId` values.
- Current-location candidate currently carries only coordinate seed and no ranking.
- Future pass must define deterministic origin-to-stop mapping policy.

## Recommended PASS 13

`PASS 13 — DESTINATION_TO_DIRECT_ROUTE_QUERY_BRIDGE_SPEC`

