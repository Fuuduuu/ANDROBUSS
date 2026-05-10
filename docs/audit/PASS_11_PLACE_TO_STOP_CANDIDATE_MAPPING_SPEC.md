# PASS_11_PLACE_TO_STOP_CANDIDATE_MAPPING_SPEC

## Objective

Add a pure Kotlin place-to-stop candidate mapping layer in `feature-search` that turns destination targets into conservative stop-group-name candidates without UI/map/geospatial/routing/Room/network behavior.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `8a489c9a9933f31e7c876e995789ba9e1a495ed0`
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
- `docs/audit/PASS_09_RAKVERE_CITY_ADAPTER_METADATA.md`
- `docs/audit/PASS_10_DESTINATION_TARGET_MODEL_AND_PLACE_RESOLVER_SPEC.md`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `city-adapters/src/main/kotlin/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/build.gradle.kts`

## Build/Dependency Changes

No dependency changes were required in this pass.

## Stop Candidate Model

Added in `ee.androbus.feature.search.destination`:

- `StopCandidate`
- `StopCandidateSource`
- `StopCandidateConfidence`
- `PlaceToStopCandidateResult`
- `PlaceToStopCandidateNotFoundReason`
- `PlaceToStopCandidateResolver`

Policy constraints in model:

- no blank target IDs or stop-group names,
- no fabricated `StopPointId` values,
- unresolved candidates are represented as name-only metadata candidates.

## Resolver Behavior

`PlaceToStopCandidateResolver.resolveCandidates(target)`:

- returns `NotFound(UNSUPPORTED_TARGET_SOURCE)` when target source is not `CITY_PLACE_METADATA`
- returns `NotFound(NO_PREFERRED_STOP_GROUPS)` when preferred stop-group names are empty
- returns `Found(candidates)` when preferred stop-group names exist

For found cases:

- each preferred stop-group name becomes one `StopCandidate`
- candidate ordering preserves metadata order
- `source = CITY_PLACE_PREFERRED_STOP_GROUP_NAME`
- `confidence = EXPLICIT_METADATA`
- `stopPointIds` remains empty (name-only unresolved stage)

## Candidate Confidence Policy

- `EXPLICIT_METADATA`: preferred stop-group name explicitly listed in destination target metadata.
- `NAME_ONLY_UNRESOLVED`: reserved for future downgrade scenarios.
- `UNCLEAR`: reserved for future low-confidence scenarios.

Current PASS 11 resolver returns `EXPLICIT_METADATA` for emitted candidates and keeps `stopPointIds` empty.

## Rakvere Metadata Use/Changes

- PASS 11 did not modify `city-adapters` metadata.
- Rakvere metadata is consumed as-is.
- Tests cover both:
  - current unresolved case (empty preferred stop-group names -> `NO_PREFERRED_STOP_GROUPS`),
  - explicit preferred-stop-group scenario by enriching resolved target in test setup only.

## Tests Added

Added:

- `PlaceToStopCandidateResolverTest`

Coverage includes:

- no preferred stop groups -> `NO_PREFERRED_STOP_GROUPS`
- unsupported target source -> `UNSUPPORTED_TARGET_SOURCE`
- one preferred stop-group name -> one candidate
- multiple preferred stop-group names preserve order
- no fabricated `StopPointId` values
- expected candidate source and confidence
- Rakvere bussijaam candidate scenario
- uncertain Rakvere place unresolved scenario
- Android-free reflection guard

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected scoped changes only

## Files Changed

- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/StopCandidate.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/PlaceToStopCandidateResult.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/PlaceToStopCandidateResolver.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/destination/PlaceToStopCandidateResolverTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/TRUTH_INDEX.md`
- `docs/audit/PASS_11_PLACE_TO_STOP_CANDIDATE_MAPPING_SPEC.md`

## No Out-of-Scope Changes Confirmation

- No UI or Compose changes.
- No Room changes.
- No network/realtime changes.
- No routing behavior changes.
- No GTFS parser behavior changes.
- No nearest-stop/geospatial engine behavior.

## Risks / Unknowns

- Candidate mapping remains name-only until stop-group/stop-point lookup layer exists.
- Metadata quality still drives candidate quality.
- No distance/ranking or service-aware candidate pruning in this pass.

## Recommended PASS 12

`PASS 12 — ORIGIN_STOPPOINT_CANDIDATE_RESOLVER_SPEC`
