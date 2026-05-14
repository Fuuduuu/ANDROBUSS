# AUDIT_INDEX.md — ANDROBUSS audit manifest

Purpose:
This file is a lightweight index of accepted audit/pass documents.
It supports lazy context loading: AI should read this index first and only open full audit docs when detail is needed.

Rules:
- This file is not canonical truth.
- Canonical current truth remains `PROJECT_STATE` / `TRUTH_INDEX` / `INVARIANTS` / `CURRENT_STATE`.
- Full audit docs live in `docs/audit/`.
- Archived docs are historical only.

| Pass / Commit | Topic | Audit file | Status | Read when |
|---|---|---|---|---|
| PASS 20 / `1ae1daa` | GTFS fixture pipeline integration proof | `docs/audit/PASS_20_GTFS_FIXTURE_TO_SEARCH_PIPELINE_INTEGRATION_TEST.md` | accepted | Need parser->search fixture integration behavior |
| PASS 20B / `8cdd748` | GTFS pipeline docs/diagram sync | `docs/audit/PASS_20B_GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC.md` | accepted | Need docs/diagram rationale after PASS 20 |
| PASS 21 / `709010d` | Domain feed snapshot/provider contract | `docs/audit/PASS_21_DOMAIN_FEED_SNAPSHOT_AND_PROVIDER_CONTRACT.md` | accepted | Need snapshot/provider boundary intent |
| PASS 22A / `e1a480e` | Scoped storage identity strategy | `docs/audit/PASS_22A_FEED_IDENTITY_AND_STORAGE_KEY_STRATEGY.md` | accepted | Need city/feed scoped key rationale |
| PASS 22B / `49dd54d` | Feed contract move + Room scoped-key baseline | `docs/audit/PASS_22B_FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_WITH_SCOPED_KEYS.md` | accepted | Need Room baseline constraints and boundaries |
| PASS 23 / `0d09b36` | FeedSnapshotImporter + CI test step | `docs/audit/PASS_23_FEED_SNAPSHOT_IMPORTER_AND_CI_TEST.md` | accepted | Need importer write-path and integration evidence |
| PASS 24 / `fdf4668` | Bundled bootstrap runtime decision (docs-only) | `docs/audit/PASS_24_FEED_BOOTSTRAP_AND_RUNTIME_WIRING_DECISION.md` | accepted | Need MVP bootstrap lifecycle decision context |
| PASS 25 / `a704bbb` | Bundled feed bootstrap app-layer implementation | `docs/audit/PASS_25_BUNDLED_FEED_BOOTSTRAP_SERIALIZATION_AND_APP_LAYER.md` | accepted | Need concrete app bootstrap behavior/test scope |
| PASS G01 / `ea835f0` | Governance bootstrap docs scaffold | `docs/audit/PASS_G01_GOVERNANCE_BOOTSTRAP.md` | accepted | Need governance baseline intent |
| PASS G02 / `4be7657` | PROJECT_STATE validation hooks | `docs/audit/PASS_G02_PROJECT_STATE_VALIDATION_HOOKS.md` | accepted | Need validator contract and limits |
| PASS G03 / `5ea802f` | Audit index and read-order sync | `docs/audit/PASS_G03_AUDIT_INDEX_AND_READ_ORDER_SYNC.md` | accepted | Need lazy-context/read-order governance change details |
| PASS AUTO-01 / `d4cf9cb` | Detekt module-boundary checks | `N/A (audit doc missing in repo)` | accepted | Need forbidden-import guardrail scope |
| DRIFT_CHECK_RULE_SYNC_PASS / `241439a` | Drift-check cadence/governance rule sync | `docs/audit/DRIFT_CHECK_RULE_SYNC_PASS.md` | accepted | Need checkpoint drift-check policy details |
| PASS AUTO-02 / `3bb2fe1` | Gradle dependency locking baseline | `docs/audit/PASS_AUTO_02_DEPENDENCY_LOCKING.md` | accepted | Need lockfile/locking baseline details |
| PASS G04 / `141d409` | Governance state sync after AUTO-02 audit findings | `docs/audit/PASS_G04_GOVERNANCE_STATE_SYNC_AFTER_AUTO_02.md` | accepted | Need docs/governance drift-fix details |
| PASS 26 / `b22e405` | GTFS legal/source and parser robustness decision | `docs/audit/PASS_26_GTFS_LEGAL_SOURCE_AND_PARSER_ROBUSTNESS_DECISION.md` | accepted | Need legal/source/freshness constraints and robustness policy details |
| PASS 26A / `cedcd65` | Real-feed profile parser robustness tests | `docs/audit/PASS_26A_REAL_FEED_PROFILE_PARSER_ROBUSTNESS_TESTS.md` | accepted | Need executable parser robustness evidence before any real asset generation |
| PASS 26B / `f7708ad` | Real Rakvere dev/test asset only (test resources) | `docs/audit/PASS_26B_REAL_RAKVERE_DEV_TEST_ASSET_ONLY.md` | accepted | Need dev/test asset validation without changing runtime default |
| PASS G05 / `7f2669a` | GTFS realtime + Peatus GraphQL future notes (docs-only) | `docs/audit/PASS_G05_GTFS_REALTIME_AND_PEATUS_GRAPHQL_FUTURE_NOTES.md` | accepted | Need future-scope constraints without runtime implementation |
| PASS 27 / `1f11ec3` | App-level Hilt DI baseline for bootstrap | `docs/audit/PASS_27_HILT_DI_BASELINE.md` | accepted | Need app-owned DI baseline and boundaries before ViewModel/UI |
| PASS AUTO-03 / `ff2a88e` | Drift + boundary verification after PASS 27 | `docs/audit/PASS_AUTO_03_DRIFT_AND_BOUNDARY_CHECK.md` | accepted | Need docs/governance/state alignment before PASS 28 scope opening |
| PASS 28A / current | App SearchViewModel feed + destination state baseline | `docs/audit/PASS_28A_APP_SEARCH_VIEWMODEL_FEED_AND_DESTINATION_STATE.md` | current candidate | Need first app-layer feed-ready and destination resolution state before route-query/UI wiring |
