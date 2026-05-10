# PASS_20_GTFS_FIXTURE_TO_SEARCH_PIPELINE_INTEGRATION_TEST

## Objective

Add a narrow integration-test-only proof that `core-gtfs` parser/mapper output integrates with the existing `feature-search` pipeline, without adding production classes or changing routing/parser behavior.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean at pass start
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `c4ad0ccc79881988f85d764c80a377ec45475f46`
- `git log --oneline -12`: includes `c4ad0cc feat(search): add direct route query preparation use case`

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/ROADMAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_07_MINIMAL_GTFS_FIXTURE_PARSER.md`
- `docs/audit/PASS_17_RAKVERE_REAL_GTFS_STOP_NAME_DISCOVERY_AND_METADATA_SPEC.md`
- `docs/audit/PASS_19_DIRECT_ROUTE_QUERY_PREPARATION_USE_CASE.md`
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/**`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/**`
- `feature-search/build.gradle.kts`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/**`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `core-routing/src/main/kotlin/ee/androbus/core/routing/**`

## Build Dependency Change

- `feature-search/build.gradle.kts`:
  - added `testImplementation(project(":core-gtfs"))`
  - no production `implementation(project(":core-gtfs"))` added

Why test-only:
- PASS 20 needs parser/mapper access in integration tests.
- `feature-search` production code must remain independent of parser module wiring.

## Fixture Used

- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`
- stop names used in PASS 20 tests are synthetic fixture names only:
  - `Jaam`
  - `Keskpeatus`
  - `Spordikeskus`

Why real `rakvere.zip` is not used:
- pass scope is fixture integration proof, not live-feed ingestion;
- no ZIP/data files are introduced into repository.

## Parser-to-Search Pipeline Flow Proven

`GtfsFeedParser.parseDirectory`  
-> `GtfsDomainMapper.map`  
-> `MappedGtfsFeed.stopPoints`  
-> `InMemoryStopPointIndex`  
-> `StopCandidateEnricher`  
-> `DestinationEnrichmentOrchestrator`  
-> `DirectRouteQueryPreparationUseCase` (+ `DirectRouteQueryBridge` + `DirectRouteSearch`) using `MappedGtfsFeed.routePatterns`

## Tests Added

Added:
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/GtfsFixtureSearchPipelineIntegrationTest.kt`

Coverage:
1. parser/mapper produce expected 4 stop points and IDs (`RKV_A_OUT`, `RKV_A_IN`, `RKV_B`, `RKV_C`)
2. parser/mapper produce expected 3 route patterns; `pattern:T1` order is `RKV_A_OUT -> RKV_B -> RKV_C`
3. parser-seeded index resolves `Jaam` to `RKV_C` with anti-fabrication assertions
4. same-name `Keskpeatus` resolves to two distinct stop IDs (`RKV_A_OUT`, `RKV_A_IN`)
5. parser-seeded enrichment resolves `Jaam` candidate
6. destination orchestration is non-ambiguous for `Jaam`
7. destination orchestration is ambiguous for `Keskpeatus`
8. full parser-derived pipeline yields `RouteFound`
9. full parser-derived pipeline yields `RouteNotFound` (reverse-direction relation)
10. ambiguous destination blocks preparation before bridge call
11. loop pattern `pattern:T3` is preserved (`RKV_A_OUT -> RKV_B -> RKV_A_OUT`)

## Anti-Fabrication Checks

- Tests assert parser-derived IDs are used (`RKV_*`).
- Tests explicitly assert resolved ID is not `StopPointId("Jaam")` and not `StopPointId("jaam")`.
- No route IDs are derived from stop names.

## Validation Result

- `.\gradlew.bat :feature-search:test` -> PASS
- `.\gradlew.bat :feature-search:build` -> PASS
- `.\gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> PASS 20 scoped files only

## Files Changed

- `feature-search/build.gradle.kts`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/GtfsFixtureSearchPipelineIntegrationTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_20_GTFS_FIXTURE_TO_SEARCH_PIPELINE_INTEGRATION_TEST.md`

## No Production-Code Change Confirmation

- No changes in `feature-search/src/main/**`.
- No changes in `core-domain`, `core-routing`, `core-gtfs/src/main`, or parser/mapper behavior.
- No provider interfaces added.

## No Room/UI/Network/Parser-Change/Routing-Change Confirmation

- No Room/DAO/AppDatabase changes.
- No UI/Compose/ViewModel changes.
- No network downloader/realtime/cache changes.
- No `DirectRouteSearch` behavior/signature changes.
- No `DirectRouteQueryBridge` behavior/signature changes.
- No `DirectRouteQueryPreparationUseCase` behavior/signature changes.
- No `DestinationEnrichmentOrchestrator` behavior/signature changes.
- No `StopCandidateEnricher` behavior/signature changes.

## Risks / Unknowns

- PASS 20 proves fixture-level integration only; production feed loading/provider wiring remains unresolved.
- `rakvere-smoke` stop names are synthetic and intentionally separate from real Rakvere POI metadata naming universe.
- RoutePattern/stop source provider abstraction is still deferred.

## Recommended PASS 21

`PASS 21 — FEED_DOMAIN_SNAPSHOT_AND_ROUTE_PATTERN_PROVIDER_SPEC`
