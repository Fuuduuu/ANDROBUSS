# PASS_10_DESTINATION_TARGET_MODEL_AND_PLACE_RESOLVER_SPEC

## Objective

Create a pure Kotlin destination-target model and metadata-based place resolver in `feature-search`, with executable tests against Rakvere city metadata and no UI/map/nearest-stop/routing changes.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `984b6e180fba357420dd4b50ec22e9210f158760`
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
- `docs/audit/PASS_08B_DOCS_AND_DIAGRAMS_SYNC.md`
- `docs/audit/PASS_09_RAKVERE_CITY_ADAPTER_METADATA.md`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `city-adapters/src/main/kotlin/**`
- `city-adapters/src/test/kotlin/**`
- `feature-search/build.gradle.kts`

## Build/Dependency Changes

In `feature-search/build.gradle.kts`:

- retained `implementation(project(":core-domain"))`
- added `implementation(project(":city-adapters"))`
- added `testImplementation(kotlin("test"))`

No external libraries were added.

## Destination Target Model

Added under `ee.androbus.feature.search.destination`:

- `DestinationTarget`
- `DestinationTargetSource`
- `DestinationTargetConfidence`
- `DestinationMatch`
- `DestinationResolutionResult`
- `DestinationNotFoundReason`

Current source behavior implementation uses only `DestinationTargetSource.CITY_PLACE_METADATA`.

## Query Normalization Policy

`DestinationQueryNormalizer` provides:

- strict normalization: trim + lowercase (Estonian locale)
- flexible normalization: strict normalization + punctuation removal + whitespace collapse

No transliteration or fuzzy library is used.

## Place Resolver Behavior

`DestinationTargetResolver.resolvePlaceQuery(city, query)`:

- blank query -> `NotFound(BLANK_QUERY)`
- no city places -> `NotFound(NO_CITY_PLACES)`
- no matches -> `NotFound(NO_MATCH)`
- match confidence:
  - strict exact -> `EXACT_ALIAS`
  - flexible exact -> `NORMALIZED_ALIAS`
  - contains partial -> `PARTIAL_ALIAS`
- deterministic ordering:
  - confidence rank (`EXACT_ALIAS`, `NORMALIZED_ALIAS`, `PARTIAL_ALIAS`, `UNCLEAR`)
  - then normalized display name alphabetical

Resolver is metadata-based only; no stop lookup, no nearest-stop logic, no routing invocation.

## Confidence Policy

- Resolver confidence is query-match confidence only.
- It does not upgrade source coordinate confidence or infer stop mappings.
- Coordinate and preferred-stop-group metadata pass through unchanged from source place metadata.

## Rakvere Metadata Usage

Tests use `RakvereCityAdapterMetadata.metadata` directly.

Verified cases include:
- `Rakvere bussijaam`
- alias `bussijaam`
- `Vaala keskus`
- `Kesklinn`
- case-insensitive queries
- extra whitespace handling

## Tests Added

- `DestinationQueryNormalizerTest`
- `DestinationTargetResolverTest`

Coverage includes:

- blank query and unknown query behavior
- exact/alias/case-insensitive/normalized matching
- deterministic partial behavior
- source type check (`CITY_PLACE_METADATA`)
- coordinate null/UNKNOWN pass-through
- no nearest-stop/route-search output assumptions
- Android-free reflection guard

## Validation Result

- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat :feature-search:build` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> expected scoped changes only

## Files Changed

- `feature-search/build.gradle.kts`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/DestinationTarget.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/DestinationMatch.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/DestinationQueryNormalizer.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/DestinationTargetResolver.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/destination/DestinationQueryNormalizerTest.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/destination/DestinationTargetResolverTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/TRUTH_INDEX.md`
- `docs/audit/PASS_10_DESTINATION_TARGET_MODEL_AND_PLACE_RESOLVER_SPEC.md`

## No Out-of-Scope Changes Confirmation

- No UI or Compose screens added.
- No Room changes.
- No network downloader/realtime changes.
- No routing engine behavior changes.
- No GTFS parser behavior changes.

## Risks / Unknowns

- Place-to-stop candidate mapping remains unimplemented.
- Resolver confidence is lexical only; no geospatial or service-aware scoring.
- Metadata quality still depends on future city adapter enrichment/verification.

## Recommended PASS 11

`PASS 11 — PLACE_TO_STOP_CANDIDATE_MAPPING_SPEC`
