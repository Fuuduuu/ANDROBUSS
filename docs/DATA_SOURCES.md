# DATA_SOURCES

## Authority

- Official source authority: Regionaal- ja Põllumajandusministeerium / Ühistranspordiregistri avaandmed.
- Authority page reference:
  - `https://www.agri.ee/regionaalareng-uhistransport/uhistransport-ja-reisimine/uhistranspordiregistri-avaandmed`

## PASS 03 Discovery Verdict (2026-05-09)

- Legacy `https://peatus.ee/gtfs/gtfs.zip` no longer behaves as a direct GTFS ZIP source for this pass.
  - Redirect chain observed: `www.peatus.ee/gtfs/gtfs.zip` -> `peatus.ee/gtfs/gtfs.zip` -> `/reitti/gtfs/gtfs.zip`
  - Final response in checks: HTML closure page (`See rakendus on suletud.`), not ZIP.
- Live downloadable GTFS ZIP feeds were verified from `https://eu-gtfs.remix.com/` direct object URLs.
  - This repo treats `eu-gtfs.remix.com` as operationally verified hosting, not as an officially confirmed ministry domain in this pass.

## Verified Feed Pattern

- Unified feed exists:
  - `estonia_unified_gtfs.zip`
- Split feeds exist (city/county/transport scope), for example:
  - `rakvere.zip`, `parnu.zip`, `narva.zip`, `tallinn.zip`, `tartu.zip`
  - `laane_virumaa.zip`, `vorumaa.zip`, `viljandimaa.zip`, `parnumaa.zip`, `saaremaa.zip`, `ida_viru.zip`
  - `kaugliinid.zip`, `elron.zip`

## Canonical Base Policy

- Static GTFS remains canonical base.
- In implementation planning, canonical should be treated as a source set (unified + split options) rather than a single immutable file.

## License/Legal Status

- Legal license confirmation from official authority text is not finalized in PASS 03.
- Current status:
  - `UNCLEAR` / `THIRD_PARTY_INDEXED_CC0_SIGNAL` only
  - Not legally confirmed in this repository yet

## Data Governance Rules

- No private, scraped, or unauthorized feeds.
- Every ingested feed must store:
  - source URL
  - retrieval timestamp
  - file size
  - SHA256
  - content-type/status snapshot
- Realtime remains optional and city-specific.
