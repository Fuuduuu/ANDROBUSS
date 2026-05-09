# PASS_07_MINIMAL_GTFS_FIXTURE_PARSER

## Objective

Implement a minimal pure Kotlin GTFS static fixture parser in `core-gtfs`, plus executable tests, to validate core file parsing/mapping into existing `core-domain` models without adding routing, Room, UI, or network logic.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean before changes
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `502ab5b276d5f4ca37017071c892c99b794fd6d7`
- `git log --oneline -8`: PASS history verified

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/DATA_SOURCES.md`
- `docs/CITY_ADAPTERS.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/ROADMAP.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`
- `docs/audit/PASS_05_CORE_DOMAIN_STOP_AND_PATTERN_MODELS.md`
- `docs/audit/PASS_05B_DOMAIN_NAMESPACE_AND_GUARDRAIL_CLEANUP.md`
- `docs/audit/PASS_06_SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS.md`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/*`
- `core-gtfs/build.gradle.kts`
- existing `core-gtfs` structure (no source/tests before this pass)

## Parser Scope

Implemented under `ee.androbus.core.gtfs`:

- CSV reader with support for:
  - comma-separated fields
  - header row
  - quoted fields
  - escaped quotes (`""`)
  - empty fields
  - CRLF/LF line endings
- GTFS raw model parse for:
  - `agency.txt`
  - `stops.txt`
  - `routes.txt`
  - `trips.txt`
  - `stop_times.txt`
  - `calendar.txt`
  - `calendar_dates.txt`
- Required file validation:
  - mandatory: `agency.txt`, `stops.txt`, `routes.txt`, `trips.txt`, `stop_times.txt`
  - at least one of `calendar.txt` or `calendar_dates.txt` required
- Domain mapper to existing `core-domain`:
  - `StopGroup` / `StopPoint`
  - `RouteLine`
  - `RoutePattern`
  - `Trip`
  - `ServiceCalendar`
  - `ServiceCalendarException`

## Required GTFS Files Supported

- `agency.txt`
- `stops.txt`
- `routes.txt`
- `trips.txt`
- `stop_times.txt`
- `calendar.txt` (optional if `calendar_dates.txt` exists)
- `calendar_dates.txt` (optional if `calendar.txt` exists)

## Optional Files Deliberately Not Parsed

- `feed_info.txt`
- `shapes.txt`
- fares/transfers/realtime files

## Fixture Path and Status

- Fixture path: `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`
- Fixture status: committed and used by parser/mapper tests.

## Why Fixture Is Synthetic and Tiny

- Fixture is hand-curated and deterministic for unit testing.
- It is Rakvere-inspired only and not an authoritative operational timetable.
- No live ZIP download or full national/county dataset is committed in this pass.

## Domain Mapping Rules

- `stop_id` maps to `StopPointId`.
- Safe baseline stop-group mapping is 1:1:
  - `StopGroupId("group:$stopId")`
- `stop_name` maps to display labels only.
- `route_id` maps to `RouteLineId`.
- `trip_id` maps to `TripId`.
- `RoutePattern` baseline:
  - one pattern per trip (`pattern:$trip_id`)
  - ordered by `stop_sequence`
- `calendar_dates.exception_type` mapping:
  - `1` -> `ADD_SERVICE`
  - `2` -> `REMOVE_SERVICE`
  - other values -> explicit parse/mapping error

## StopPoint Identity Protection

- Same `stop_name` with different `stop_id` remains separate `StopPoint` entries.
- Mapper does not group stop points by stop name.
- Routing identity remains `StopPointId`/`stop_id`.

## Tests Added

- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/CsvTableReaderTest.kt`
- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/GtfsFeedParserTest.kt`
- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/GtfsDomainMapperTest.kt`

Coverage includes:

- CSV quoted-comma fields
- CSV escaped quotes
- CRLF/LF handling
- parser loading fixture
- mandatory file failure cases
- calendar file presence rule (`calendar` and/or `calendar_dates`)
- exception type mapping and invalid exception failures
- same-name stop identity separation
- ordered pattern creation
- trip/service reference mapping
- Android-free API guard via reflection checks

## Build/Dependency Changes

- `core-gtfs/build.gradle.kts`:
  - retained: `implementation(project(":core-domain"))`
  - added: `testImplementation(kotlin("test"))`

## Validation Result

Validation commands and results:

- Java setup in shell:
  - `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`
- `.\gradlew.bat :core-gtfs:test` -> **BUILD SUCCESSFUL**
- `.\gradlew.bat :core-gtfs:build` -> **BUILD SUCCESSFUL**
- `.\gradlew.bat build` -> **BUILD SUCCESSFUL**
- `git diff --check` -> no patch-format errors
- `git status --short --untracked-files=all` -> expected PASS 07 changes only
- Note: full build emits non-blocking AGP warning (`compileSdk 35` with AGP `8.5.2`).

## Files Changed

- `core-gtfs/build.gradle.kts`
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/GtfsParseException.kt`
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/CsvTableReader.kt`
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/GtfsModels.kt`
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/GtfsFeedParser.kt`
- `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/GtfsDomainMapper.kt`
- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/CsvTableReaderTest.kt`
- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/GtfsFeedParserTest.kt`
- `core-gtfs/src/test/kotlin/ee/androbus/core/gtfs/GtfsDomainMapperTest.kt`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/agency.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/stops.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/routes.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/trips.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/stop_times.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/calendar.txt`
- `core-gtfs/src/test/resources/gtfs/rakvere-smoke/calendar_dates.txt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_07_MINIMAL_GTFS_FIXTURE_PARSER.md`

## Android-Free Confirmation

- No `android.*` imports.
- No `Context` usage.
- `core-gtfs` remains pure Kotlin/JVM.

## No Routing / Room / UI / Network Confirmation

- No direct-route search logic.
- No routing engine implementation.
- No Room schema/entities/DAOs.
- No Android UI/ViewModel/Compose logic.
- No network feed downloader/update client.
- No live ZIP parsing.

## Risks / Unknowns

- This parser is intentionally minimal and not production-normalized for full national feeds.
- No pattern de-duplication across trips yet (one pattern per trip baseline).
- No `shapes.txt`, transfers, fares, or realtime mapping yet.
- City-specific grouping/normalization remains future adapter work.

## Recommended PASS 08

`PASS 08 — DIRECT_ROUTE_SEARCH_CORE`
