# Standalone Bus App Architecture

Status: DRAFT 1.0
Type: Architecture guidance
Phase: Pre-implementation

## Intent

Define the foundational architecture for a standalone Android bus application focused on practical city transit, starting from underserved regions and preserving strict data/routing correctness.

## Non-Goals

- Not an AnniVibe UI clone.
- Not tied to `AnniVibez_clean`.
- Not implementing ticketing without legal and partner basis.

## Core Decisions (Locked for Draft 1.0)

- Standalone Android bus app.
- Modular, city-agnostic transit core.
- Tallinn and Tartu are future adapters, not initial priorities.
- Wave 0 target city: Rakvere.
- Wave 1 target cities: Võru, Viljandi, Pärnu, Kuressaare.
- National GTFS static feed is canonical base.
- Realtime is optional and adapter-specific per city.
- Offline-first behavior is mandatory.
- Stack: Kotlin + Jetpack Compose + MVVM + Room + WorkManager + Hilt.
- `Stop` vs `StopPoint` distinction is critical and canonical.
- `ServiceCalendarResolver` is a protected surface with strict test requirements.
- Workflow: docs-first, pass-based, sniper-prompt execution.

## Planned Architecture Shape

- Presentation layer: Compose screens with MVVM ViewModels.
- Domain layer: canonical entities, routing logic, calendar resolution.
- Data layer: GTFS ingest, local Room cache, optional realtime merge.
- Adapter layer: city-specific contracts for feed interpretation and realtime capabilities.

## Priority Sequencing

- First implementation wave emphasizes Rakvere baseline utility.
- Later waves add underserved cities through adapter reuse.
- Dense network cities (Tallinn/Tartu) are deferred until core stability is proven.

## Critical Correctness Areas

- Calendar correctness across weekday/weekend/exception service windows.
- Stop versus stopPoint semantic consistency across parsing, storage, and UI outputs.
- Routing answers must be actionable rider guidance, not raw transport metadata.

## Risk Register (Initial)

- GTFS quality variance between sources and city publishers.
- Calendar exceptions causing silent route disappearance or false positives.
- Realtime drift and latency creating confidence issues if not clearly optional.

## Verification Philosophy

- Treat `ServiceCalendarResolver` and canonical data model as regression-critical.
- Require exhaustive branch/path testing for calendar decision points.
- Validate routing output against field reality in each onboarded city.

