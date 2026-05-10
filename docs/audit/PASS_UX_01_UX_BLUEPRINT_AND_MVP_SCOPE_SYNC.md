# PASS_UX_01_UX_BLUEPRINT_AND_MVP_SCOPE_SYNC

## Objective

Integrate Gemini UX research into ANDROBUSS planning docs while preserving accepted technical state through PASS 15 and keeping this pass docs-only.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- branch: `main`
- remote: `origin https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at pass start: `5dd2af29fef7cdbd74dfdd0162d2151cf214c440`
- working tree at pass start: clean

## Gemini UX Findings Integrated

Integrated as canonical planning input:

1. Destination-first baseline (`Kuhu soovid minna?`).
2. Home is list-first and map-second.
3. Home MVP contains high-contrast search and 2-4 quick destination actions.
4. Results focus on 1-3 best cards with strong ETA emphasis.
5. Empty states must be empathetic and actionable.
6. Accessibility is an MVP requirement, not polish.
7. MVP exclusions locked (no ticketing/login/realtime map/multimodal clutter).
8. Constrained assumptions preserved (local metadata first, coordinate seed unresolved, live map future-only).

## PASS 15 Context Preserved

Preserved without modification:

- PASS 15 integration-test result remains accepted.
- No production enrichment/wiring added.
- No Room/UI/network/realtime/nearest-stop implementation added.
- PASS 16 remains the next technical pass.

## Docs Changed

- `docs/UX_PRINCIPLES.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/CURRENT_STATE.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/audit/PASS_UX_01_UX_BLUEPRINT_AND_MVP_SCOPE_SYNC.md`

## Accepted UX Principles

- Destination-first query model.
- List-first home.
- Progressive disclosure for route detail.
- Actionable empty states.
- Accessibility-first interaction design.

## Modified / Constrained Ideas

- External POI API use remains constrained and deferred by default.
- Current location remains a coordinate seed, not a resolved stop.
- Map remains optional helper and must not become default first interaction.

## Rejected / Deferred MVP Ideas

Deferred from MVP:

- ticketing/payment/accounts
- mandatory login
- realtime live vehicle map
- multimodal integration (scooters/taxis/rideshare)
- heavy/3D map-first UX
- dense filter-heavy timetable-first UI

## No Source-Code Confirmation

- No `app/`, `core-*`, `data-*`, `feature-*`, or `city-adapters/` code touched.
- No Gradle/build/CI changes.
- No tests changed.

## No UI/Room/Network/Realtime/Map Implementation Confirmation

- No Compose screens or ViewModels added.
- No Room/cache/runtime storage work added.
- No network downloader or external Places API implementation added.
- No realtime implementation added.
- No map logic implementation added.

## Risks / Unknowns

- UX blueprint is defined, but UI behavior is not yet production-wired.
- Candidate enrichment and verified stop-point resolution still need PASS 16 production work.
- Future UI pass must balance compact MVP with city-specific edge-case messaging.

## Recommended Next Technical Pass

`PASS 16 — STOP_CANDIDATE_ENRICHMENT_AND_BRIDGE_WIRING_PRODUCTION`

## Recommended Future UI Pass

`PASS UI-01 — DESTINATION_FIRST_HOME_AND_RESULT_CARDS_IMPLEMENTATION`
