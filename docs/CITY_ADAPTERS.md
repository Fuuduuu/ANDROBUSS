# CITY_ADAPTERS

## Wave Plan

- Wave 0: Rakvere.
- Wave 1: Voru, Viljandi, Parnu, Kuressaare.
- Wave 2: Narva, Kohtla-Jarve, Sillamae.
- Later: Haapsalu, Paide.

## Future-Only Adapters

- Tallinn and Tartu are future-only and out of initial implementation scope.

## Current Runtime Status

- `city-adapters` now contains a pure Kotlin metadata contract and registry.
- Rakvere metadata is implemented as the first active city metadata provider.
- `city-adapters` metadata module depends on `core-domain` only.
- No city adapter runtime integration has been added yet.

## PASS 04 City/Feed Mapping Baseline (2026-05-09)

| City | Wave | Primary feed | Secondary/context feed | Mapping confidence | Notes |
| --- | --- | --- | --- | --- | --- |
| Rakvere | Wave 0 | `rakvere.zip` | `laane_virumaa.zip` | CONFIRMED | Dedicated city feed plus county context. |
| Voru | Wave 1 | `vorumaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | CONFIRMED | No dedicated `voru.zip` resolved in PASS 03. |
| Viljandi | Wave 1 | `viljandimaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | CONFIRMED | No dedicated `viljandi.zip` resolved in PASS 03. |
| Parnu | Wave 1 | `parnu.zip` | `parnumaa.zip` | CONFIRMED | City and county feeds both resolved. |
| Kuressaare | Wave 1 | `saaremaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | No dedicated `kuressaare.zip` resolved in PASS 03. |
| Narva | Wave 2 | `narva.zip` | `ida_viru.zip` | PARTIAL | URL reachable in PASS 03, not deeply profiled yet. |
| Kohtla-Jarve | Wave 2 | `ida_viru.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | City ZIP unresolved; county context available. |
| Sillamae | Wave 2 | `ida_viru.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | County-level authority signals in PASS 03. |
| Haapsalu | Later | `laanemaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | UNCLEAR | Not yet city-validated. |
| Paide | Later | `jarvamaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | UNCLEAR | Not yet city-validated. |
| Tallinn | Future-only | `tallinn.zip` | `harjumaa.zip` | PARTIAL | Future adapter only. |
| Tartu | Future-only | `tartu.zip` | `tartumaa.zip` | PARTIAL | Future adapter only. |

## PASS 09 to PASS 17 Rakvere Metadata State

Implemented metadata includes:

- City identity and wave classification (`rakvere`, `WAVE_0`).
- Aliases for search normalization (`Rakvere`, `rakvere`, `Rakvere linn`).
- Feed mappings:
  - primary: `rakvere.zip` (`feedId=rakvere`, `CITY`, `CONFIRMED`)
  - context: `laane_virumaa.zip` (`feedId=laane_virumaa`, `COUNTY`, `PARTIAL`)
- Conservative legal status:
  - `HOSTING_VERIFIED_LEGAL_UNCLEAR`
  - no overclaim of official-domain/legal certainty
- Seed place list for future destination resolver work:
  - includes city-center, station, healthcare, shopping, and tourism targets
  - coordinates intentionally unset (`null` / `UNKNOWN`) until verified

PASS 17 added conservative `preferredStopGroupNames` only for real `rakvere.zip` `stops.txt` exact matches:

- `Rakvere bussijaam` -> `Rakvere bussijaam`
- `Polikliinik` -> `Polikliinik`
- `Põhjakeskus` -> `Põhjakeskus`

POIs left unresolved in PASS 17 (no confident exact stop-name mapping):

- `Kesklinn`
- `Rakvere raudteejaam`
- `Rakvere haigla`
- `Rakvere teater`
- `Vaala keskus`
- `Aqva`
- `Rakvere linnus`
- `Vallimägi`

## Next Adapter-Related Scope

`PASS 21 — FEED_DOMAIN_SNAPSHOT_AND_ROUTE_PATTERN_PROVIDER_SPEC` is the next infrastructure boundary pass before runtime adapter/provider wiring.

City-adapter runtime integration, UI wiring, realtime, Room, and downloader behavior remain out of scope here.
