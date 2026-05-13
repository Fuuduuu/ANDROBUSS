# PASS_26B_REAL_RAKVERE_DEV_TEST_ASSET_ONLY

## Objective

Add a real-derived Rakvere dev/test BootstrapFeed asset under `app/src/test/resources` only, verify DTO/domain identity safety, and keep production runtime default synthetic.

## Why test resources, not main assets

- Real-derived JSON is restricted to test scope for PASS 26B.
- Runtime default remains `app/src/main/assets/bootstrap/rakvere_bootstrap.json`.
- This pass does not switch production bootstrap source.

## Asset placement

- Added/validated: `app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json`
- Added/validated: `app/src/test/resources/bootstrap/README.md`
- Confirmed unchanged: `app/src/main/assets/bootstrap/rakvere_bootstrap.json`

## Source/license/freshness note

- Real-derived dev/test profile remains non-production.
- Legal/source/freshness constraints from PASS 26 remain active.
- No raw `rakvere.zip` is committed.

## RoutePattern ID correction

- Dev/test asset route-pattern IDs are retained GTFS trip-derived values.
- Synthetic counter-style IDs (e.g. `rakvere-dev-pattern*`) are rejected by tests.

## Tests added

- `app/src/test/kotlin/ee/androbus/app/bootstrap/RakvereDevProfileAssetTest.kt`
  - resource load and decode through `BootstrapFeedDto`
  - identity markers (`cityId`, `feedId`)
  - expected size (`98` stop points, `7` route patterns)
  - anti-fabrication stop-ID assertions
  - non-synthetic route-pattern ID assertions
  - route-pattern stop-reference integrity assertions
  - runtime default synthetic-asset guard assertion

## Runtime default unchanged confirmation

- `FeedBootstrapLoader` default asset path remains `bootstrap/rakvere_bootstrap.json`.
- No changes were made to `app/src/main/kotlin`.

## Boundary grep results

- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main` -> no matches.
- `rg -n "@Serializable" core-domain/src` -> no matches.
- `rg -n "allowMainThreadQueries" data-local/src` -> no matches.

## Validation result

- `py -3 tools/validate_project_state.py` -> PASSED
- `.\gradlew.bat :app:test` -> PASSED
- `.\gradlew.bat :core-gtfs:test` -> PASSED
- `.\gradlew.bat detekt` -> PASSED
- `.\gradlew.bat build` -> PASSED
- `git diff --check` -> PASSED (only LF/CRLF warnings)
- `git status --short --untracked-files=all` -> only PASS 26B scoped files changed

## Files changed

- `app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json`
- `app/src/test/resources/bootstrap/README.md`
- `app/src/test/kotlin/ee/androbus/app/bootstrap/RakvereDevProfileAssetTest.kt`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/PROJECT_STATE.yml`
- `docs/audit/PASS_26B_REAL_RAKVERE_DEV_TEST_ASSET_ONLY.md`

## Remaining blockers for production real asset

- Production freshness/update policy is still unresolved.
- Real-derived asset is not approved as runtime default.

## Next recommended pass

- `PASS 27 - HILT_DI_BASELINE_SCOPE_AUDIT`
