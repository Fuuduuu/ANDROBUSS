# PASS_17_RAKVERE_REAL_GTFS_STOP_NAME_DISCOVERY_AND_METADATA_SPEC

## Objective

Inspect real `rakvere.zip` `stops.txt` and apply only conservative, evidence-backed Rakvere POI `preferredStopGroupNames` metadata updates without adding runtime behavior.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `b8da7f39dbd0b1bb6fb8f9675b2f39d44f36a681`
- working tree at pass start: clean

## Files Read

- `README.md`
- `AGENTS.md`
- `.gitignore`
- `settings.gradle.kts`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/ROADMAP.md`
- `docs/DATA_SOURCES.md`
- `docs/CITY_ADAPTERS.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/audit/PASS_09_RAKVERE_CITY_ADAPTER_METADATA.md`
- `docs/audit/PASS_14_STOPPOINT_RESOLUTION_CONTRACT_AND_NAME_INDEX.md`
- `docs/audit/PASS_15_STOPPOINT_RESOLUTION_INTEGRATION_AND_BRIDGE_WIRING.md`
- `docs/audit/PASS_16_STOP_CANDIDATE_ENRICHMENT_PRODUCTION.md`
- `docs/audit/PASS_16B_ENRICHMENT_DOCS_AND_DIAGRAMS_SYNC.md`
- `city-adapters/build.gradle.kts`
- `city-adapters/src/main/kotlin/**`
- `city-adapters/src/test/kotlin/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/**`

## Guardrail/Build Cleanup

- `.gitignore`: `*.zip` guardrail was already present; no change needed.
- `city-adapters/build.gradle.kts`: removed unused `implementation(project(":core-gtfs"))`.

## GTFS Download and Temp Paths

- Download URL: `https://eu-gtfs.remix.com/rakvere.zip`
- Temp folder used: `C:\Users\Kasutaja\AppData\Local\Temp\androbuss-pass17-20260510-135842`
- Downloaded ZIP path: `C:\Users\Kasutaja\AppData\Local\Temp\androbuss-pass17-20260510-135842\rakvere.zip`
- Extracted `stops.txt` path: `C:\Users\Kasutaja\AppData\Local\Temp\androbuss-pass17-20260510-135842\extract\stops.txt`

## SHA256 Comparison

- PASS 03 documented `rakvere.zip` SHA256: `b55d52a9994890dcd31574c47359f512985c214e78eab401d8b2b34dac950596`
- PASS 17 downloaded SHA256: `b55d52a9994890dcd31574c47359f512985c214e78eab401d8b2b34dac950596`
- SHA status: MATCH

## stops.txt Inspection Method

- Parsed `stops.txt` with PowerShell `Import-Csv` (proper CSV parser path for this pass).
- Queried case-insensitive exact/partial terms for each target POI.
- Collected exact `stop_name` values from real `rakvere.zip` only.
- `stops.txt` row count observed: `1215` (consistent with PASS 03 table context).

## POI Discovery Table

| POI | searched terms | matching `stop_name` values | decision | confidence | notes |
| --- | --- | --- | --- | --- | --- |
| Rakvere bussijaam | `rakvere bussijaam`, `bussijaam` | `Rakvere bussijaam`, `Bussijaam` | mapped to `Rakvere bussijaam` only | VERIFIED_STOP_NAME | exact city-specific name exists; generic `Bussijaam` left unused |
| Rakvere raudteejaam | `rakvere raudteejaam`, `raudtee` | `Raudteejaam`, `Viru-Kabala raudteejaam` | unresolved | UNCLEAR | no exact `Rakvere raudteejaam`; generic name ambiguous |
| Kesklinn | `kesklinn` | none | unresolved | NOT_FOUND | no direct match found |
| Rakvere haigla | `rakvere haigla`, `haigla` | `Haigla` | unresolved | PARTIAL_MATCH | generic stop name, city-specific mapping not explicit |
| Polikliinik | `polikliinik` | `Polikliinik` | mapped to `Polikliinik` | VERIFIED_STOP_NAME | exact match |
| Rakvere teater | `rakvere teater`, `teater` | `Teater` | unresolved | PARTIAL_MATCH | generic stop name; no exact city-specific label |
| Põhjakeskus | `põhjakeskus`, `pohjakeskus` | `Põhjakeskus` | mapped to `Põhjakeskus` | VERIFIED_STOP_NAME | exact match |
| Vaala keskus | `vaala` | `Vaala` | unresolved | PARTIAL_MATCH | POI label differs (`Vaala keskus` vs `Vaala`) |
| Aqva | `aqva` | none | unresolved | NOT_FOUND | no direct match found |
| Rakvere linnus | `rakvere linnus`, `linnus` | none | unresolved | NOT_FOUND | no direct match found |
| Vallimägi | `vallimägi`, `vallimagi` | `Vallimäe` | unresolved | PARTIAL_MATCH | close spelling variant, but not exact |
| Spordikeskus | `spordi`, `spordikeskus` | none | unresolved | NOT_FOUND | no direct match found |

## Metadata Changes Made

- Updated `RakvereCityAdapterMetadata` `preferredStopGroupNames` only for exact verified names:
  - `Rakvere bussijaam` -> `["Rakvere bussijaam"]`
  - `Polikliinik` -> `["Polikliinik"]`
  - `Põhjakeskus` -> `["Põhjakeskus"]`
- Added per-place notes to distinguish:
  - verified stop-name mapping in PASS 17
  - unresolved stop-name mapping kept empty in PASS 17
- Kept all coordinates `null` and coordinate confidence `UNKNOWN`.
- No stop IDs were added or derived.
- No legal-status overclaim changes were made.

## Mappings Left Unresolved

Unresolved POIs kept with empty `preferredStopGroupNames`:

- `kesklinn`
- `rakvere-raudteejaam`
- `vaala-keskus`
- `rakvere-haigla`
- `rakvere-teater`
- `aqva`
- `rakvere-linnus`
- `vallimagi`

## Tests Added/Updated

Updated `city-adapters` tests to verify:

1. At least one Rakvere POI has non-empty `preferredStopGroupNames`.
2. Any non-empty preferred stop-group name is in PASS 17 discovered real stop-name set.
3. Uncertain POIs remain unresolved (empty preferred-stop-group list).
4. Existing conservative legal-status, coordinate, registry, and Android-free guards remain enforced.

No `feature-search` test updates were required in PASS 17.

## Validation Result

- `./gradlew.bat :city-adapters:test` -> PASS
- `./gradlew.bat :feature-search:test` -> PASS
- `./gradlew.bat build` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> PASS 17 scoped files only

## Files Changed

- `city-adapters/build.gradle.kts`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/rakvere/RakvereCityAdapterMetadata.kt`
- `city-adapters/src/test/kotlin/ee/androbus/cityadapters/CityAdapterRegistryTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/CITY_ADAPTERS.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_17_RAKVERE_REAL_GTFS_STOP_NAME_DISCOVERY_AND_METADATA_SPEC.md`

## No ZIP/Data Commit Confirmation

- `rakvere.zip` and extracted `stops.txt` stayed in temp folder only.
- No GTFS ZIP or extracted feed files were added to Git.

## StopPointId Anti-Fabrication Confirmation

- No `StopPointId` values were added to metadata.
- No IDs were derived from stop names, POI names, manual text, display labels, or coordinates.

## No UI/Room/Network/Parser/Routing-Change Confirmation

- No UI/Compose/ViewModel changes.
- No Room/DAO/AppDatabase changes.
- No downloader/network/realtime/cache changes.
- No `core-domain`, `core-gtfs`, or `core-routing` source changes.
- No `InMemoryStopPointIndex`, `DirectRouteQueryBridge`, `StopCandidateEnricher`, or `DirectRouteSearch` behavior changes.

## Risks / Unknowns

- Several Rakvere POIs currently have only partial or no direct stop-name evidence.
- Exact `preferredStopGroupNames` do not solve multi-stop-point selection by direction/platform.
- Production wiring for enrichment/bridge flow is still pending.

## Recommended PASS 18

`PASS 18 — VERIFIED_STOPPOINT_SELECTION_AND_BRIDGE_WIRING_STRATEGY`
