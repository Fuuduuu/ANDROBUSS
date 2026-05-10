# PASS_16_STOP_CANDIDATE_ENRICHMENT_PRODUCTION

## Objective

Add a narrow destination-side production enrichment class that converts name-level `StopCandidate` values into verified `StopPointId`-enriched candidates using the existing stop-point resolution contract.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `b35db77b782b30581427ea543eb8a65a55bf9969`
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
- `docs/audit/PASS_14_STOPPOINT_RESOLUTION_CONTRACT_AND_NAME_INDEX.md`
- `docs/audit/PASS_15_STOPPOINT_RESOLUTION_INTEGRATION_AND_BRIDGE_WIRING.md`
- `docs/audit/PASS_UX_01_UX_BLUEPRINT_AND_MVP_SCOPE_SYNC.md`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/**`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`

## Why Only StopCandidateEnricher

PASS 16 scope is strictly destination-side enrichment for `StopCandidate` objects. Origin enrichment, bridge wiring changes, and resolver/index behavior changes are explicitly deferred to later passes.

## Production Class Added

Added in `ee.androbus.feature.search.resolution`:

- `StopCandidateEnricher`
- `StopCandidateEnrichmentResult`

## Enrichment Behavior

- Builds `StopPointResolutionInput` from `candidate.stopGroupName` and `cityId`.
- Calls `StopPointResolver.resolve(...)`.
- On `Resolved`:
  - copies `stopPointIds` from `VerifiedStopPointCandidate.stopPointId` only,
  - returns `Enriched` with both enriched candidate and full verified candidates.
- On `NotResolved`:
  - returns `NotEnriched` with original candidate unchanged and reason preserved.

## Result Model

- `Enriched(enrichedCandidate, verifiedCandidates)` for success.
- `NotEnriched(originalCandidate, reason)` for failure.

## Multiple Same-Name StopPoint Behavior

- Same-name multi-stop resolution preserves all resolved IDs in resolver-provided order.
- Enrichment exposes corresponding `verifiedCandidates` list for downstream deterministic use.

## Anti-Fabrication Rules

- `StopPointId` values are never derived from names or coordinates.
- ID source is strictly `VerifiedStopPointCandidate.stopPointId`.
- Unknown/failed resolution paths return unchanged unresolved candidate.

## Tests Added

Added:

- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/StopCandidateEnricherTest.kt`

Coverage includes:

1. exact match enrichment (`Jaam` -> `RKV_C`)
2. normalized match enrichment (`jaam` / whitespace)
3. same-name two-stop enrichment (`Keskpeatus` -> `RKV_A_OUT`, `RKV_A_IN`)
4. ID-list size equals verified-candidate size
5. unknown name -> `NotEnriched(NoStopGroupMatch)`
6. empty index -> `NotEnriched(NoIndexAvailable)`
7. `EmptyStopGroupName` branch propagated via fake resolver (without weakening `StopCandidate` invariants)
8. anti-fabrication assertions (`RKV_C` != `Jaam`/`jaam`)
9. failure path keeps `stopPointIds` empty
10. verified candidates exposed in `Enriched`
11. original candidate preserved in `NotEnriched`
12. deterministic repeated calls
13. Android-free reflection guard

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected PASS 16 scoped changes only

## Files Changed

- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/StopCandidateEnricher.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/StopCandidateEnrichmentResult.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/StopCandidateEnricherTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/audit/PASS_16_STOP_CANDIDATE_ENRICHMENT_PRODUCTION.md`

## No UI/Room/Network/Parser/Routing-Change Confirmation

- No UI/Compose/ViewModel changes.
- No Room/DAO/AppDatabase changes.
- No network/downloader/realtime/cache changes.
- No `core-domain` / `core-gtfs` / `core-routing` source changes.
- No `DirectRouteSearch` / `DirectRouteQueryBridge` / `InMemoryStopPointIndex` behavior changes.
- No origin enrichment production class added.

## Risks / Unknowns

- Production runtime wiring of enrichment into app/ViewModel flow is still pending.
- Origin-side production enrichment remains deferred.
- Candidate ranking/selection policy across multiple resolved IDs remains future work.

## Recommended PASS 17

`PASS 17 — RAKVERE_STOPPOINT_MAPPING_FIXTURE_SPEC`
