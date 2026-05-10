# PASS_18_DESTINATION_ENRICHMENT_ORCHESTRATOR_AND_AMBIGUITY_SPEC

## Objective

Add a destination-side production orchestration layer that coordinates stop-candidate enrichment, preserves ambiguity, and keeps routing-bridge invocation out of scope.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `ebd1ff3c8c0a52975612d68bfd09d805dd3ccdad`
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
- `docs/audit/PASS_16_STOP_CANDIDATE_ENRICHMENT_PRODUCTION.md`
- `docs/audit/PASS_16B_ENRICHMENT_DOCS_AND_DIAGRAMS_SYNC.md`
- `docs/audit/PASS_17_RAKVERE_REAL_GTFS_STOP_NAME_DISCOVERY_AND_METADATA_SPEC.md`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/**`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`

## Why Destination-Only

PASS 18 scope is limited to destination-side orchestration.  
Origin enrichment and route-query execution remain separate concerns and are deferred.

## Why Origin Enrichment Is Deferred

Origin enrichment includes separate policy questions (manual text vs location seed, nearest-stop strategy, fallback semantics) that are outside PASS 18 and would broaden risk.

## Production Classes Added

Added package `ee.androbus.feature.search.orchestration`:

- `DestinationEnrichmentResult`
- `DestinationEnrichmentOrchestrator`

## Orchestration Behavior

- Accepts destination `StopCandidate` list + `CityId`.
- Uses existing `StopCandidateEnricher` for each candidate.
- Returns:
  - `NoCandidates` when input list is empty.
  - `NoneEnriched` when all enrichments fail.
  - `Enriched` when at least one candidate succeeds.
- Preserves both successful and failed enrichment results.
- Preserves all verified stop-point candidates produced by resolver pipeline.

## Result Model

- `DestinationEnrichmentResult.Enriched`:
  - `enrichedCandidates`
  - `failedCandidates`
  - `isAmbiguous`
- `DestinationEnrichmentResult.NoneEnriched`:
  - `failedCandidates`
- `DestinationEnrichmentResult.NoCandidates`

## Ambiguity Policy

- `isAmbiguous = true` when total verified stop-point candidate count across enriched results is greater than one.
- `isAmbiguous = false` when total verified count is exactly one.
- Orchestrator does not choose a single candidate; disambiguation is deferred to caller/ViewModel layer.

## Why Orchestrator Does Not Select `.first()`

Selecting `.first()` would hide multi-stop ambiguity and can break direction/platform correctness. PASS 18 keeps all verified candidates explicit.

## Why Orchestrator Does Not Call `DirectRouteQueryBridge`

Bridge invocation is a separate stage with separate preconditions and route-pattern inputs. PASS 18 is intentionally enrichment-only orchestration.

## Anti-Fabrication Rules

- Orchestrator relies on `StopCandidateEnricher` output.
- `StopPointId` values still flow only from `VerifiedStopPointCandidate.stopPointId`.
- No IDs are derived from stop-group names, display names, manual text, or coordinates.

## Tests Added

Added:

- `feature-search/src/test/kotlin/ee/androbus/feature/search/orchestration/DestinationEnrichmentOrchestratorTest.kt`

Coverage includes:

1. `NoCandidates` branch.
2. `NoneEnriched` branch with preserved failures.
3. Single verified candidate -> `Enriched` + `isAmbiguous=false`.
4. Same-name two-stop candidate -> `Enriched` + `isAmbiguous=true` with both candidates preserved.
5. Mixed success/failure preservation.
6. Explicit ambiguity threshold checks (`==1` vs `>1`).
7. No `DirectRouteQueryBridge` dependency/reference in orchestrator contract.
8. Anti-fabrication ID checks (`RKV_C` != `Jaam`/`jaam`).
9. Deterministic repeated-call behavior.
10. Android-free reflection guard.

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> PASS 18 scoped files only

## Files Changed

- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/DestinationEnrichmentOrchestrator.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/DestinationEnrichmentResult.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/orchestration/DestinationEnrichmentOrchestratorTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/audit/PASS_18_DESTINATION_ENRICHMENT_ORCHESTRATOR_AND_AMBIGUITY_SPEC.md`

## No UI/Room/Network/Parser/Routing-Change Confirmation

- No UI/Compose/ViewModel changes.
- No Room/DAO/AppDatabase changes.
- No network/downloader/realtime/cache changes.
- No parser or `core-gtfs` behavior changes.
- No `core-domain` or `core-routing` source changes.
- No `DirectRouteSearch` behavior/signature changes.
- No `DirectRouteQueryBridge` behavior/signature changes.
- No `InMemoryStopPointIndex` behavior/signature changes.
- No `StopCandidateEnricher` behavior/signature changes.
- No origin enrichment production class added.

## Risks / Unknowns

- Ambiguity resolution policy is still deferred to future pass.
- Bridge wiring/use-case sequencing remains pending.
- Origin-side production enrichment remains deferred.

## Recommended PASS 19

`PASS 19 — DESTINATION_ROUTE_QUERY_PREPARATION_USE_CASE`
