# rakvere_dev_profile_v1.json

This file is a real-derived **dev/test-only** BootstrapFeedDto JSON asset for ANDROBUSS tests.

## Source

- Source dataset: Ühistranspordiregister / Peatus.ee GTFS (`rakvere.zip`)
- Publisher from `feed_info.txt`: Regionaal- ja Põllumajandusministeerium
- Publisher URL from `feed_info.txt`: https://agri.ee/
- Feed version from `feed_info.txt`: Updated: Apr 28, 2026, 7:41 AM
- Feed start/end: 20251003 - 20271231
- Generated from the `rakvere.zip` uploaded to this ChatGPT session on 2026-05-13.

## Use policy

- Test/dev only.
- Must not be copied to `app/src/main/assets`.
- Must not become production runtime default until freshness/update policy exists.
- Raw `rakvere.zip` is not committed.
- Current runtime default remains the synthetic `rakvere_bootstrap.json`.

## Contents

- cityId: `rakvere`
- feedId: `rakvere-dev-profile-v1`
- stopPoints: 98 (`stop_area == "Rakvere linn"`)
- routePatterns: 7 unique retained Rakvere-city route-pattern variants


## Identity policy

- `stopPoints[].id` values are copied from GTFS `stops.txt.stop_id`.
- `routePatterns[].id` values are copied from a representative GTFS `trips.txt.trip_id` for each unique retained stop sequence.
- No `StopPointId` or `RoutePatternId` is generated from display names, route labels, city names, coordinates, or synthetic counters.
- `routePatterns[].routeLineId` is copied from GTFS `routes.txt.route_id`.

## Important notes

- StopPoint IDs are copied from GTFS `stop_id` fields.
- Stop names are display labels only and must not be used as routing identity.
- This asset intentionally filters stops to `stop_area == "Rakvere linn"`.
- The source GTFS file contains 1215 stops in total.
- The source GTFS file has 0/361 trips with non-empty `block_id` in this uploaded version.
