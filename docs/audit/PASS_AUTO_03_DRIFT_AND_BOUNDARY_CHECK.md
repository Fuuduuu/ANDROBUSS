# PASS_AUTO_03_DRIFT_AND_BOUNDARY_CHECK

## Objective

Run a docs-only governance drift and boundary verification pass after accepted PASS 27 Hilt baseline, before opening PASS 28 ViewModel/feed-state scope.

## Repo guard result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `1f11ec3ef12f7034d2f317e978baea72f15cad47`
- Working tree before edits: clean
- `py -3 tools/validate_project_state.py`: passed

## Files read

- Governance/docs:
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
  - `docs/TESTING_STRATEGY.md`
- Recent audits:
  - `docs/audit/PASS_27_HILT_DI_BASELINE.md`
  - `docs/audit/PASS_26B_REAL_RAKVERE_DEV_TEST_ASSET_ONLY.md`
  - `docs/audit/PASS_G05_GTFS_REALTIME_AND_PEATUS_GRAPHQL_FUTURE_NOTES.md`
- Build/boundary context:
  - `config/detekt/**`
  - `build.gradle.kts`
  - `gradle/libs.versions.toml`
  - `app/build.gradle.kts`
  - `app/src/main/kotlin/ee/androbus/app/AndrobussApplication.kt`
  - `app/src/main/kotlin/ee/androbus/app/di/**`
  - `data-local/src/main/**`
  - `feature-search/src/main/**`
  - `core-domain/src/main/**`
  - `core-gtfs/src/main/**`
  - `core-routing/src/main/**`

## Drift findings

- `PROJECT_STATE` was stale:
  - `last_accepted_commit` still pointed at `f7708ad`
  - `current_pass` still pointed at PASS 27
  - drift counter remained `5 / 5`
- `CURRENT_STATE` was stale:
  - latest accepted HEAD still pointed to `7f2669a`
  - PASS 27 described as candidate instead of accepted
- `ROADMAP` was stale:
  - PASS 27 still marked current candidate
  - PASS 28 scope-audit sequencing was not explicit
- `AUDIT_INDEX` was stale:
  - PASS 27 row still current candidate
  - no PASS AUTO-03 row
- `CODEBASE_IMPACT_MAP` contained stale pre-Hilt wording in current-state snapshot.

## Docs updated

- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/ANDROID_ARCHITECTURE.md` (optional stale candidate wording sync)
- `docs/audit/PASS_AUTO_03_DRIFT_AND_BOUNDARY_CHECK.md`

## Boundary verification results

- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main` -> no matches
- `rg -n "@Serializable" core-domain/src` -> no matches
- `rg -n "allowMainThreadQueries" data-local/src` -> no matches
- `rg -n "@HiltAndroidApp|@Module|@InstallIn|@Inject" core-domain core-gtfs core-routing feature-search` -> no matches
- `rg -n "WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main data-local/src/main data-remote/src/main feature-search/src/main` -> no matches in existing source roots
  - Note: `data-remote/src/main` path does not exist in current repo layout.

## Build/test/detekt result

- `.\gradlew.bat detekt` passed
- `.\gradlew.bat test` passed
- `.\gradlew.bat build` passed

## Dependency lock consistency

- `app/gradle.lockfile` exists.
- Hilt dependencies are present and locked in `app/gradle.lockfile`:
  - `com.google.dagger:hilt-android:2.52`
  - `com.google.dagger:hilt-compiler:2.52`
- Root dependency locking is still enabled (`lockAllConfigurations()`).
- No lockfiles were regenerated in this pass.

## Source-code untouched confirmation

- No Kotlin/source/build/runtime files were edited in this pass.
- This pass performed docs-only synchronization and verification.

## Validation result

- `py -3 tools/validate_project_state.py` passed
- `.\gradlew.bat detekt` passed
- `.\gradlew.bat test` passed
- `.\gradlew.bat build` passed
- `git diff --check` passed
- `git status --short --untracked-files=all` checked
- `git diff --name-only` checked

## Drift counter reset note

- Drift counter reset to `0 / 5` after docs/state alignment and boundary verification.

## Next recommended pass

- `PASS_28 — SEARCH_VIEWMODEL_AND_FEED_STATE_SCOPE_AUDIT`
- then `PASS_28A — SEARCH_VIEWMODEL_AND_FEED_STATE` only after PASS 28 scope lock.

## Remaining risks

- ViewModel/UI scope is still unopened and must remain behind PASS 28 scope audit.
- Realtime/network/WorkManager remain future scope and protected boundary decisions.
- Real Rakvere production asset is still blocked by legal/source/freshness policy constraints.
