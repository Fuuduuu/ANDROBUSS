# CITY_ADAPTERS

## Wave Plan

- Wave 0: Rakvere.
- Wave 1: Võru, Viljandi, Pärnu, Kuressaare.
- Wave 2: Narva, Kohtla-Järve, Sillamäe.
- Later: Haapsalu, Paide.

## Future-Only Adapters

- Tallinn and Tartu are future adapters and not initial priorities.

## PASS 04 City/Feed Mapping Baseline (2026-05-09)

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

## CityFeedMapping Metadata (Conceptual, Docs-Only)

Required fields for future `CityFeedMapping` / `CitySourceMapping`:

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
- `stopPointPrecisionRequired` (must be true for ANDROBUSS)
- `preferredFixtureScope`
- `notes`
- `lastVerifiedAt`
- `hashFromDiscoveryPass`
- `legalStatus`

## Adapter Strategy Rules

- Keep transit core city-agnostic.
- Keep mapping metadata explicit and versionable.
- Avoid city inference from `stops.txt` row count alone because many feeds carry broad stop tables.
- Use route/authority/service context for city validity.
- Realtime remains optional per city adapter.
