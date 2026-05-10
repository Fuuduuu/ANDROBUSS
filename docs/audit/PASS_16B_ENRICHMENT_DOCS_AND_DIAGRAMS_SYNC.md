# PASS_16B_ENRICHMENT_DOCS_AND_DIAGRAMS_SYNC

## Objective

Synchronize architecture/state/truth/testing/diagram docs with accepted PASS 16 enrichment behavior without touching source/build/test files.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD: `1657d94fd8e74bf68997c92ae568c060c663c6ca`
- `git log --oneline -12`: PASS 16 commit present as latest
- working tree at pass start: clean

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
- `docs/audit/PASS_13_DIRECT_ROUTE_QUERY_BRIDGE_AND_PRECONDITION_SPEC.md`
- `docs/audit/PASS_14_STOPPOINT_RESOLUTION_CONTRACT_AND_NAME_INDEX.md`
- `docs/audit/PASS_15_STOPPOINT_RESOLUTION_INTEGRATION_AND_BRIDGE_WIRING.md`
- `docs/audit/PASS_UX_01_UX_BLUEPRINT_AND_MVP_SCOPE_SYNC.md`
- `docs/audit/PASS_16_STOP_CANDIDATE_ENRICHMENT_PRODUCTION.md`

Read-only source tree verification for factual sync:

- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/`

## PASS 16 Truths Synchronized

- `StopCandidateEnricher` production status is reflected in current-state and roadmap docs.
- `StopCandidateEnrichmentResult` semantics are captured as protected surface.
- Enrichment ID source policy is explicit: only `VerifiedStopPointCandidate.stopPointId`.
- Anti-fabrication rule is explicit across truth/protected/routing docs.
- Multiple same-name stop-point preservation is called out in truth/testing docs.
- Origin enrichment remains deferred and not runtime-wired.
- DirectRouteSearch semantics are documented as unchanged by PASS 16.

## Diagrams Updated

- Timeline updated through PASS 16B with PASS 17 planned.
- Added post-PASS-16 search pipeline diagram:
  - `DestinationTarget -> StopCandidate -> StopCandidateEnricher -> StopPointResolver/InMemoryStopPointIndex -> VerifiedStopPointCandidate -> enriched StopCandidate -> DirectRouteQueryBridge -> DirectRouteSearch`.
- Added bridge precondition flow diagram for `NotReady` gating.
- Added anti-fabrication ID-flow diagram showing forbidden derivation paths.
- Updated module dependency labels to reflect implemented `feature-search` core logic and city-adapter metadata state.

## Docs Changed

- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_16B_ENRICHMENT_DOCS_AND_DIAGRAMS_SYNC.md`

## Source Code Untouched Confirmation

- No Kotlin source files changed.
- No test source files changed.
- No Gradle/build files changed.
- No CI/workflow files changed.
- No parser/routing/domain/adapter runtime behavior changed.

## Validation Result

- `git diff --check`: PASS
- `git status --short --untracked-files=all`: PASS 16B docs-only scoped changes only

## Risks / Unknowns

- `StopCandidateEnricher` is still not wired into app/ViewModel/runtime flow.
- Origin-side enrichment remains deferred.
- Rakvere preferred-stop-group-name to verified stop-point mapping fixture spec is still pending (PASS 17).

## Recommended Next Pass

`PASS 17 — RAKVERE_STOPPOINT_MAPPING_FIXTURE_SPEC`
