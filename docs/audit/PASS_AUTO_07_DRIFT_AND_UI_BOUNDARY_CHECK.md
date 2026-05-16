# PASS_AUTO_07_DRIFT_AND_UI_BOUNDARY_CHECK

## Objective

Run a docs-only governance drift check and UI boundary verification after PASS 31 quick-destination chips, without changing runtime/source/build behavior.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD: `bcf62983d06edd6bf77a22cb1b24ba91392f6e88`
- Working tree at start: clean
- Gate validator: `py -3 tools/validate_project_state.py` passed (warning-only stale commit message before sync)

## Files Read

- Governance/state docs:
  - `docs/PROJECT_STATE.yml`
  - `docs/CURRENT_STATE.md`
  - `docs/ROADMAP.md`
  - `docs/AUDIT_INDEX.md`
  - `docs/TRUTH_INDEX.md`
  - `docs/INVARIANTS.md`
  - `docs/PROTECTED_SURFACES.md`
  - `docs/CODEBASE_IMPACT_MAP.md`
  - `docs/ANDROID_ARCHITECTURE.md`
  - `docs/GTFS_PIPELINE.md`
  - `docs/ROUTING_LOGIC.md`
  - `docs/UX_PRINCIPLES.md`
  - `docs/TESTING_STRATEGY.md`
- Relevant audits:
  - `docs/audit/PASS_30_REAL_RAKVERE_STATIC_RUNTIME_PROFILE_BASELINE.md`
  - `docs/audit/PASS_31_RAKVERE_QUICK_DESTINATIONS_UI_ONLY.md`
- Runtime/UI boundary context:
  - `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
  - `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchViewModel.kt`
  - `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchUiState.kt`
  - `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
  - `app/src/test/kotlin/ee/androbus/app/presentation/search/RakvereQuickDestinationReadinessTest.kt`
  - `app/src/main/assets/bootstrap/rakvere_feed_20260428.json`
  - `app/build.gradle.kts`
  - `config/detekt/**`

## Drift Findings

- PASS 31 state was still represented as current candidate in governance docs.
- `PROJECT_STATE.yml` `last_accepted_commit` and current pass fields were stale.
- Drift counter still showed `5 / 5` and required reset after this checkpoint.
- `ROADMAP` and `AUDIT_INDEX` needed explicit PASS 31 accepted status.

## Docs Updated

- `docs/PROJECT_STATE.yml`
  - `last_accepted_commit` and `last_known_good_commit` set to `bcf62983d06edd6bf77a22cb1b24ba91392f6e88`
  - current pass set to `PASS_AUTO_07_DRIFT_AND_UI_BOUNDARY_CHECK`
  - PASS 31 added to accepted guardrails
  - drift counter reset to `0 / 5`
  - next recommended pass set to narrow scope-audit path
- `docs/CURRENT_STATE.md`
  - latest accepted HEAD updated to PASS 31 commit
  - PASS 31 marked accepted
  - current pass updated to AUTO-07
  - PASS 31 quick-chip boundaries explicitly recorded
- `docs/ROADMAP.md`
  - PASS 31 marked completed
  - AUTO-07 marked current governance checkpoint candidate
  - next passes kept narrow and scope-audit-first
- `docs/AUDIT_INDEX.md`
  - PASS 31 row marked accepted with commit `bcf6298`
  - PASS AUTO-07 row added as current candidate
- `docs/TESTING_STRATEGY.md`
  - PASS 31 quick-destination coverage updated from candidate -> accepted
  - AUTO-07 boundary verification coverage note added
- `docs/CODEBASE_IMPACT_MAP.md`
  - snapshot baseline updated to AUTO-07 candidate
  - PASS 31 impact added (UI-only quick-chip wiring, no scope expansion)
- `docs/ANDROID_ARCHITECTURE.md`
  - status heading updated to after PASS 31
  - PASS 31 quick-destination UI layer documented
  - PASS 30 wording corrected to accepted baseline
- `docs/UX_PRINCIPLES.md`
  - implementation alignment updated to after PASS 31
  - quick-chip label/query-text policy and Tõrma exclusion recorded

## Quick Destination Boundary Verification Results

- Parser types in app/data-local production:
  - `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main` -> no matches
- Core-domain serialization:
  - `rg -n "@Serializable" core-domain/src` -> no matches
- Room main-thread query:
  - `rg -n "allowMainThreadQueries" data-local/src` -> no matches
- Hilt leak into core/feature-search:
  - `rg -n "@HiltAndroidApp|@Module|@InstallIn|@Inject" core-domain core-gtfs core-routing feature-search` -> no matches
- `StopPointId` in `SearchScreen.kt`:
  - matches exist only in existing origin-chip section/types; no quick-chip `StopPointId` shortcuts found
- Quick-chip vs search-route wiring:
  - quick-chip path uses `handleQuickDestinationSelection(...)->onDestinationSelect(queryText)`
  - no quick-chip call path invokes `searchRoute`/`onSearch`
  - `searchRoute` appears only in existing "Otsi" button flow
- GPS/network/realtime/workmanager keywords:
  - no matches in `app/src/main`, `data-local/src/main`, `feature-search/src/main`
- Navigation keywords:
  - no `NavHost` / `NavController` / `rememberNavController` matches
- Realtime/live wording:
  - no `app/src/main` matches
  - docs-only future notes remain in `docs/UX_PRINCIPLES.md` and `docs/CURRENT_STATE.md` (acceptable as non-runtime context)

## Runtime Feed / Quick-Destination Consistency

- `app/src/main/assets/bootstrap/rakvere_feed_20260428.json` exists.
- `app/src/main/assets/bootstrap/rakvere_bootstrap.json` exists as fallback.
- Quick-destination doc policy aligns with UI:
  - `Rakvere bussijaam`, `Polikliinik`, `Näpi`, `Keskväljak`, `Põhjakeskus -> query Põhja`
  - `Tõrma` excluded
- Docs do not claim public-production freshness is solved.

## Build/Test/Detekt Result

Commands run:

- `.\gradlew.bat detekt`
- `.\gradlew.bat test`
- `.\gradlew.bat build`

Result:

- all passed.

## Dependency Lock Consistency

- `app/gradle.lockfile` exists.
- dependency locking remains enabled (`lockAllConfigurations()` present in root `build.gradle.kts`).
- no lockfile updates were performed in this pass.

## Source-Code Untouched Confirmation

- This pass changed docs only.
- No source/build/config/lockfile/runtime files were modified.

## Drift Counter Reset

- Reset to `0 / 5` after this docs-only drift checkpoint.

## Next Recommended Pass

- `PASS_32_ORIGIN_SELECTION_IMPROVEMENT_SCOPE_AUDIT`
- Alternative: `PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT`
- Alternative: `PASS_UI_02_SEARCH_SCREEN_POLISH_SCOPE_AUDIT`

## Remaining Risks

- Runtime remains internal/MVP static-feed baseline; public-production freshness/update lifecycle is unresolved.
- Origin selection remains dev/MVP chip-based and needs dedicated scope-audited improvement pass.
- UI remains single-screen MVP baseline and not production-ready.
