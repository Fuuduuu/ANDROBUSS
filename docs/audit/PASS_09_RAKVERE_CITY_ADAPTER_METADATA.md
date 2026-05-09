# PASS_09_RAKVERE_CITY_ADAPTER_METADATA

## Objective

Implement the first city adapter metadata layer for Rakvere with executable tests, without adding UI, Room, network, realtime, parser changes, or routing behavior changes.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `31256f0f90a041564ff7e69a2f958c0a60389afb`
- working tree at pass start: clean

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROADMAP.md`
- `docs/DATA_SOURCES.md`
- `docs/CITY_ADAPTERS.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`
- `docs/audit/PASS_08B_DOCS_AND_DIAGRAMS_SYNC.md`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `city-adapters/build.gradle.kts`

## Build/Dependency Changes

- `city-adapters` was converted to a pure Kotlin JVM module.
- Dependencies used:
  - `implementation(project(":core-domain"))`
  - `implementation(project(":core-gtfs"))`
  - `testImplementation(kotlin("test"))`

No external or Android/network libraries were added.

## Metadata Model Added

Added pure Kotlin metadata contract models:

- `CityAdapterMetadata`
- `CityFeedMapping`
- `CityPlaceMetadata`
- enums for:
  - city wave,
  - feed scope,
  - mapping confidence,
  - legal status,
  - place category,
  - coordinate confidence.

Also added registry/provider:

- `CityAdapterRegistry`
- `RakvereCityAdapterMetadata`

## Rakvere Feed Mapping

Implemented two mappings:

1. Primary city mapping:
- `feedId = rakvere`
- `feedName = rakvere.zip`
- `sourceUrl = https://eu-gtfs.remix.com/rakvere.zip`
- `feedScope = CITY`
- `mappingConfidence = CONFIRMED`
- `legalStatus = HOSTING_VERIFIED_LEGAL_UNCLEAR`

2. Context county mapping:
- `feedId = laane_virumaa`
- `feedName = laane_virumaa.zip`
- `sourceUrl = https://eu-gtfs.remix.com/laane_virumaa.zip`
- `feedScope = COUNTY`
- `mappingConfidence = PARTIAL`
- `legalStatus = HOSTING_VERIFIED_LEGAL_UNCLEAR`

Authority string is kept aligned with PASS 03/04 docs wording.

## Rakvere Aliases and POI Seeds

Aliases:
- `Rakvere`
- `rakvere`
- `Rakvere linn`

Seed places include:
- Kesklinn
- Rakvere bussijaam
- Rakvere raudteejaam
- Pohjakeskus
- Vaala keskus
- Rakvere haigla
- Polikliinik
- Rakvere teater
- Aqva
- Rakvere linnus
- Vallimagi

All place coordinates are intentionally `null` with `CoordinateConfidence.UNKNOWN` to avoid false precision.

## Confidence and Legal Policy

- Mapping confidence follows PASS 03/04 evidence (`CONFIRMED` primary city feed, conservative context mapping).
- Legal status remains conservative (`HOSTING_VERIFIED_LEGAL_UNCLEAR`), with no claim that hosting domain is ministry-owned or legally confirmed.

## Tests Added

Added `CityAdapterRegistryTest` coverage for:

- Rakvere metadata existence and identity.
- `WAVE_0` assignment.
- primary/context feed mapping presence.
- conservative legal status.
- Tallinn/Tartu absence from active registry.
- non-empty POI list with conservative coordinate policy.
- alias coverage.
- registry lookup and duplicate city-id protection.
- Android-free API guard.

## Validation Result

- `./gradlew.bat :city-adapters:test` -> PASS
- `./gradlew.bat :city-adapters:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected scoped changes only

## Files Changed

- `city-adapters/build.gradle.kts`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/CityAdapterRegistry.kt`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/metadata/CityAdapterEnums.kt`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/metadata/CityAdapterMetadata.kt`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/metadata/CityFeedMapping.kt`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/metadata/CityPlaceMetadata.kt`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/rakvere/RakvereCityAdapterMetadata.kt`
- `city-adapters/src/test/kotlin/ee/androbus/cityadapters/CityAdapterRegistryTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/CITY_ADAPTERS.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_09_RAKVERE_CITY_ADAPTER_METADATA.md`

## No Out-of-Scope Changes Confirmation

- No UI changes.
- No Room changes.
- No network downloader/realtime changes.
- No routing behavior changes.
- No GTFS parser behavior changes.

## Risks / Unknowns

- Place-to-stop mapping remains unresolved and requires a dedicated resolver spec.
- Legal/license status remains conservative pending direct official confirmation.
- City adapter runtime integration remains future scope.

## Recommended PASS 10

`PASS 10 — DESTINATION_TARGET_MODEL_AND_PLACE_RESOLVER_SPEC`
