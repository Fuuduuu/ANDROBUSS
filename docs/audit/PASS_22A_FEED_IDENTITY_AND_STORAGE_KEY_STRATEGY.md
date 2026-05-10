# PASS_22A_FEED_IDENTITY_AND_STORAGE_KEY_STRATEGY

## Objective

Define and document storage-identity strategy before Room implementation:
- GTFS raw IDs are treated as feed/city-local for persistence.
- Future Room entity keys must be composite and city/feed-scoped.

No Room/DAO/AppDatabase/provider/runtime wiring is implemented in this pass.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean at pass start
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `709010ddb6cec27b1d45719765bf4065f248fcf2`
- `git log --oneline -10`: HEAD `709010d feat(search): add domain feed snapshot provider contract`

## Files Read

- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/audit/PASS_21_DOMAIN_FEED_SNAPSHOT_AND_PROVIDER_CONTRACT.md`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/feed/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/feed/**`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/TransitIds.kt`
- `core-domain/src/test/kotlin/**`
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/GtfsDomainMapper.kt`
- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/GtfsDomainMapperTest.kt`

## Decision Captured (PASS 22A)

- `StopPointId` currently comes from raw GTFS `stop_id`.
- `RoutePatternId` currently comes from `RoutePatternId("pattern:${tripId}")`.
- These IDs are treated as feed/city-local for persistence storage.
- Future Room persistence keys must be composite:
  - stop-point key: `cityId + feedId + stopId`
  - route-pattern key: `cityId + feedId + patternId`
  - pattern-stop key: `cityId + feedId + patternId + sequence`
- Pattern stops must preserve sequence ordering and allow repeated `stopId` values inside one route pattern.

## Minimal Encoding Result

- `FeedId` already exists in `core-domain` (`TransitIds.kt`), so no new domain ID type was added.
- No runtime code behavior changes were introduced.
- Strategy is encoded in architecture/truth/testing/protected-surface docs for PASS 22 follow-up implementation.

## Docs Updated

- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/audit/PASS_22A_FEED_IDENTITY_AND_STORAGE_KEY_STRATEGY.md`

## Explicit Non-Changes

- No Room/DAO/AppDatabase code.
- No data-local provider implementation.
- No parser behavior changes.
- No routing/search behavior changes.
- No UI/Compose/ViewModel/app wiring changes.
- No network/realtime/nearest-stop/cache changes.

## Validation Result

- `.\gradlew.bat :core-domain:test` -> PASS
- `.\gradlew.bat :feature-search:test` -> PASS
- `.\gradlew.bat build` -> PASS
- `git diff --check` -> PASS (line-ending warnings only)
- `git status --short --untracked-files=all` -> PASS 22A scoped files only

## Risks / Unknowns

- Composite persistence-key strategy is now documented but not yet implemented in schema/DAO/provider code.
- Feed contract remains in `feature-search` until the next pass moves it to `core-domain`.

## Recommended Next Pass

- `PASS 22 — FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_BASELINE`
  - move feed contract to `core-domain`
  - add minimal Room schema using composite storage keys
  - add load-then-serve Room provider baseline
