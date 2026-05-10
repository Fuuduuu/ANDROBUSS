# PASS_13_DIRECT_ROUTE_QUERY_BRIDGE_AND_PRECONDITION_SPEC

## Objective

Add a pure Kotlin bridge in `feature-search` that connects origin candidates, destination stop candidates, route patterns, and `DirectRouteSearch` with explicit precondition checks and conservative unresolved handling.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `20c1afba20a02095009254d1d3d1e22cdcb85706`
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
- `docs/ROUTING_LOGIC.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_10_DESTINATION_TARGET_MODEL_AND_PLACE_RESOLVER_SPEC.md`
- `docs/audit/PASS_11_PLACE_TO_STOP_CANDIDATE_MAPPING_SPEC.md`
- `docs/audit/PASS_12_ORIGIN_STOPPOINT_CANDIDATE_RESOLVER_SPEC.md`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `core-routing/src/main/kotlin/ee/androbus/core/routing/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/**`
- `feature-search/build.gradle.kts`

## Build/Dependency Changes

- Added `implementation(project(":core-routing"))` to `feature-search/build.gradle.kts`.
- No other Gradle files were changed.

## Bridge Result Model

Added:

- `DirectRouteQueryBridgeResult.RouteFound(DirectRouteSearchResult.Found)`
- `DirectRouteQueryBridgeResult.RouteNotFound(DirectRouteSearchResult.NotFound)`
- `DirectRouteQueryBridgeResult.NotReady` variants:
  - `OriginUnresolved`
  - `DestinationUnresolved`
  - `BothUnresolved`
  - `NoPatternsAvailable`

## Precondition Policy

Bridge resolves candidate IDs using:

- `originCandidates.flatMap { it.stopPointIds }`
- `destinationCandidates.flatMap { it.stopPointIds }`

Precondition order:

1. both empty -> `BothUnresolved`
2. origin empty -> `OriginUnresolved`
3. destination empty -> `DestinationUnresolved`
4. patterns empty -> `NoPatternsAvailable`

Only after all pass does the bridge call route search.

## DirectRouteSearch Call Policy

- Bridge constructor supports `DirectRouteSearch` directly.
- Bridge also uses a minimal `DirectRouteSearchPort` for test-double injection without changing `core-routing`.
- Search invocation policy is deterministic and minimal:
  - first resolved origin `StopPointId`
  - first resolved destination `StopPointId`
- No ranking, scoring, or inference is added in PASS 13.

## StopPointId Anti-Fabrication Rule

- Bridge never derives `StopPointId` from:
  - `displayName`
  - stop-group names
  - manual text
  - coordinates
- If explicit IDs are absent, bridge returns `NotReady` and does not call route search.

## Deterministic Selection Policy

When multiple resolved IDs exist, bridge uses:

- `resolvedOriginIds.first()`
- `resolvedDestinationIds.first()`

This is intentional PASS 13 behavior and is covered by tests.

## Tests Added

Added:

- `feature-search/src/test/kotlin/ee/androbus/feature/search/bridge/DirectRouteQueryBridgeTest.kt`

Coverage includes:

- all `NotReady` precondition variants
- unresolved call guard with throwing route-search port
- success path (`RouteFound`) with explicit StopPoint IDs
- not-found path (`RouteNotFound`) with explicit StopPoint IDs
- anti-fabrication behavior when only names/text exist
- deterministic first-ID selection
- Android-free reflection guard

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected PASS 13 scoped changes only

## Files Changed

- `feature-search/build.gradle.kts`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/DirectRouteQueryBridge.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/DirectRouteQueryBridgeResult.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/bridge/DirectRouteQueryBridgeTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/TRUTH_INDEX.md`
- `docs/audit/PASS_13_DIRECT_ROUTE_QUERY_BRIDGE_AND_PRECONDITION_SPEC.md`

## No Out-of-Scope Changes Confirmation

- No UI/Compose/ViewModel changes.
- No Room changes.
- No network/downloader/realtime changes.
- No GTFS parser changes.
- No `core-routing` source changes.
- No nearest-stop geospatial implementation.

## Risks / Unknowns

- Bridge still depends on future verified stop-point candidate resolution.
- First-ID selection policy is deterministic but naive; ranking/resolution quality is future work.
- Multiple unresolved candidate sources still require a formal verified mapping contract.

## Recommended PASS 14

`PASS 14 — STOPPOINT_RESOLUTION_CONTRACT`

