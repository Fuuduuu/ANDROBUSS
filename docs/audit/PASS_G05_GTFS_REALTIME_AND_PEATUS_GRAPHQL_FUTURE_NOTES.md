# PASS_G05_GTFS_REALTIME_AND_PEATUS_GRAPHQL_FUTURE_NOTES

## Objective

Document future-only GTFS Realtime identity notes and Peatus.ee / Digitransit GraphQL metadata-source notes without changing runtime scope.

## Repo guard

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `f7708ad8a81f45348c4d697b9559511ff9824a87`
- Working tree before edits: clean
- `py -3 tools/validate_project_state.py`: passed (warning-only stale-commit warning before update)

## Files read

- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/TRUTH_INDEX.md`
- `docs/INVARIANTS.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_26_GTFS_LEGAL_SOURCE_AND_PARSER_ROBUSTNESS_DECISION.md`
- `docs/audit/PASS_26A_REAL_FEED_PROFILE_PARSER_ROBUSTNESS_TESTS.md`
- `docs/audit/PASS_26B_REAL_RAKVERE_DEV_TEST_ASSET_ONLY.md`

## Future notes added

- Added GTFS pipeline future-only notes for:
  - loop/repeated stop identity with `stop_sequence`
  - GTFS Realtime matching expectations (`trip_id` + `stop_sequence`)
  - route-pattern ID anti-synthetic policy reminder
  - future realtime freshness/runtime constraints
  - future enum note (`NEW` / `DUPLICATED`, not deprecated `ADDED`-centric approach)
  - future Peatus.ee / Digitransit GraphQL metadata-discovery ideas
- Added explicit non-scope bullets for block transfers, frequencies, alerts, vehicle positions, pathways/fares, and GraphQL integration.

## No runtime implementation confirmation

- No Kotlin/source code changed.
- No parser/routing/runtime behavior changed.
- No network/realtime/API client implementation added.
- No Room schema changes.
- No build/CI/lockfile changes.

## Validation result

Executed:

- `py -3 tools/validate_project_state.py`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`

All passed for docs-only scope.

## Files changed

- `docs/GTFS_PIPELINE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/PROJECT_STATE.yml`
- `docs/audit/PASS_G05_GTFS_REALTIME_AND_PEATUS_GRAPHQL_FUTURE_NOTES.md`

## Next recommended pass

- `PASS 27 - HILT_DI_BASELINE_SCOPE_AUDIT`
