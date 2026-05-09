# PASS_08_DIRECT_ROUTE_SEARCH_CORE

## Objective

Implement the first minimal direct-route search core in `core-routing` as pure Kotlin with executable tests, using `StopPointId` and `RoutePattern` ordering rules only.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean before changes
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `e868480997e7c694e7455e6dce6d6bcdac224ab3`
- `git log --oneline -9`: PASS history verified

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/CITY_ADAPTERS.md`
- `docs/TESTING_STRATEGY.md`
- `docs/ROADMAP.md`
- `docs/audit/PASS_05_CORE_DOMAIN_STOP_AND_PATTERN_MODELS.md`
- `docs/audit/PASS_05B_DOMAIN_NAMESPACE_AND_GUARDRAIL_CLEANUP.md`
- `docs/audit/PASS_06_SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS.md`
- `docs/audit/PASS_07_MINIMAL_GTFS_FIXTURE_PARSER.md`
- all files under `core-domain/src/main/kotlin/ee/androbus/core/domain/`
- all files under `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/`
- all fixture files under `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`
- `core-routing/build.gradle.kts`
- current `core-routing` structure

## Build/Dependency Changes

- `core-routing/build.gradle.kts`:
  - retained: `implementation(project(":core-domain"))`
  - added: `testImplementation(kotlin("test"))`

## Direct-Route Rule Implemented

Valid direct candidate requires:

- origin `StopPointId` and destination `StopPointId` are both in the same `RoutePattern`
- and destination index is after origin index (`destinationIndex > originIndex`)

No stop-name/group inference is used for identity.

## API / Result Shape

Added in `ee.androbus.core.routing`:

- `DirectRouteSearch`
  - `findDirectRoutes(origin, destination, patterns): DirectRouteSearchResult`
- `DirectRouteSearchResult`
  - `Found(candidates: List<DirectRouteCandidate>)`
  - `NotFound(reason: DirectRouteNotFoundReason)`
- `DirectRouteCandidate`
  - `routePatternId`
  - `originStopPointId`
  - `destinationStopPointId`
  - `originSequence`
  - `destinationSequence`
  - `segmentStopCount`
  - `segmentStopPointIds`
- `DirectRouteNotFoundReason`
  - `ORIGIN_NOT_FOUND`
  - `DESTINATION_NOT_FOUND`
  - `SAME_STOP`
  - `NO_DIRECT_PATTERN`
  - `DESTINATION_NOT_AFTER_ORIGIN`

## Not-Found Reason Precedence

Deterministic order:

1. `SAME_STOP`
2. `ORIGIN_NOT_FOUND`
3. `DESTINATION_NOT_FOUND`
4. `DESTINATION_NOT_AFTER_ORIGIN` (shared pattern exists, but only reverse order)
5. `NO_DIRECT_PATTERN` (origin and destination exist globally, but not in same pattern)

If both origin and destination are absent, result is `ORIGIN_NOT_FOUND` by precedence.

## Loop / Duplicate StopPointId Policy

- Duplicate stop points within a pattern are allowed.
- Search evaluates all origin indices and all destination indices in each pattern.
- Candidate is valid if any destination index is greater than an origin index.
- For each pattern, earliest valid origin/destination index pair is chosen.

## Tests Added

- `core-routing/src/test/kotlin/ee/androbus/core/routing/DirectRouteSearchTest.kt`

Coverage includes:

- direct route success in same pattern
- reverse-direction failure
- no-shared-pattern failure
- missing origin
- missing destination
- deterministic both-missing precedence
- same stop failure
- StopPointId identity protection (no shared-name inference)
- duplicate stop point loop-pattern handling
- ordered segment extraction
- deterministic multi-pattern ordering
- Android-free reflection guard

## Validation Result

- Java setup in shell:
  - `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`
- `.\gradlew.bat :core-routing:test` -> **BUILD SUCCESSFUL**
- `.\gradlew.bat :core-routing:build` -> **BUILD SUCCESSFUL**
- `.\gradlew.bat build` -> **BUILD SUCCESSFUL**
- `git diff --check` -> no patch-format errors
- `git status --short --untracked-files=all` -> expected PASS 08 changes only
- Note: full build emits non-blocking AGP warning (`compileSdk 35` with AGP `8.5.2`).

## Files Changed

- `core-routing/build.gradle.kts`
- `core-routing/src/main/kotlin/ee/androbus/core/routing/DirectRouteModels.kt`
- `core-routing/src/main/kotlin/ee/androbus/core/routing/DirectRouteSearch.kt`
- `core-routing/src/test/kotlin/ee/androbus/core/routing/DirectRouteSearchTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/TRUTH_INDEX.md`
- `docs/audit/PASS_08_DIRECT_ROUTE_SEARCH_CORE.md`

## Android-Free Confirmation

- No `android.*` imports.
- No `Context` usage.
- `core-routing` remains pure Kotlin/JVM.

## No Room / UI / Network / City-Adapter Confirmation

- No Room schema/entities/DAOs.
- No UI/ViewModel/Compose code.
- No network downloader or realtime logic.
- No city-adapter implementation.
- No transfer routing / nearest-stop / map logic.

## Risks / Unknowns

- This pass is pattern-based direct routing only; no schedule-time filtering or trip availability filtering yet.
- No transfer routing in scope yet.
- Integration with adapter-specific stop selection and city metadata remains future work.

## Recommended PASS 09

`PASS 09 — RAKVERE_CITY_ADAPTER_METADATA`
