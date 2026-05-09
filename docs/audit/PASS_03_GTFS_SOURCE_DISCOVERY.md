# PASS_03_GTFS_SOURCE_DISCOVERY

Pass: PASS 03 — GTFS_SOURCE_DISCOVERY  
Type: docs/audit/data-discovery

## Objective

Document the current Estonian GTFS source situation for ANDROBUSS without adding runtime code.

## Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- Branch: `main`
- Remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `290066248eaea010ff3a61c9db0a6659b93e4969`
- Working tree at pass start: clean

## Discovery Timestamp

- Local timezone: `Europe/Tallinn`
- Discovery date: `2026-05-09`

## Exact Temp Folders Used

- `C:\Users\Kasutaja\AppData\Local\Temp\ANDROBUSS_PASS03_20260509_205411` (HTML/source crawling snapshots)
- `C:\Users\Kasutaja\AppData\Local\Temp\ANDROBUSS_PASS03_FEEDS_20260509_205931` (GTFS ZIP downloads, hashes, row-count and parser outputs)

## Sources Checked

Primary authority and service pages:

- `https://www.agri.ee/regionaalareng-uhistransport/uhistransport-ja-reisimine/uhistranspordiregistri-avaandmed`
- `https://peatus.ee/content/teenusest`
- `https://www.peatus.ee/gtfs/`
- `https://peatus.ee/gtfs/gtfs.zip`
- `https://peatus.ee/reitti/gtfs/gtfs.zip`

Supporting discovery references (index/crawl aids, not treated as legal authority):

- `https://www.transit.land/feeds/f-ud-rakvere` and related feed pages

## Official Wording Constraint

- Official source authority is **Regionaal- ja Põllumajandusministeerium / Ühistranspordiregistri avaandmed**.
- `eu-gtfs.remix.com` appears to be the **live GTFS hosting location discovered via crawling/reference correlation and verified by live ZIP downloads**.
- This audit does **not** claim `eu-gtfs.remix.com` is the official ministry domain.

## GTFS URL Checks (Every GTFS URL Tested)

Raw machine-readable check file:

- `C:\Users\Kasutaja\AppData\Local\Temp\ANDROBUSS_PASS03_FEEDS_20260509_205931\gtfs_url_checks.csv`

### A) Legacy/Peatus GTFS URL checks

| URL | HEAD status | GET status | Content-Type (GET) | Notes |
| --- | --- | --- | --- | --- |
| `https://www.peatus.ee/gtfs/gtfs.zip` | `301` | `200` | `text/html; charset=utf-8` | Redirect source |
| `https://peatus.ee/gtfs/gtfs.zip` | `302` | `200` | `text/html; charset=utf-8` | Redirects to `/reitti/gtfs/gtfs.zip` |
| `https://peatus.ee/reitti/gtfs/gtfs.zip` | `200` | `200` | `text/html; charset=utf-8` | Returns HTML page, not ZIP |
| `https://peatus.ee/reitti/gtfs/{candidate}.zip` (all tested county/city/type candidates) | `200` | `200` | `text/html; charset=utf-8` | All tested candidates returned same HTML closure page |

Redirect chain evidence (`curl -I`):

- `https://www.peatus.ee/gtfs/gtfs.zip` -> `301` -> `https://peatus.ee/gtfs/gtfs.zip`
- `https://peatus.ee/gtfs/gtfs.zip` -> `302` -> `/reitti/gtfs/gtfs.zip`

`/reitti/gtfs/gtfs.zip` body title/content confirms closure page (`"See rakendus on suletud."`).

Peatus `/reitti/gtfs/` candidate names tested:

- `gtfs.zip`, `estonia.zip`, `all.zip`, `national.zip`, `feed.zip`, `feeds.zip`
- `harjumaa.zip`, `hiiumaa.zip`, `ida-virumaa.zip`, `idavirumaa.zip`, `jarvamaa.zip`, `jogevamaa.zip`, `laanemaa.zip`, `laane-virumaa.zip`, `laanevirumaa.zip`, `parnumaa.zip`, `polvamaa.zip`, `raplamaa.zip`, `saaremaa.zip`, `tartumaa.zip`, `valgamaa.zip`, `viljandimaa.zip`, `vorumaa.zip`
- `rakvere.zip`, `voru.zip`, `viljandi.zip`, `parnu.zip`, `kuressaare.zip`, `narva.zip`, `kohtla-jarve.zip`, `sillamae.zip`
- `urban-lines.zip`, `county-lines.zip`, `train-lines.zip`, `ferry-lines.zip`, `long-distance-lines.zip`

### B) `eu-gtfs.remix.com` URL checks

Observed behavior:

- Root `https://eu-gtfs.remix.com/` -> `403` (no directory listing)
- Multiple direct ZIP URLs return `200` with `application/zip`

`eu-gtfs.remix.com` URLs tested:

- Unified/special: `estonia_unified_gtfs.zip`, `kaugliinid.zip`, `elron.zip`
- Counties/regions tested: `harjumaa.zip`, `hiiumaa.zip`, `jogevamaa.zip`, `jarvamaa.zip`, `laanemaa.zip`, `laane_virumaa.zip`, `parnumaa.zip`, `polvamaa.zip`, `raplamaa.zip`, `saaremaa.zip`, `tartumaa.zip`, `valgamaa.zip`, `viljandimaa.zip`, `vorumaa.zip`, `ida_viru.zip`
- Cities tested: `tallinn.zip`, `tartu.zip`, `parnu.zip`, `narva.zip`, `rakvere.zip`, `kuressaare.zip`, `voru.zip`, `viljandi.zip`
- Variant names tested (403 in checks): `idavirumaa.zip`, `ida_virumaa.zip`, `ida-virumaa.zip`, `laanevirumaa.zip`, `laane-virumaa.zip`, `laaneviru.zip`

## Files Downloaded and Inspected

Downloaded feed summary file:

- `C:\Users\Kasutaja\AppData\Local\Temp\ANDROBUSS_PASS03_FEEDS_20260509_205931\feed_summary.csv`

| Feed | URL | Size (bytes) | SHA256 |
| --- | --- | ---:| --- |
| `estonia_unified_gtfs` | `https://eu-gtfs.remix.com/estonia_unified_gtfs.zip` | 51168593 | `247191fc860991100837466b36263662156a2eb777385f864fe3e007e7794592` |
| `laane_virumaa` | `https://eu-gtfs.remix.com/laane_virumaa.zip` | 2166462 | `b9fb25f8d583910c11baf27f97bbf7a21baea9f3e22c26683e2d1011aae39b8a` |
| `rakvere` | `https://eu-gtfs.remix.com/rakvere.zip` | 96158 | `b55d52a9994890dcd31574c47359f512985c214e78eab401d8b2b34dac950596` |
| `vorumaa` | `https://eu-gtfs.remix.com/vorumaa.zip` | 2962088 | `c396f6d2d7df9fa3d26b7f54511a89591b53c1a10e5ae02f868a8cb462501c4a` |
| `viljandimaa` | `https://eu-gtfs.remix.com/viljandimaa.zip` | 3100487 | `8391a06690090c4dc2124eca090e870c3b178db7adfcd52629d693cd6be71be4` |
| `parnumaa` | `https://eu-gtfs.remix.com/parnumaa.zip` | 3667867 | `45f1f9e0cb7b627c4919ab531ac9a579f6257131f25cb27b8e762e2bd7f32517` |
| `parnu` | `https://eu-gtfs.remix.com/parnu.zip` | 1349506 | `f7c7a14dbb4513d1638e439cd7f4023a11ce79bf2a186aa6668dc2eaac8df536` |
| `saaremaa` | `https://eu-gtfs.remix.com/saaremaa.zip` | 2380222 | `8d668554ceaf69e12ed6068671f87a9708427197d401f919a1565b09a2a51987` |
| `ida_viru` | `https://eu-gtfs.remix.com/ida_viru.zip` | 3264452 | `6c874d9e252624575c237768e9a11ea2418bf65982fd041749f6dd4cd8dc7823` |

## Contained GTFS File Lists

- `estonia_unified_gtfs`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `fare_attributes.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `laane_virumaa`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `rakvere`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `vorumaa`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `viljandimaa`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `parnumaa`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `parnu`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `saaremaa`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`
- `ida_viru`: `agency.txt`, `calendar_dates.txt`, `calendar.txt`, `feed_info.txt`, `routes.txt`, `shapes.txt`, `stop_times.txt`, `stops.txt`, `trips.txt`

Core-file presence check for downloaded feeds:

- `agency.txt`: present in all downloaded feeds
- `stops.txt`: present in all downloaded feeds
- `routes.txt`: present in all downloaded feeds
- `trips.txt`: present in all downloaded feeds
- `stop_times.txt`: present in all downloaded feeds
- `calendar.txt`: present in all downloaded feeds
- `calendar_dates.txt`: present in all downloaded feeds
- `feed_info.txt`: present in all downloaded feeds
- `shapes.txt`: present in all downloaded feeds

## Row Counts Gathered

Raw row-count file:

- `C:\Users\Kasutaja\AppData\Local\Temp\ANDROBUSS_PASS03_FEEDS_20260509_205931\feed_file_row_counts.csv`

Selected counts for analyzed feeds:

| Feed | routes.txt | stops.txt | trips.txt | stop_times.txt | shapes.txt | calendar.txt | calendar_dates.txt | feed_info.txt |
| --- | ---:| ---:| ---:| ---:| ---:| ---:| ---:| ---:|
| `estonia_unified_gtfs` | 2336 | 18157 | 89227 | 2158910 | 2308030 | 2349 | 25693 | 25 |
| `laane_virumaa` | 67 | 18041 | 1589 | 36668 | 82419 | 7 | 2 | 1 |
| `rakvere` | 4 | 1215 | 361 | 6292 | 1037 | 7 | 44 | 1 |
| `vorumaa` | 144 | 18094 | 2700 | 112173 | 107542 | 115 | 1220 | 1 |
| `viljandimaa` | 112 | 18070 | 2317 | 73657 | 147023 | 36 | 523 | 1 |
| `parnumaa` | 113 | 18076 | 3443 | 115238 | 143618 | 54 | 610 | 1 |
| `parnu` | 37 | 18046 | 3274 | 55456 | 15970 | 35 | 190 | 1 |
| `saaremaa` | 132 | 18067 | 1309 | 43863 | 93734 | 99 | 1068 | 1 |
| `ida_viru` | 56 | 18031 | 2631 | 86453 | 142133 | 122 | 1374 | 1 |

## Calendar Range Extraction (Proper CSV Parser)

Parser method:

- `.NET TextFieldParser` with quoted-field handling (`HasFieldsEnclosedInQuotes = true`).
- Replaced earlier naive comma-split parse for calendar safety.

Calendar ranges:

| Feed | `calendar.txt` min start_date | `calendar.txt` max end_date | `calendar_dates.txt` min date | `calendar_dates.txt` max date | exception_type=1 | exception_type=2 |
| --- | --- | --- | --- | --- | ---:| ---:|
| `estonia_unified_gtfs` | 20250101 | 20310430 | 20250101 | 20310429 | 524 | 25169 |
| `laane_virumaa` | 20260501 | 20260614 | 20260501 | 20260501 | 1 | 1 |
| `rakvere` | 20251003 | 20271231 | 20251224 | 20271225 | 22 | 22 |
| `vorumaa` | 20250930 | 20261231 | 20251004 | 20261226 | 24 | 1196 |
| `viljandimaa` | 20250901 | 20261231 | 20251027 | 20261226 | 13 | 510 |
| `parnumaa` | 20251001 | 20270831 | 20260401 | 20270820 | 13 | 597 |
| `parnu` | 20250901 | 20261231 | 20251222 | 20261226 | 12 | 178 |
| `saaremaa` | 20250901 | 20270103 | 20250909 | 20270101 | 49 | 1019 |
| `ida_viru` | 20250901 | 20271231 | 20251020 | 20270831 | 62 | 1312 |

## GTFS Schema/Field Notes

Headers observed include several extension columns beyond strict baseline GTFS fields:

- `stops.txt` extras observed: `alias`, `stop_area`, `authority`, `lest_x`, `lest_y`, `kommentaar`, `kovde_oigusakt`
- `routes.txt` extras observed: `competent_authority`, `route_sort_order`, `contract`
- `trips.txt` extra observed: `trip_headsign_code`
- Unified feed includes additional file `fare_attributes.txt`

## City Coverage Findings (Wave 0 / Wave 1)

Supporting feed-page reference crawl summary:

- `C:\Users\Kasutaja\AppData\Local\Temp\ANDROBUSS_PASS03_FEEDS_20260509_205931\transitland_feed_page_checks.csv`

Key findings from feed page checks:

- `f-ud-rakvere` -> `https://eu-gtfs.remix.com/rakvere.zip`
- `f-ud-vorumaa` -> `https://eu-gtfs.remix.com/vorumaa.zip`
- `f-ud-viljandimaa` -> `https://eu-gtfs.remix.com/viljandimaa.zip`
- `f-ud-parnumaa` -> `https://eu-gtfs.remix.com/parnumaa.zip`
- `f-ud-saaremaa` -> `https://eu-gtfs.remix.com/saaremaa.zip`
- `f-ud-parnu` -> `https://eu-gtfs.remix.com/parnu.zip`
- `f-ud-kuressaare`, `f-ud-voru`, `f-ud-viljandi` -> `NONE_FOUND`

### Coverage table

| Target city | Likely feed(s) | Competent authority signal | Routes | Stops | Trips | Service range signal | Confidence | Notes |
| --- | --- | --- | ---:| ---:| ---:| --- | --- | --- |
| Rakvere (Wave 0) | `rakvere.zip` (primary city), `laane_virumaa.zip` (county context) | `Rakvere linn` (city feed), `Lääne-Virumaa` (county feed) | 4 (city), 67 (county) | 1215 (city), 18041 (county feed includes near-national stop table) | 361 (city), 1589 (county) | City feed calendar 20251003..20271231 | CONFIRMED | Dedicated city feed exists; county feed likely needed for broader county coverage. |
| Võru (Wave 1) | `vorumaa.zip` | `Võrumaa` | 144 | 18094 | 2700 | 20250930..20261231 | CONFIRMED | No dedicated `voru.zip` resolved; county feed appears canonical. |
| Viljandi (Wave 1) | `viljandimaa.zip` | `Viljandimaa` | 112 | 18070 | 2317 | 20250901..20261231 | CONFIRMED | No dedicated `viljandi.zip` resolved; county feed appears canonical. |
| Pärnu (Wave 1) | `parnu.zip` (city), `parnumaa.zip` (county context) | `Pärnu linn` (city), `Pärnumaa` (county) | 37 (city), 113 (county) | 18046 (city feed includes broad stop table), 18076 (county) | 3274 (city), 3443 (county) | City feed calendar 20250901..20261231 | CONFIRMED | Both city and county feeds resolved. |
| Kuressaare (Wave 1) | `saaremaa.zip` | `Saaremaa` | 132 | 18067 | 1309 | 20250901..20270103 | PARTIAL | No dedicated `kuressaare.zip` resolved; county feed includes Kuressaare area. |

## Architecture Implications

1. Multi-feed reality is confirmed.

- Data is not only a single bundle in practice; both unified and region/city split feeds are reachable.

2. Feed selection layer is required.

- City adapters should carry explicit source mapping metadata (`city -> primary feed + optional supplemental feed(s)`).

3. Stops table scope caution.

- Many regional/city feeds include broad national stop tables; route-level/authority-level filtering is required when mapping city coverage.

4. Offline-first cache strategy should be feed-scoped.

- Prefer per-feed cached snapshots (with hash/version tracking) over assuming one immutable national file.

5. Source governance should be explicit.

- Official ministry authority pages should remain canonical policy reference.
- Hosting endpoint (`eu-gtfs.remix.com`) should be treated as operationally verified but policy/legal metadata remains separately validated.

## Risks / Unknowns

- Legal license confirmation is not explicitly extracted from official authority text in this pass.
  - Status: `UNCLEAR` / `THIRD_PARTY_INDEXED_CC0_SIGNAL` only.
- `eu-gtfs.remix.com` is operationally live but not confirmed in this audit as an official ministry-owned domain.
- Feed content appears mutable over time; Content-Length and hash can drift between checks.
- Some city-named feeds (`kuressaare.zip`, `voru.zip`, `viljandi.zip`) were not directly resolvable despite county feeds being present.

## Recommended PASS 04

`PASS 04 — GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING`

## Exact Files Changed (Repository)

- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`

## Validation Result (No Runtime Changes)

- Runtime/build/source modules not modified in this pass section.
- This audit section is docs-only.
