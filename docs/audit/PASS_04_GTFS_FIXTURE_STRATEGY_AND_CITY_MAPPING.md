# PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING

Pass: PASS 04 — GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING  
Type: docs/planning/audit only

## Objective

Define GTFS fixture strategy and city/feed mapping for ANDROBUSS before any parser, Room schema, routing engine, Android UI, or runtime implementation.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean at gate
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- `git rev-parse HEAD`: `298bb75544147b73c365579876d6ff2013aed227`

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
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/sources/ANNIVIBE_BUS_MODULE_SOURCE.md` (missing)
- `docs/sources/ANDROBUSS_PROJECT_WORKING_MEMORY.md` (missing)

## Source From PASS 03 Used

PASS 04 planning is based on PASS 03 findings:

- Official authority remains Regionaal- ja Põllumajandusministeerium / Ühistranspordiregistri avaandmed.
- `eu-gtfs.remix.com` is operationally live for GTFS ZIP downloads.
- Legacy `peatus.ee/gtfs/gtfs.zip` is not a live ZIP endpoint in observed checks.
- Multi-feed ingestion is required.
- Wave 0/1 source signals:
  - Rakvere: `rakvere.zip` (+ county context `laane_virumaa.zip`)
  - Võru: `vorumaa.zip` (no `voru.zip` resolved)
  - Viljandi: `viljandimaa.zip` (no `viljandi.zip` resolved)
  - Pärnu: `parnu.zip` + `parnumaa.zip`
  - Kuressaare: `saaremaa.zip` (city-level direct ZIP unresolved)

## City/Feed Mapping Table (PASS 04 Baseline)

| City | Wave | Primary feed | Secondary/context feed | Mapping confidence | Notes |
| --- | --- | --- | --- | --- | --- |
| Rakvere | Wave 0 | `rakvere.zip` | `laane_virumaa.zip` | CONFIRMED | Dedicated city feed plus county context. |
| Võru | Wave 1 | `vorumaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | CONFIRMED | No dedicated `voru.zip` resolved in PASS 03. |
| Viljandi | Wave 1 | `viljandimaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | CONFIRMED | No dedicated `viljandi.zip` resolved in PASS 03. |
| Pärnu | Wave 1 | `parnu.zip` | `parnumaa.zip` | CONFIRMED | City and county feeds both resolved. |
| Kuressaare | Wave 1 | `saaremaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | No dedicated `kuressaare.zip` resolved in PASS 03. |
| Narva | Wave 2 | `narva.zip` | `ida_viru.zip` | PARTIAL | URL reachable in PASS 03, but not deeply profiled yet. |
| Kohtla-Järve | Wave 2 | `ida_viru.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | City-specific ZIP unresolved; county feed carries Ida-Viru context. |
| Sillamäe | Wave 2 | `ida_viru.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | PASS 03 authority signal found in `ida_viru` route metadata. |
| Haapsalu | Later | `laanemaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | UNCLEAR | URL pattern reachable; city-level mapping not verified yet. |
| Paide | Later | `jarvamaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | UNCLEAR | URL pattern reachable; city-level mapping not verified yet. |
| Tallinn | Future-only | `tallinn.zip` | `harjumaa.zip` | PARTIAL | Future adapter only, not initial scope. |
| Tartu | Future-only | `tartu.zip` | `tartumaa.zip` | PARTIAL | Future adapter only, not initial scope. |

## CityFeedMapping Metadata Proposal (Conceptual Only)

Proposed future metadata fields:

- `cityId`
- `displayName`
- `wave`
- `primaryFeedId`
- `secondaryFeedIds`
- `sourceUrl`
- `sourceAuthority`
- `hostingUrl`
- `feedScope` (`CITY`, `COUNTY`, `REGION`, `NATIONAL`, `MIXED`)
- `mappingConfidence` (`CONFIRMED`, `PARTIAL`, `UNCLEAR`, `NOT_FOUND`)
- `stopPointPrecisionRequired`
- `preferredFixtureScope`
- `notes`
- `lastVerifiedAt`
- `hashFromDiscoveryPass`
- `legalStatus`

## Fixture Strategy

A. Smoke fixture:

- Tiny, one-city scope.
- One or two lines.
- Validates mandatory parser input files can be read.

B. StopPoint precision fixture:

- Must include same-name/opposite-direction stop case where possible.
- Protects StopGroup vs StopPoint separation.

C. Direct-route fixture:

- Must include valid ordered pattern (`destinationIndex > originIndex`).
- Must include invalid reverse-direction case.

D. Calendar fixture:

- Must include `calendar.txt` and `calendar_dates.txt`.
- Must include add/remove exception behavior (`exception_type=1` and `2`) from real data or synthetic later fixture.

E. City-mapping fixture:

- Proves city maps to correct feed(s).
- Must avoid parsing full Estonia in normal unit tests.

## Fixture Anti-Goals

Do not include yet:

- Whole Estonia unified feed as normal unit-test fixture.
- Full multi-city GTFS dumps in repository.
- Realtime feeds/payloads.
- Transfers/ticketing/map-tiles fixtures.
- Huge `stop_times` files where smaller deterministic subset is enough.
- Generated Room DB snapshots.

## Proposed Future Fixture Paths (Not Created in PASS 04)

- `test-fixtures/gtfs/rakvere-smoke/`
- `test-fixtures/gtfs/rakvere-stop-point-precision/`
- `test-fixtures/gtfs/rakvere-calendar/`
- `test-fixtures/gtfs/parnu-city-county-mapping/`

## Fixture Storage Policy

- Do not commit large raw GTFS ZIPs.
- Keep downloaded real feeds in temp/local/cache only.
- Keep fixtures tiny and deterministic.
- If fixture generation tooling is needed, schedule a dedicated pass.
- Record source URLs and hashes in docs/audit for traceability.

## Parser Preconditions (Before Parser Implementation)

Minimum first-support files:

- Mandatory: `agency.txt`, `stops.txt`, `routes.txt`, `trips.txt`, `stop_times.txt`
- Service calendar requirement: at least one of `calendar.txt` or `calendar_dates.txt`
- Optional initially: `shapes.txt`, `feed_info.txt`, `fare_attributes.txt`

Expected behavior:

- Missing mandatory file: fail ingest with structured error.
- Missing optional file: continue with warning metadata.
- Missing both calendar files: fail ingest.

Tracking requirements:

- Track source URL, source authority, retrieval timestamp, size, SHA256, and row counts per feed.

Calendar priority requirement:

- `calendar_dates` override behavior must be tested against base `calendar`.

Stop precision requirement:

- Preserve StopGroup vs StopPoint separation.
- Do not collapse same-name opposite-direction stop points.

## Risks / Unknowns

- Legal license confirmation for live hosting remains unresolved (`UNCLEAR` / `THIRD_PARTY_INDEXED_CC0_SIGNAL`).
- Several later/future city mappings are still URL-level or authority-level signals, not deep profile validations.
- Feed content is mutable over time; mapping confidence can drift without periodic re-verification.

## Recommended PASS 05

`PASS 05 — CORE_DOMAIN_STOP_AND_PATTERN_MODELS`

PASS 05 should implement pure Kotlin core domain models only, with no Android dependency.

## Exact Files Changed

- `docs/CITY_ADAPTERS.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`

## Validation Result

- `git diff -- docs/CITY_ADAPTERS.md docs/GTFS_PIPELINE.md docs/TESTING_STRATEGY.md docs/ROUTING_LOGIC.md docs/CURRENT_STATE.md docs/ROADMAP.md docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`: PASS (only scoped docs paths changed)
- `git diff --check`: PASS (no whitespace/conflict markers detected)
- `git status --short --untracked-files=all`: PASS for expected docs-only dirty state

## No Runtime Change Confirmation

- No changes to `app/`, `core-*`, `data-*`, `feature-*`, `city-adapters/`, Android source, build files, Kotlin runtime code, Room schema, routing implementation, ViewModels, or Compose screens.
