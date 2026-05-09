# PASS_05_CORE_DOMAIN_STOP_AND_PATTERN_MODELS

Pass: PASS 05 — CORE_DOMAIN_STOP_AND_PATTERN_MODELS  
Type: pure Kotlin core-domain implementation only

## Objective

Create the first canonical ANDROBUSS core-domain models for stop/group identity, route/pattern ordering, trip/service identity basics, and data confidence without adding parser, routing, Room, Android, or UI logic.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `9cc5b74a9d8244851f03f71b643071baea0907d0`
- working tree at pass start: clean

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/CITY_ADAPTERS.md`
- `docs/TESTING_STRATEGY.md`
- `docs/ROADMAP.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`
- `core-domain/build.gradle.kts`
- existing `core-domain` package/file state (no existing source package before this pass)

## Domain Models Added

Added package:

- `ee.fuuduu.androbuss.core.domain`

Added files:

- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/TransitIds.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/GeoPoint.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/DataConfidence.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/StopGroup.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/StopPoint.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/RouteLine.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/PatternStop.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/RoutePattern.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/ServiceModels.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/Trip.kt`

Model coverage:

- ID wrappers: `StopGroupId`, `StopPointId`, `RouteLineId`, `RoutePatternId`, `TripId`, `ServiceId`, `AgencyId`, `FeedId`, `CityId`
- Core models: `GeoPoint`, `StopGroup`, `StopPoint`, `RouteLine`, `PatternStop`, `RoutePattern`, `ServiceRef`, `Trip`
- Data confidence enum: `DataConfidence` (`STATIC`, `FORECAST`, `REALTIME`)

## Invariants Locked

- ID value classes reject blank IDs.
- `GeoPoint` enforces latitude in `[-90.0, 90.0]` and longitude in `[-180.0, 180.0]`.
- Display names reject blank values for:
  - `StopGroup`
  - `StopPoint`
  - `RouteLine`
  - `RoutePattern`
- `PatternStop.sequence` must be positive.
- `RoutePattern` requires at least two stops.
- `RoutePattern` enforces strictly increasing sequence order.
- `RoutePattern` preserves list order and allows repeated `StopPointId` values (no uniqueness enforcement on stop point identity).
- StopGroup vs StopPoint routing/display distinction is explicit in separate model types.

## Tests Added

- No test source files added in this pass.

Reason:

- `:core-domain:testCompileClasspath` currently contains only `org.jetbrains.kotlin:kotlin-stdlib`.
- No JUnit/kotlin-test dependency exists in module configuration.
- Per pass rules, test infra changes were not made in this pass.

## Validation Result

Commands run:

1. `.\\gradlew.bat :core-domain:build`  
   Result: `BUILD SUCCESSFUL`
2. `.\\gradlew.bat :core-domain:test`  
   Result: `BUILD SUCCESSFUL` with `:core-domain:test NO-SOURCE`
3. `git diff --check`  
   Result: no diff-check errors
4. `git status --short --untracked-files=all`  
   Result: only expected pass files changed

## No Android Dependency Confirmation

- No `android.*` imports added.
- No `Context` usage.
- `core-domain` remains pure Kotlin/JVM.

## No Parser / Routing / Room / UI Confirmation

- No GTFS parser implementation added.
- No service calendar resolver implementation added.
- No routing engine implementation added.
- No Room entities/schema/DAO added.
- No Android UI/ViewModel/Compose/map logic added.

## Risks / Unknowns

- Unit tests for model invariants are still missing due absent test framework dependency in `core-domain`.
- PASS 06 should define and lock calendar resolver semantics/tests before parser and routing implementation.

## Recommended PASS 06

`PASS 06 — SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS`

Focus:

- `calendar.txt` semantics
- `calendar_dates.txt` semantics
- `exception_type=1` add service
- `exception_type=2` remove service
- date validity boundaries

## Exact Files Changed

- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/TransitIds.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/GeoPoint.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/DataConfidence.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/StopGroup.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/StopPoint.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/RouteLine.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/PatternStop.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/RoutePattern.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/ServiceModels.kt`
- `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/Trip.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/audit/PASS_05_CORE_DOMAIN_STOP_AND_PATTERN_MODELS.md`
