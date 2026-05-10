# TRUTH_INDEX

Canonical truths for this repository:

- `StopPointId` is routing identity.
- `StopGroup` is display/search grouping.
- Stop names and display names are never routing identity.
- Same-name stops may map to separate directional/platform stop points.
- `RoutePattern` is an ordered sequence of `PatternStop` values.
- Duplicate `StopPointId` values inside one `RoutePattern` are allowed for loop/circular patterns.
- Direct route validity requires origin and destination in the same `RoutePattern` and destination after origin.
- `calendar_dates` overrides base `calendar` semantics.
- GTFS `exception_type 1` maps to add service.
- GTFS `exception_type 2` maps to remove service.
- `ServiceCalendarResolver` takes explicit `LocalDate` input and must not compute "today" internally.
- Data confidence levels are `STATIC`, `FORECAST`, `REALTIME`.
- Static GTFS is canonical base data.
- Realtime is optional and adapter-specific.
- Destination-first UX remains product direction.
- Map is an input aid, not the routing engine.
- Destination target resolution is metadata-based (`CITY_PLACE_METADATA`) and does not perform nearest-stop/routing/map logic.
- Place-to-stop candidate mapping uses preferred stop-group names as unresolved name-level candidates; it does not fabricate `StopGroupId` or `StopPointId`.
- Origin candidate resolution (manual text or current location) is unresolved seed generation only and must not fabricate `StopPointId`/`StopGroupId`.
- Direct-route bridge must return `NotReady` until explicit origin and destination `StopPointId` candidates exist; names/coordinates must not be converted into `StopPointId`.
- Verified stop-point resolution candidates must be sourced from actual `StopPoint` objects; `stopPointId` must come from `StopPoint.id` only.
- Name-index resolution may return multiple candidates for same-name stops; PASS 14 does not use coordinate hints for matching.
- Integration behavior must preserve `StopPointId` identity: same display name can resolve to different stop points and produce different routing outcomes.
- Wave 0 is Rakvere.
- Wave 1 is Voru, Viljandi, Parnu, Kuressaare.
- Wave 2 is Narva, Kohtla-Jarve, Sillamae.
- Later cities are Haapsalu and Paide.
- Tallinn and Tartu are future-only adapters.
- Ticketing is out of scope without legal/partner basis.

## StopGroup vs StopPoint Semantics

- `StopGroup.displayName` is the rider-facing shared stop name.
- `StopPoint.displayName` may be direction/platform-specific.
- Routing and pattern order logic must use `StopPointId`, not `displayName`.

## Direct Route Determinism

Not-found precedence for the current direct-route core:
- `SAME_STOP`
- `ORIGIN_NOT_FOUND`
- `DESTINATION_NOT_FOUND`
- `DESTINATION_NOT_AFTER_ORIGIN`
- `NO_DIRECT_PATTERN`
