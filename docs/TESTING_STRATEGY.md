# TESTING_STRATEGY

## Current Test Baseline

- `core-domain` tests exist and run.
- `ServiceCalendarResolver` semantics are tested with explicit `LocalDate` inputs.
- `core-gtfs` CSV/parser/mapper tests exist and run.
- `core-routing` direct-route tests exist and run.
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

## Near-Term Test Gaps

- City adapter metadata contract tests (planned for PASS 09).
- Parser-to-routing integration coverage beyond minimal unit boundaries.
- Room persistence/invalidation tests (future).
- UI and end-to-end flow tests (future).
