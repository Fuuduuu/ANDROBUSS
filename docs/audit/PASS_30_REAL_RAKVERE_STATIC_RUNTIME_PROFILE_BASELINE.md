# PASS_30_REAL_RAKVERE_STATIC_RUNTIME_PROFILE_BASELINE

## Objective

Promote a real-derived Rakvere static feed profile to app runtime primary bootstrap baseline for internal/MVP use, while preserving synthetic fallback and explicitly not claiming public-production freshness compliance.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `936e3ec5de39b45c5e91abbb9f4fa23a8399868a`
- Working tree at start: clean
- `py -3 tools/validate_project_state.py` gate: passed

## Why This Baseline Is Needed

- PASS 29A/29C established that synthetic runtime labels blocked realistic Rakvere quick-destination readiness.
- Runtime now needs real label coverage baseline in active snapshot while keeping implementation scope narrow (no downloader/network/realtime/UI expansion).

## Runtime Asset Changes

- Added runtime primary asset:
  - `app/src/main/assets/bootstrap/rakvere_feed_20260428.json`
- Source for profile content:
  - copied from `app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json`
- Updated runtime primary feed identity:
  - `feedId = "rakvere-v20260428"`
- Kept synthetic fallback unchanged:
  - `app/src/main/assets/bootstrap/rakvere_bootstrap.json`

## Source Attribution

- Added runtime asset attribution/policy note:
  - `app/src/main/assets/bootstrap/README.md`
- Runtime screen now includes source note text in `SearchScreen`:
  - `Bussiandmed: Ühistranspordiregister / Regionaal- ja Põllumajandusministeerium`

## Bootstrap Behavior (Old vs New)

Old baseline:
- cache check -> prepare synthetic feed scope -> synthetic import fallback.

New baseline:
1. cache check (`getSnapshot(cityId)`)
2. `prepare(cityId, primaryFeedId = rakvere-v20260428)`
3. if snapshot exists -> return
4. try primary asset import (`rakvere_feed_20260428.json`) + prepare primary
5. if primary missing/unreadable -> import fallback synthetic asset (`rakvere_bootstrap.json`) + prepare fallback

## Tests Updated

- `FeedBootstrapLoaderTest` now validates:
  - default bootstrap loads real static runtime profile,
  - runtime snapshot non-null with expected size (`98` stops, `7` route patterns),
  - idempotency under primary profile,
  - Room-existing primary snapshot load without requiring assets,
  - synthetic fallback when primary asset is missing,
  - missing primary+fallback is safe no-crash result,
  - anti-fabrication ID checks.

- `RakvereQuickDestinationReadinessTest` now validates:
  - runtime real static profile contains key real labels,
  - `SearchViewModel` resolves `Rakvere bussijaam` against runtime-like snapshot,
  - synthetic fallback stays synthetic when loaded explicitly,
  - IDs remain asset-derived and not label-fabricated.

## Public-Production Freshness Limitation

- This pass does not solve downloader/update/freshness policy for public/freely distributed production usage.
- This baseline is explicitly internal/MVP runtime scope only.

## Forbidden Scope Confirmation

- No changes to `core-domain`, `core-gtfs`, `core-routing`, `feature-search`, `data-local`, `data-remote`, `city-adapters` production logic.
- No Room schema/entity/DAO changes.
- No network/downloader/WorkManager/realtime/navigation/GPS additions.
- No raw GTFS ZIP committed.

## Validation Result

Commands run:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `.\gradlew.bat test`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `rg -n "allowMainThreadQueries" data-local/src`
- `rg -n "@Serializable" core-domain/src`
- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main`
- `rg -n "WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main data-local/src/main feature-search/src/main`
- `Get-ChildItem -Recurse -File -Filter *.zip`

Result:

- validator/build/test/detekt all passed.
- boundary greps reported no forbidden matches.
- no raw ZIP files were added.

## Files Changed

- `app/src/main/assets/bootstrap/rakvere_feed_20260428.json`
- `app/src/main/assets/bootstrap/README.md`
- `app/src/main/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoader.kt`
- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoaderTest.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/RakvereQuickDestinationReadinessTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_30_REAL_RAKVERE_STATIC_RUNTIME_PROFILE_BASELINE.md`

## Next Recommended Pass

- `PASS_31_RAKVERE_QUICK_DESTINATIONS_UI_ONLY_SCOPE_AUDIT`
- or `PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT`
