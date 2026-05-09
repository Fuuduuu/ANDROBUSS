# CITY_ADAPTERS

## Wave Plan

- Wave 0: Rakvere.
- Wave 1: Võru, Viljandi, Pärnu, Kuressaare.
- Wave 2: Narva, Kohtla-Järve, Sillamäe.
- Later: Haapsalu, Paide.

## Future-Only Adapters

- Tallinn and Tartu are future-only and out of initial implementation scope.

## Current Runtime Status

- `city-adapters` module exists as skeleton only.
- No city adapter runtime implementation has been added yet.

## PASS 04 City/Feed Mapping Baseline (2026-05-09)

| City | Wave | Primary feed | Secondary/context feed | Mapping confidence | Notes |
| --- | --- | --- | --- | --- | --- |
| Rakvere | Wave 0 | `rakvere.zip` | `laane_virumaa.zip` | CONFIRMED | Dedicated city feed plus county context. |
| Võru | Wave 1 | `vorumaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | CONFIRMED | No dedicated `voru.zip` resolved in PASS 03. |
| Viljandi | Wave 1 | `viljandimaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | CONFIRMED | No dedicated `viljandi.zip` resolved in PASS 03. |
| Pärnu | Wave 1 | `parnu.zip` | `parnumaa.zip` | CONFIRMED | City and county feeds both resolved. |
| Kuressaare | Wave 1 | `saaremaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | No dedicated `kuressaare.zip` resolved in PASS 03. |
| Narva | Wave 2 | `narva.zip` | `ida_viru.zip` | PARTIAL | URL reachable in PASS 03, not deeply profiled yet. |
| Kohtla-Järve | Wave 2 | `ida_viru.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | City ZIP unresolved; county context available. |
| Sillamäe | Wave 2 | `ida_viru.zip` | `estonia_unified_gtfs.zip` (fallback only) | PARTIAL | County-level authority signals in PASS 03. |
| Haapsalu | Later | `laanemaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | UNCLEAR | Not yet city-validated. |
| Paide | Later | `jarvamaa.zip` | `estonia_unified_gtfs.zip` (fallback only) | UNCLEAR | Not yet city-validated. |
| Tallinn | Future-only | `tallinn.zip` | `harjumaa.zip` | PARTIAL | Future adapter only. |
| Tartu | Future-only | `tartu.zip` | `tartumaa.zip` | PARTIAL | Future adapter only. |

## Next Adapter Pass Scope

`PASS 09 — RAKVERE_CITY_ADAPTER_METADATA` should add metadata only:
- aliases,
- POI/source mapping rules,
- feed confidence and mapping fields.

PASS 09 should not add:
- UI,
- routing rewrites,
- network sync implementation,
- realtime logic.
