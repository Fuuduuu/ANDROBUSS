# TESTING_STRATEGY

## Current Test Baseline

- `core-domain` tests exist and run.
- `ServiceCalendarResolver` semantics are tested with explicit `LocalDate` inputs.
- `core-gtfs` CSV/parser/mapper tests exist and run.
- `core-routing` direct-route tests exist and run.
- `city-adapters` metadata tests exist and run.
- `feature-search` destination resolver tests exist and run.
- `feature-search` place-to-stop candidate mapping tests exist and run.
- CI baseline runs Gradle build/lint.

## Core-Domain Coverage Focus

- ID/display-name invariants.
- `GeoPoint` range validation.
- `RoutePattern` ordering and minimum-stop invariants.
- Duplicate stop-point compatibility for loop patterns.
- Calendar base + exception override semantics.

## Core-GTFS Coverage Focus

- CSV quoting/escaping and CRLF/LF behavior.
- Required-file validation.
- Calendar file presence rules.
- Exception mapping (`1` add, `2` remove, invalid values fail).
- StopPoint identity protection (same-name different-`stop_id` remains separate).

## Core-Routing Coverage Focus

- Direct-route validity rule (destination after origin in same pattern).
- Deterministic not-found precedence.
- Duplicate-stop loop handling.
- Ordered segment extraction.
- StopPointId-only identity behavior.

## City-Adapters Metadata Coverage Focus (PASS 09)

- Rakvere metadata presence and city identity.
- Wave assignment (`WAVE_0`).
- Primary and context feed mapping presence.
- Conservative legal status (no overclaim).
- Alias presence for basic Rakvere variants.
- POI seed non-empty with conservative coordinate policy.
- Registry lookup and duplicate city-id protection.
- Android-free API guard.

## Feature-Search Destination Coverage Focus (PASS 10)

- Blank query handling (`BLANK_QUERY`).
- Unknown query handling (`NO_MATCH`).
- Exact display-name and alias matching.
- Case-insensitive and whitespace normalization behavior.
- Deterministic partial-match ordering.
- Rakvere metadata-backed matches (`Rakvere bussijaam`, `Vaala keskus`, `Kesklinn`).
- Destination target source (`CITY_PLACE_METADATA`) and conservative coordinate pass-through.
- Android-free resolver API guard.

## Feature-Search Place-to-Stop Candidate Coverage Focus (PASS 11)

- `NO_PREFERRED_STOP_GROUPS` behavior.
- `UNSUPPORTED_TARGET_SOURCE` behavior.
- One/multi preferred-stop-group candidate creation.
- Deterministic metadata-order preservation.
- No fabricated `StopPointId` values.
- Conservative candidate confidence policy.
- Android-free candidate resolver API guard.

## Near-Term Test Gaps

- Origin stop-point candidate resolver tests (planned for PASS 12).
- Destination-to-route query bridge behavior tests (future).
- Room persistence/invalidation tests (future).
- UI and end-to-end flow tests (future).
