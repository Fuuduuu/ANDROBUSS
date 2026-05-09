# PASS_06_SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS

## Objective

Add minimal pure Kotlin test infrastructure to `core-domain`, add executable tests for PASS 05 invariants, and implement/test `ServiceCalendarResolver` semantics for GTFS-style `calendar` plus `calendar_dates` behavior.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean before changes
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `0583af1c1332d792568135682b3b8e8135478b05`
- `git log --oneline -7`: PASS history verified

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
- `docs/audit/PASS_05_CORE_DOMAIN_STOP_AND_PATTERN_MODELS.md`
- `docs/audit/PASS_05B_DOMAIN_NAMESPACE_AND_GUARDRAIL_CLEANUP.md`
- all files under `core-domain/src/main/kotlin/ee/androbus/core/domain`
- `core-domain/build.gradle.kts`
- `.github/workflows/ci.yml`

## Test Infra Added

- `core-domain/build.gradle.kts` now declares:
  - `testImplementation(kotlin("test"))`
- CI file was not changed because existing `./gradlew build` already executes module tests.

## PASS 05 Invariant Tests Added

- Added `core-domain/src/test/kotlin/ee/androbus/core/domain/DomainInvariantsTest.kt`.
- Coverage includes:
  - blank ID rejection (`StopGroupId`, `StopPointId`, `RouteLineId`, `RoutePatternId`)
  - blank displayName rejection (`StopGroup`, `StopPoint`, `RouteLine`, `RoutePattern`)
  - invalid `GeoPoint` lat/lon rejection
  - invalid `PatternStop.sequence` rejection
  - `RoutePattern` minimum stop count + strict sequence ordering
  - ordered stop preservation
  - duplicate `StopPointId` support for loop/circular compatibility
  - `Trip` blank headsign rejection when provided
  - `DataConfidence` enum value set

## Calendar Models Added

- `core-domain/src/main/kotlin/ee/androbus/core/domain/ServiceCalendar.kt`
  - `serviceId`, `activeDays`, `startDate`, `endDate`
  - rejects empty `activeDays`
  - rejects `startDate > endDate`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/ServiceCalendarException.kt`
  - `serviceId`, `date`, `exceptionType`
  - `ServiceExceptionType`: `ADD_SERVICE`, `REMOVE_SERVICE`

## Resolver Semantics

- Added `core-domain/src/main/kotlin/ee/androbus/core/domain/ServiceCalendarResolver.kt`.
- API:
  - `fun isServiceActive(serviceId: ServiceId, date: LocalDate): Boolean`
- Behavior:
  - exception rows override base calendar rows
  - `ADD_SERVICE` => active even without base calendar
  - `REMOVE_SERVICE` => inactive even if base calendar would be active
  - no base calendar and no add exception => inactive
  - outside base date range => inactive unless exact-date add exception exists
  - all decisions use explicit caller-provided `LocalDate`; resolver does not compute current date/time internally

## Duplicate Exception Policy

- Duplicate exceptions for the same `(serviceId, date)` are rejected at resolver construction.
- Duplicate base calendars for the same `serviceId` are also rejected for deterministic behavior.

## Validation Result

Validation commands and results:

- Java setup in shell:
  - `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`
  - `java -version` -> `openjdk version "17.0.19" 2026-04-21` (Temurin 17.0.19+10)
- `.\gradlew.bat :core-domain:test` -> **BUILD SUCCESSFUL**
- `.\gradlew.bat :core-domain:build` -> **BUILD SUCCESSFUL**
- `.\gradlew.bat build` -> **BUILD SUCCESSFUL**
- `git diff --check` -> no patch-format errors
- `git status --short --untracked-files=all` -> expected PASS 06 file changes only
- Note: full build emits non-blocking AGP warning (`compileSdk 35` with AGP `8.5.2`).

## Files Changed

- `core-domain/build.gradle.kts`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/ServiceCalendar.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/ServiceCalendarException.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/ServiceCalendarResolver.kt`
- `core-domain/src/test/kotlin/ee/androbus/core/domain/DomainInvariantsTest.kt`
- `core-domain/src/test/kotlin/ee/androbus/core/domain/ServiceCalendarResolverTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/TRUTH_INDEX.md`
- `docs/audit/PASS_06_SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS.md`

## No Runtime Scope Creep Confirmation

- No Android dependency added to `core-domain`.
- No GTFS parser implementation.
- No routing engine implementation.
- No Room schema/entities/DAOs.
- No UI/ViewModel/Compose logic.
- No city adapter implementation.

## Risks / Unknowns

- GTFS parser mapping from raw `exception_type` integers to domain enum is still pending a parser pass.
- Synthetic fixture strategy for calendar edge cases still depends on PASS 07 acceptance.

## Recommended PASS 07

`PASS 07 — MINIMAL_GTFS_FIXTURE_PARSER`
