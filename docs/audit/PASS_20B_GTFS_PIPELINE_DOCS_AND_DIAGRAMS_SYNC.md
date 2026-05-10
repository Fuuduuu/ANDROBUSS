# PASS_20B_GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC

## Objective

Synchronize architecture/state/testing/diagram docs after accepted `PASS 20`, without any source/build/test changes.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean at pass start
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `1ae1daaf57f957b824e480f652fb9923ca86edfb`
- `git log --oneline -12`: HEAD `1ae1daa test(search): prove GTFS fixture pipeline integration`

## Docs Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROADMAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CITY_ADAPTERS.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/audit/PASS_17_RAKVERE_REAL_GTFS_STOP_NAME_DISCOVERY_AND_METADATA_SPEC.md`
- `docs/audit/PASS_18_DESTINATION_ENRICHMENT_ORCHESTRATOR_AND_AMBIGUITY_SPEC.md`
- `docs/audit/PASS_19_DIRECT_ROUTE_QUERY_PREPARATION_USE_CASE.md`
- `docs/audit/PASS_20_GTFS_FIXTURE_TO_SEARCH_PIPELINE_INTEGRATION_TEST.md`

High-level source tree inspection (read-only):
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/`
- `core-routing/src/main/kotlin/ee/androbus/core/routing/`

## Docs Changed

- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_20B_GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC.md`

## Diagrams Updated

`docs/MERMAID_DIAGRAMS.md` synchronized with:
- PASS 20 fixture-to-search pipeline
- StopPointId anti-fabrication source chain
- Future provider/Room boundary (explicitly future)
- Current implemented module state after PASS 20

## PASS 20 Truths Synchronized

- PASS 20 is accepted and reflected in current state/roadmap.
- Fixture-level integration scope is explicit (not production feed ingestion).
- `rakvere-smoke` names are marked synthetic and not real Rakvere POI metadata names.
- Parser-derived `StopPoint.id` remains the only valid routing ID source in this pipeline proof.
- `MappedGtfsFeed.stopPoints` -> `InMemoryStopPointIndex` and `MappedGtfsFeed.routePatterns` -> `DirectRouteQueryPreparationUseCase` are documented.
- `feature-search` parser dependency is documented as test-scope only.

## Source Code Untouched Confirmation

- No Kotlin/source file changes.
- No test file changes.
- No Gradle/build file changes.
- No CI/workflow changes.

## Validation Result

- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> PASS (docs-only scoped changes)

## Files Changed

- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_20B_GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC.md`

## Risks / Unknowns

- Production feed snapshot/provider boundary is still undefined.
- Room/cache provider implementation is still future.
- Runtime app/ViewModel wiring for this pipeline is still future.
- `rakvere-smoke` remains synthetic and must not be conflated with real Rakvere POI naming.

## Recommended Next Pass

`PASS 21 — FEED_DOMAIN_SNAPSHOT_AND_ROUTE_PATTERN_PROVIDER_SPEC`
