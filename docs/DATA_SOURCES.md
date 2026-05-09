# DATA_SOURCES

## Authority

- Official source authority: Regionaal- ja Põllumajandusministeerium / Ühistranspordiregistri avaandmed.
- Authority page reference:
  - `https://www.agri.ee/regionaalareng-uhistransport/uhistransport-ja-reisimine/uhistranspordiregistri-avaandmed`

## PASS 03 Discovery Verdict (2026-05-09)

- Legacy `https://peatus.ee/gtfs/gtfs.zip` no longer behaves as a direct GTFS ZIP source.
- Operationally live GTFS ZIP hosting was verified from direct `https://eu-gtfs.remix.com/...` feed object URLs.
- In this repo, `eu-gtfs.remix.com` is treated as verified hosting location, not confirmed ministry-owned domain.

See:
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/CITY_ADAPTERS.md` for city/feed mapping baseline.

## Verified Feed Pattern

- Unified feed exists: `estonia_unified_gtfs.zip`.
- Split feeds exist by county/city/transport scope.
- Multi-feed ingestion is required for ANDROBUSS.

## Canonical Base Policy

- Static GTFS remains canonical base.
- Canonical base should be treated as a source set (unified + split feed options), not one guaranteed single file.

## Legal Status

- License/legal confirmation from official authority text is still unresolved in repository docs.
- Current tracking status: `UNCLEAR` / `THIRD_PARTY_INDEXED_CC0_SIGNAL`.

## Governance Rules

- No private or unauthorized feeds.
- Feed provenance metadata must be tracked (URL, timestamp, size, hash, content-type/status evidence).
- Realtime remains optional and adapter-specific.
- Synthetic test fixtures are allowed; full live feed dumps are not normal unit-test assets.
