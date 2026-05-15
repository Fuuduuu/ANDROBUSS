# PASS_29A_RAKVERE_QUICK_DESTINATION_RESOLUTION_READINESS

## Objective

Validate whether proposed Rakvere quick destination labels are currently resolvable through the existing app destination input flow, without adding any quick-destination UI implementation.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `50383a3f03cb7c44d5f54df8d5c8e7be88798fe4`
- Working tree at start: clean
- `py -3 tools/validate_project_state.py` gate: passed

## Why Real Rakvere Quick Destination UI Is Blocked

- Current production runtime bootstrap asset is synthetic and contains only:
  - `Keskpeatus`
  - `Spordikeskus`
  - `Jaam`
- Proposed labels for quick destinations (`Rakvere bussijaam`, `Polikliinik`, `Põhjakeskus`, `Näpi`, `Keskväljak`, `Tõrma`) are not all represented in the active runtime snapshot.
- Existing destination resolution is snapshot-driven; labels absent from active snapshot cannot be treated as ready quick-destination actions.

## Real Dev Profile Status

- `app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json` is test-only.
- It may contain real-derived labels and richer stop coverage, but it is not the runtime default asset.
- Runtime default remains `app/src/main/assets/bootstrap/rakvere_bootstrap.json`.

## Safe Future Design Path

- Quick destination interaction must remain:
  - quick label/query text
  - `SearchViewModel.onDestinationChanged(queryText)`
  - existing resolver/enrichment pipeline
- Direct `StopPointId` shortcuts from UI labels are forbidden.

## Tests Added

- `app/src/test/kotlin/ee/androbus/app/presentation/search/RakvereQuickDestinationReadinessTest.kt`
  - verifies runtime synthetic asset label scope
  - verifies proposed labels are absent in runtime synthetic asset
  - verifies SearchViewModel resolves only labels present in active snapshot
  - verifies labels absent from active snapshot do not resolve
  - verifies real-derived dev profile remains separate from runtime default
  - does not create `StopPointId` values from quick-destination label strings

## Validation Result

Commands run:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`

Manual checks run:

- no quick destination UI implementation added
- no production asset changes
- no runtime source changes outside allowed files

## Files Changed

- `app/src/test/kotlin/ee/androbus/app/presentation/search/RakvereQuickDestinationReadinessTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_29A_RAKVERE_QUICK_DESTINATION_RESOLUTION_READINESS.md`

## Next Recommended Pass

- `PASS_29B_RAKVERE_QUICK_DESTINATION_METADATA_RESOLVER_SCOPE_AUDIT`
