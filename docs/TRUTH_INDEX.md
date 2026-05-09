# TRUTH_INDEX

Canonical truths for this repository:

- Tallinn and Tartu are future adapters, not initial targets.
- Wave 0 target city is Rakvere.
- Wave 1 targets underserved cities (for example Võru, Viljandi, Pärnu, Kuressaare).
- National GTFS is the static canonical base data source.
- Realtime is optional per city adapter.
- `Stop` versus `StopPoint` distinction is canonical and must not be collapsed.
- `ServiceCalendarResolver` is high-risk, protected, and must be heavily tested.
- Ticketing is out of scope without legal and partner basis.
- Android code does not exist yet.

## Stop vs StopPoint Canonical Definition

- One public stop name may map to multiple directional `stop_id` / stop point values.
- Routing must resolve the correct `stop_id` using direction and service pattern context.
- UI may present a single rider-friendly stop label while backend logic preserves directional stop-point precision.
- `StopGroup.displayName` is the rider-facing shared/group name.
- `StopPoint.displayName` may be platform- or direction-specific.
- Routing identity remains `StopPointId`, never `displayName`.

## Service Calendar Resolution Priority

- `calendar_dates` exceptions override base `calendar` rules for matching service/date.
- Exception type `1` means add service.
- Exception type `2` means remove service.
- Duplicate `calendar_dates` rows for the same `service_id` + date are treated as invalid and rejected by resolver construction.
- Resolver behavior must apply exception precedence deterministically before route availability decisions.

## Data Confidence Levels

- `REALTIME`: live vehicle/ETA data from active realtime feed ingestion.
- `FORECAST`: computed prediction from schedule and derived timing logic without confirmed live vehicle state.
- `STATIC`: schedule-only GTFS output with no realtime/forecast augmentation.

Threshold policy before realtime implementation:

- `REALTIME` freshness threshold: TODO
- `FORECAST` staleness threshold: TODO
- `STATIC` fallback threshold: TODO
