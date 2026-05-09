# PASS_08B_DOCS_AND_DIAGRAMS_SYNC

## Objective

Synchronize architecture/state docs and Mermaid diagrams after PASS 03 to PASS 08, without changing runtime/source/build logic.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `d8d91b0c83b588733500dccd08a20bea05bb4878`
- `git status --short --untracked-files=all` at pass start: clean

## Docs Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROADMAP.md`
- `docs/DATA_SOURCES.md`
- `docs/CITY_ADAPTERS.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/DEPLOYMENT.md`
- `docs/audit/standalone-bus-app-architecture.md`
- `docs/audit/PASS_01B_AUDIT.md`
- `docs/audit/PASS_02_AUDIT.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`
- `docs/audit/PASS_05_CORE_DOMAIN_STOP_AND_PATTERN_MODELS.md`
- `docs/audit/PASS_05B_DOMAIN_NAMESPACE_AND_GUARDRAIL_CLEANUP.md`
- `docs/audit/PASS_06_SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS.md`
- `docs/audit/PASS_07_MINIMAL_GTFS_FIXTURE_PARSER.md`
- `docs/audit/PASS_08_DIRECT_ROUTE_SEARCH_CORE.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- module `build.gradle.kts` files
- high-level source inventory for:
  - `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
  - `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/**`
  - `core-routing/src/main/kotlin/ee/androbus/core/routing/**`

## Docs Changed

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROADMAP.md`
- `docs/DATA_SOURCES.md`
- `docs/CITY_ADAPTERS.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/DEPLOYMENT.md`
- `docs/audit/PASS_08B_DOCS_AND_DIAGRAMS_SYNC.md`

## Diagram Updates

Updated Mermaid source-of-truth diagrams for:

- pass timeline through PASS 08 with PASS 09 planned,
- module dependency graph with implemented/planned labeling,
- GTFS pipeline baseline,
- direct-route algorithm flow,
- calendar resolver semantics,
- future city-adapter metadata path.

## Stale Claims Fixed

- Removed stale pre-implementation claims from README/architecture/status docs.
- Aligned docs with implemented PASS 05/06/07/08 core modules and tests.
- Kept PASS 09 as next pass and city-adapter runtime as future scope.

## Source Code Untouched Confirmation

- No changes to `app/`, `core-domain/`, `core-gtfs/`, `core-routing/`, `data-*`, `feature-*`, or `city-adapters/` source files.
- No changes to Gradle build logic or CI workflow.
- No parser/routing/runtime behavior changes in this pass.

## Validation Result

- `git diff --check`: PASS
- `git status --short --untracked-files=all`: docs-only changes in allowed files
- Optional `gradlew build` not run because this pass is docs-only and no runtime/build files were changed.

## Risks / Unknowns

- Feed legal/license confirmation remains unresolved (`UNCLEAR`/`THIRD_PARTY_INDEXED_CC0_SIGNAL`).
- City mappings outside Wave 0/1 still carry partial/unclear confidence.
- No runtime city-adapter implementation yet.

## Recommended Next Pass

`PASS 09 — RAKVERE_CITY_ADAPTER_METADATA`
