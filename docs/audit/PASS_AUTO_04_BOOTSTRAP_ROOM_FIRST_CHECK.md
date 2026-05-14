# PASS_AUTO_04_BOOTSTRAP_ROOM_FIRST_CHECK

## Objective

Harden app bootstrap lifecycle so cold start first attempts to load an existing Room snapshot before falling back to bundled synthetic asset import.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `0965cc3be77f5355bce16ea4cf1f35f50e2a1932`
- Working tree at start: clean
- `py -3 tools/validate_project_state.py`: passed (stale-commit warning allowed)

## Problem Fixed

Previous flow relied on `provider.getSnapshot(cityId)` cache-only check.
After process restart, provider cache can be empty even when Room already has snapshot, which could trigger unnecessary bundled import attempts.

## Old vs New Bootstrap Order

Old:
1. check provider cache
2. if empty -> load bundled asset
3. import bundled asset
4. prepare provider

New:
1. check provider cache
2. if empty -> `provider.prepare(cityId, feedId)` (Room load)
3. if cache now filled -> return
4. else load bundled asset
5. import bundled asset
6. prepare provider

## Tests Added / Updated

- `FeedBootstrapLoaderTest` keeps existing checks:
  - empty Room + existing asset imports successfully,
  - missing asset remains safe (no crash),
  - idempotency of repeated bootstrap.
- New key cold-start test:
  - pre-populate Room with importer,
  - use fresh provider cache + missing asset path,
  - `bootstrapIfNeeded()` still yields non-null snapshot via Room `prepare` path.

## Source Scope Confirmation

- Production changes only in:
  - `app/src/main/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoader.kt`
- Test changes only in:
  - `app/src/test/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoaderTest.kt`
- No Room schema/entity/DAO/provider/importer implementation changes.
- No SearchViewModel/UI/network/realtime/workmanager changes.

## Remaining Future Limitation

AUTO-04 checks only configured bootstrap feed scope (`cityId`, `feedId` pair).
Future downloader/fresher-feed policy still needs explicit active-feed and freshness design.

## Drift Counter Note

After PASS_AUTO_04 acceptance, drift counter is `3 / 5` since PASS_AUTO_03 reset
(`PASS 28A`, `PASS 28B`, `PASS_AUTO_04`).

## Validation Result

Commands executed in this pass:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat :data-local:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `.\gradlew.bat test`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main`
- `rg -n "@Serializable" core-domain/src`
- `rg -n "allowMainThreadQueries" data-local/src`
- `rg -n "WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main data-local/src/main data-remote/src/main feature-search/src/main`

## Files Changed

- `app/src/main/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoader.kt`
- `app/src/test/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoaderTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_AUTO_04_BOOTSTRAP_ROOM_FIRST_CHECK.md`

## Next Recommended Pass

- `PASS_28C_COMPOSE_SEARCH_SCREEN_SCOPE_AUDIT`
- Alternative: `PASS_AUTO_05_EXTEND_DETEKT_BOUNDARY_COVERAGE`
