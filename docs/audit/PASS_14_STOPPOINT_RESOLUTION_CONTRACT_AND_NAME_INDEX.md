# PASS_14_STOPPOINT_RESOLUTION_CONTRACT_AND_NAME_INDEX

## Objective

Define the first verified `StopPointId` resolution contract and implement a minimal in-memory name index in `feature-search` without Room, geospatial nearest-stop logic, or routing/parser changes.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `cd8d5aa6942ad83c999307e04094f5913a7c0c8d`
- working tree at pass start: clean

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/ROADMAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_11_PLACE_TO_STOP_CANDIDATE_MAPPING_SPEC.md`
- `docs/audit/PASS_12_ORIGIN_STOPPOINT_CANDIDATE_RESOLVER_SPEC.md`
- `docs/audit/PASS_13_DIRECT_ROUTE_QUERY_BRIDGE_AND_PRECONDITION_SPEC.md`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/**`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/stops.txt`

## Build/Dependency Changes

- No Gradle dependency changes were required for PASS 14.

## Stop-Point Resolution Contract

Added under `ee.androbus.feature.search.resolution`:

- `StopPointResolutionInput`
- `VerifiedStopPointCandidate`
- `StopPointResolutionConfidence`
- `StopPointResolutionSource`
- `StopPointResolutionResult`
- `StopPointResolver`
- `InMemoryStopPointIndex`

Result contract:

- `Resolved(candidates)`
- `NotResolved.EmptyStopGroupName`
- `NotResolved.NoStopGroupMatch`
- `NotResolved.NoIndexAvailable`

## In-Memory Name Index Behavior

`InMemoryStopPointIndex`:

- receives `List<StopPoint>` in constructor,
- indexes by normalized stop display name + city ID,
- normalization: trim + lowercase (`et-EE`) + whitespace collapse,
- blank input name -> `EmptyStopGroupName`,
- empty index -> `NoIndexAvailable`,
- no key match -> `NoStopGroupMatch`,
- key match -> `Resolved(candidates)` for all matching stop points.

## Verified StopPointId Source Policy

`VerifiedStopPointCandidate.stopPointId` is always taken from `StopPoint.id` of matched domain objects.

No constructor or resolver path derives `StopPointId` from text labels or coordinates.

## Anti-Fabrication Rules

- No `StopPointId` derivation from:
  - `stopGroupName`,
  - `displayName`,
  - manual text,
  - coordinates.
- No `StopGroupId` fabrication.
- Emitted source in PASS 14 is only `GTFS_STOP_ID`.
- `FUTURE_GEOSPATIAL` and `FUTURE_COORDINATE_NEAREST` remain placeholders only.

## CoordinateHint Ignored Policy

- `StopPointResolutionInput.coordinateHint` is accepted for future compatibility.
- PASS 14 `InMemoryStopPointIndex` intentionally ignores it.
- Nearest-stop behavior is out of scope.

## Same-Name StopPoint Behavior

- Multiple stop points with same normalized display name are all returned.
- Candidate ordering preserves source `stopPoints` list order.
- This protects directional/platform-specific stop-point identity.

## Tests Added

Added:

- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/InMemoryStopPointIndexTest.kt`

Coverage includes:

- exact/normalized matches,
- same-name multi-stop returns,
- empty/unknown/blank error branches,
- ID/group/location field integrity,
- future source/confidence not emitted,
- coordinate hint ignored behavior,
- city boundary behavior,
- Android-free reflection guard.

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected PASS 14 scoped changes only

## Files Changed

- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/StopPointResolutionInput.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/VerifiedStopPointCandidate.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/StopPointResolutionResult.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/StopPointResolver.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/InMemoryStopPointIndex.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/InMemoryStopPointIndexTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/audit/PASS_14_STOPPOINT_RESOLUTION_CONTRACT_AND_NAME_INDEX.md`

## No Out-of-Scope Changes Confirmation

- No UI/Compose/ViewModel changes.
- No Room/DAO/AppDatabase changes.
- No network/downloader/realtime changes.
- No GTFS parser changes.
- No `DirectRouteSearch` changes.
- No nearest-stop geospatial implementation.
- No city-adapter changes.

## Risks / Unknowns

- Resolution remains strict name-index matching; no fuzzy/geospatial disambiguation.
- Candidate ranking across multiple same-name stop points is intentionally absent.
- Bridge wiring still needs integration with verified candidate outputs.

## Recommended PASS 15

`PASS 15 — STOPPOINT_RESOLUTION_INTEGRATION_AND_BRIDGE_WIRING`

