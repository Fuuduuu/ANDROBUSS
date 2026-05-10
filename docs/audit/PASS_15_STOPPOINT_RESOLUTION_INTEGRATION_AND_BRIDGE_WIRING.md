# PASS_15_STOPPOINT_RESOLUTION_INTEGRATION_AND_BRIDGE_WIRING

## Objective

Prove that existing `feature-search` resolution and bridge components work together using hand-built domain objects, without adding production classes or parser/runtime integrations.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `c57eddf6beb741e5a1f21433c804e1c8266b3f5f`
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
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_13_DIRECT_ROUTE_QUERY_BRIDGE_AND_PRECONDITION_SPEC.md`
- `docs/audit/PASS_14_STOPPOINT_RESOLUTION_CONTRACT_AND_NAME_INDEX.md`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `core-routing/src/main/kotlin/ee/androbus/core/routing/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/origin/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/**`

## Why This Is Integration-Test-Only

PASS 15 intentionally validates component interoperability through tests only:

- no production classes added,
- no production wiring code added,
- no behavior changes to core domain/routing/search production paths.

## Why Core-GTFS Parser Is Not Used

- Scope explicitly requires hand-built domain data.
- Parser/fixture coupling is avoided to keep this pass focused on resolution/bridge integration behavior only.

## Hand-Built Domain Data

Tests define city `CityId("rakvere")` with manual `StopPoint` objects:

- `RKV_A_OUT` / `Keskpeatus` / `group:keskpeatus-out`
- `RKV_A_IN` / `Keskpeatus` / `group:keskpeatus-in`
- `RKV_B` / `Spordikeskus` / `group:spordikeskus`
- `RKV_C` / `Jaam` / `group:jaam`

And route pattern:

- `RoutePatternId("pattern:T1")` with ordered stops:
  - `RKV_A_OUT` -> `RKV_B` -> `RKV_C`

## Integration Scenarios

Implemented in `StopResolutionBridgeIntegrationTest`:

1. Full pipeline: `BothUnresolved` -> destination resolved -> `OriginUnresolved` -> origin resolved -> `RouteFound`.
2. Same-name stop resolution returns two candidates and produces different route outcomes by selected `StopPointId`.
3. Reverse direction returns `RouteNotFound` with current direct-route not-found reason.
4. Resolved candidates with empty patterns return `NoPatternsAvailable`.
5. Unresolved candidate precondition cases do not call route search (throwing fake port guard).

## Anti-Fabrication Checks

Tests assert:

- resolved `Jaam` candidate ID is exactly `StopPointId("RKV_C")`,
- it is not `StopPointId("Jaam")` or `StopPointId("jaam")`,
- unknown name returns `NoStopGroupMatch` instead of fabricated IDs.

## Same-Name StopPoint Behavior

Integration test explicitly verifies:

- `"Keskpeatus"` resolves to both `RKV_A_OUT` and `RKV_A_IN`,
- selecting `RKV_A_OUT` yields `RouteFound`,
- selecting `RKV_A_IN` yields `RouteNotFound(ORIGIN_NOT_FOUND)`.

This preserves the canonical rule: same display name is not routing identity.

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected PASS 15 scoped changes only

## Files Changed

- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/StopResolutionBridgeIntegrationTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_15_STOPPOINT_RESOLUTION_INTEGRATION_AND_BRIDGE_WIRING.md`

## No Production-Code Confirmation

- No changes under `feature-search/src/main/kotlin/**`.
- No production classes added.
- No main-module behavior changes.

## No UI/Room/Network/Parser/Routing-Change Confirmation

- No UI/Compose/ViewModel changes.
- No Room/DAO/AppDatabase changes.
- No network/downloader/realtime/cache changes.
- No parser (`core-gtfs`) changes.
- No `DirectRouteSearch` or `DirectRouteQueryBridge` code changes.

## Risks / Unknowns

- Production enrichment/wiring is still absent; integration proof is test-only.
- Multi-candidate selection policy in real user flows is still unresolved.
- No service-day aware filtering in this integration pass.

## Recommended PASS 16

`PASS 16 — STOP_CANDIDATE_ENRICHMENT_AND_BRIDGE_WIRING_PRODUCTION`

