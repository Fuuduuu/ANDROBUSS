# PASS_19_DIRECT_ROUTE_QUERY_PREPARATION_USE_CASE

## Objective

Add a narrow production use-case that prepares and executes direct-route bridge queries only when destination enrichment, origin ID, and route-pattern inputs are safe.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `a2ebe41f90c91c4d03f7fd3446050e4e3d297556`
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
- `docs/audit/PASS_18_DESTINATION_ENRICHMENT_ORCHESTRATOR_AND_AMBIGUITY_SPEC.md`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/orchestration/**`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `core-routing/src/main/kotlin/ee/androbus/core/routing/**`

## Production Use-Case Added

Added in `ee.androbus.feature.search.orchestration`:

- `DirectRouteQueryPreparationUseCase`
- `DirectRouteQueryPreparationResult`

## Result Model Behavior

`DirectRouteQueryPreparationResult` branches:

- `Executed(bridgeResult)`
- `DestinationAmbiguous(enrichedCandidates)`
- `DestinationUnresolved`
- `NoCandidates`
- `OriginNotProvided`
- `NoPatternsAvailable`

## Precondition Policy

Use-case evaluation order:

1. `NoCandidates`
2. `DestinationUnresolved`
3. `DestinationAmbiguous`
4. `OriginNotProvided`
5. `NoPatternsAvailable`
6. exact-one destination candidate check via `single()`
7. bridge execution

## DestinationAmbiguous Policy

- Ambiguous destination enrichment blocks routing.
- Use-case returns `DestinationAmbiguous` and preserves enriched-candidate context for caller-level disambiguation.

## Origin Explicit StopPointId Requirement

- Origin must be explicitly provided as `StopPointId?`.
- `null` origin returns `OriginNotProvided`.

## RoutePattern Caller Responsibility

- Use-case does not load route patterns.
- Caller must provide `List<RoutePattern>`.
- Empty list returns `NoPatternsAvailable`.

## Bridge Call Policy

- Use-case may call `DirectRouteQueryBridge` only after all preconditions pass.
- Use-case does not call `DestinationEnrichmentOrchestrator`.
- Use-case does not perform enrichment itself.

## Why Use-Case Does Not Call DestinationEnrichmentOrchestrator

Separation of responsibilities:

- enrichment orchestration step remains explicit and independent,
- query preparation step consumes already-computed enrichment result.

## Why Use-Case Does Not Load RoutePatterns

Data loading/provider selection is deferred to later pass.  
PASS 19 keeps route-query preparation pure and deterministic for unit testing.

## Exact-One (`single()`) Policy

- Destination selection uses `single()` over flattened verified destination candidates.
- No hidden fallback `first()` selection.
- Malformed non-ambiguous input with multiple candidates fails loudly.

## Anti-Fabrication Rules

- Destination ID passed to bridge comes from `VerifiedStopPointCandidate.stopPointId`.
- No ID derivation from names, stop-group labels, manual text, or coordinates.

## Tests Added

Added:

- `feature-search/src/test/kotlin/ee/androbus/feature/search/orchestration/DirectRouteQueryPreparationUseCaseTest.kt`

Coverage includes:

1. `NoCandidates` -> bridge not called
2. `NoneEnriched` -> `DestinationUnresolved` -> bridge not called
3. ambiguous enrichment -> `DestinationAmbiguous` -> bridge not called
4. missing origin -> `OriginNotProvided` -> bridge not called
5. missing patterns -> `NoPatternsAvailable` -> bridge not called
6. safe route-found path -> `Executed(RouteFound)`
7. safe route-not-found path -> `Executed(RouteNotFound)`
8. reverse-direction route-not-found semantics (`DESTINATION_NOT_AFTER_ORIGIN`)
9. malformed non-ambiguous + multiple verified candidates -> `single()` fails loudly
10. precondition branches do not call bridge (call-count probe)
11. anti-fabrication destination ID assertions
12. Android-free reflection guard
13. deterministic repeated calls

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> PASS 19 scoped files only

## Files Changed

- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/DirectRouteQueryPreparationUseCase.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/DirectRouteQueryPreparationResult.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/orchestration/DirectRouteQueryPreparationUseCaseTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/audit/PASS_19_DIRECT_ROUTE_QUERY_PREPARATION_USE_CASE.md`

## No UI/Room/Network/Parser/Routing-Change Confirmation

- No UI/Compose/ViewModel changes.
- No Room/DAO/AppDatabase changes.
- No network/downloader/realtime/cache changes.
- No parser or feed-ingestion changes.
- No `core-domain` / `core-routing` source changes.
- No `DirectRouteSearch` behavior/signature changes.
- No `DirectRouteQueryBridge` behavior/signature changes.
- No `DestinationEnrichmentOrchestrator` behavior/signature changes.
- No `StopCandidateEnricher` behavior/signature changes.
- No `InMemoryStopPointIndex` behavior/signature changes.
- No origin enrichment added.

## Risks / Unknowns

- Caller-side route-pattern provider is still undefined.
- Ambiguity disambiguation UX/use-case policy remains deferred.
- End-to-end wiring with app/ViewModel remains deferred.

## Recommended PASS 20

`PASS 20 — GTFS_FEED_DOMAIN_INTEGRATION_AND_ROUTE_PATTERN_PROVIDER_SPEC`
