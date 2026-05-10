# UX_PRINCIPLES

## Destination-First Home

- Main question and CTA: `Kuhu soovid minna?`
- Home starts with a large, high-contrast destination search field.
- Home includes 2-4 large quick destination buttons.
- Riders must not be forced into stop-first, line-first, or timetable-first flow.
- Nearby departures are secondary context, not the primary task.

## List-First, Map-Second

- Home is list-first, not map-first.
- Map is a secondary helper surface behind actions like `Näita kaardil` or detail view.
- Map is for later support tasks such as walking guidance and opposite-side stop disambiguation.
- MVP must avoid marker clutter and map-heavy cognitive load.

## Route Result Cards

- Initial results should show only 1-3 best options.
- Each card should prioritize:
  - large ETA (`väljub X min pärast`),
  - line number and direction,
  - arrival time and total duration.
- Primary action should be explicit (`Alusta teekonda` or `See sobib`).
- Full stop lists and dense timetable detail belong to progressive disclosure in route detail view.

## Empty and Error States

Use explanatory, action-oriented rider language.

Preferred examples:

- `Otsebussi praegu ei lähe. Näitame lähimaid variante.`
- `Selles suunas täna enam busse ei sõida.`
- `Asukoht pole lubatud - saad alguspunkti ka käsitsi sisestada.`
- `Bussid täna enam ei sõida. Lähim väljumine on homme kell ...`

Avoid:

- `Error`
- `No result`
- blank screens
- raw technical failure text

## Accessibility (MVP Requirement)

- Large text as default-friendly baseline.
- Support system font scaling.
- High contrast; avoid light gray low-contrast text.
- Minimum comfortable touch targets (44dp / ~9x9mm class).
- Few colors and calm spacing.
- Icons must have text labels; avoid mystery icon-only controls.
- Senior-friendly mode is a planned future enhancement.

## MVP Inclusions

- Destination-first home query (`Kuhu soovid minna?`).
- Quick destination shortcuts.
- Simple route result cards (1-3 options).
- Progressive disclosure route detail.
- Manual origin fallback when location is unavailable.

## MVP Exclusions

Out of MVP scope:

- ticketing, payment, accounts
- mandatory login
- realtime live vehicle map
- multimodal clutter (scooters, taxis, rideshare, etc.)
- heavy/3D map experience
- complex filter-heavy timetable UI
- full timetable table on home
- external Places API dependency unless later justified

## Constrained Product Inputs

- External POI APIs (including Google Places) are not MVP default.
- MVP POI coverage should come from local city-adapter metadata and aliases first.
- Current location is an origin coordinate seed until explicit stop-point resolution/nearest-stop logic is implemented.
- Live vehicle map remains future-only until data freshness semantics are proven.

## Current Implementation Alignment (After PASS 15)

- Core routing and candidate pipeline logic exists in pure Kotlin modules.
- No production UI flow is implemented yet.
- No Room cache, network downloader, realtime integration, or nearest-stop geospatial resolution exists yet.
- PASS 16 remains the next technical pass before UI implementation phases.

## Future UI Phases

- Voice search (later).
- Opt-in reminder notifications (later).
- Realtime overlays only after data quality and freshness semantics are proven.
- Deeper map detail/navigation layers (later).
- Additional accessibility enhancements, including senior-friendly mode (later).

## Information Discipline

- Do not expose raw stop IDs in rider-facing UI.
- Do not expose raw coordinate tuples in primary UI.
- Prefer human-readable place and route guidance.
