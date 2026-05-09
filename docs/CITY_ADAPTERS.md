# CITY_ADAPTERS

## Wave Plan

- Wave 0: Rakvere.
- Wave 1: Võru, Viljandi, Pärnu, Kuressaare.
- Wave 2: Ida-Viru cluster.

## Future-Only Adapters

- Tallinn and Tartu are future adapters and not initial priorities.

## PASS 03 Feed Mapping Snapshot (2026-05-09)

| City | Primary feed candidate(s) | Signal | Confidence |
| --- | --- | --- | --- |
| Rakvere | `rakvere.zip` (+ optional `laane_virumaa.zip`) | Route competent authority: `Rakvere linn` in city feed; `Lääne-Virumaa` in county feed | CONFIRMED |
| Võru | `vorumaa.zip` | Route competent authority: `Võrumaa`; no `voru.zip` resolved in checks | CONFIRMED |
| Viljandi | `viljandimaa.zip` | Route competent authority: `Viljandimaa`; no `viljandi.zip` resolved in checks | CONFIRMED |
| Pärnu | `parnu.zip` (+ optional `parnumaa.zip`) | Route competent authority: `Pärnu linn` in city feed; `Pärnumaa` in county feed | CONFIRMED |
| Kuressaare | `saaremaa.zip` | County feed signal; direct `kuressaare.zip` not resolved in checks | PARTIAL |

## Adapter Strategy

- Keep transit core city-agnostic.
- Keep city adapter metadata explicit:
  - `primary_feed`
  - `supplemental_feeds`
  - expected competent authority tags
  - confidence level
- Avoid city inference based only on `stops.txt` counts, because many split feeds still include a broad stop table.
- Realtime remains optional per city adapter.

