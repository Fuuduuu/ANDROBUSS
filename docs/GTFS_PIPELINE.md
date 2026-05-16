# GTFS_PIPELINE

GTFS pipeline status after PASS 24 docs decision.

## Current Implemented Scope

- `core-gtfs` can parse tiny local fixture folders.
- Supported file set in current parser:
  - `agency.txt`
  - `stops.txt`
  - `routes.txt`
  - `trips.txt`
  - `stop_times.txt`
  - `calendar.txt` and/or `calendar_dates.txt`
- Required-file validation exists.
- Calendar exception mapping exists:
  - `exception_type 1` -> `ADD_SERVICE`
  - `exception_type 2` -> `REMOVE_SERVICE`
- Domain mapping exists for:
  - stop points,
  - route patterns,
  - trips,
  - service calendars/exceptions.

## Fixture Status

- Parser tests use synthetic tiny fixture only:
  - `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`
- Fixture is deterministic and intentionally small.
- It is not a production data snapshot.
- Synthetic fixture stop names are not authoritative city metadata for real Rakvere mapping.
- PASS 20 integration tests prove parser/mapper output can seed feature-search pipeline:
  - `MappedGtfsFeed.stopPoints` seeding `InMemoryStopPointIndex`
  - `MappedGtfsFeed.routePatterns` supplied to `DirectRouteQueryPreparationUseCase`
- This integration proof does not implement production feed ingestion, provider abstractions, or cache wiring.
- PASS 20 tests do not use real `rakvere.zip`; they use `rakvere-smoke` fixture only.

## PASS 21 Feed Boundary

- PASS 21 introduced parser-agnostic `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` boundary contracts.
- PASS 22B moved those contracts to `core-domain`.
- Callers may convert parser output into snapshot boundary objects:
  - `DomainFeedSnapshot(cityId, mappedFeed.stopPoints, mappedFeed.routePatterns)`
- PASS 21/22B do not implement downloader or production ingestion orchestration.

## PASS 22A/22B Identity + Storage Baseline

- GTFS `stop_id` and trip-derived route-pattern IDs are treated as feed/city-local storage identifiers.
- Room baseline schema keys are city/feed-scoped:
  - stop point storage key: `cityId + feedId + stopId`
  - route pattern storage key: `cityId + feedId + patternId`
  - pattern stop storage key: `cityId + feedId + patternId + sequence`
- PASS 22B adds `data-local` baseline Room entities/DAO/mapper/loader/provider using these scoped keys.
- Routing identity semantics remain unchanged (`StopPointId` still comes from persisted/verified stop IDs only).

## PASS 23 Import Writer Baseline

- PASS 23 adds `data-local` production `FeedSnapshotImporter`:
  - input: `DomainFeedSnapshot` + explicit `CityId` + explicit `FeedId`
  - write: scoped Room replace transaction in `FeedSnapshotDao`
- Importer remains parser-agnostic in production code:
  - no `MappedGtfsFeed`
  - no `GtfsFeedParser`
  - no `GtfsDomainMapper`
- PASS 23 integration coverage proves fixture-based roundtrip:
  - `GtfsFeedParser` + `GtfsDomainMapper` (test scope only)
  - `DomainFeedSnapshot`
  - Room importer write
  - Room provider prepare/load
  - feature-search route query preparation from Room-loaded patterns

## PASS 24 Bundled Asset Bootstrap Strategy

- MVP feed bootstrap source is a bundled APK asset, not a live network download.
- Bundled asset is produced offline from real GTFS and represented as serialized `DomainFeedSnapshot` (or equivalent app-readable format).
- PASS 24 does not choose JSON vs protobuf vs other serialization; that is PASS 25 scope.
- First launch / Room empty flow:
  1. app layer reads bundled feed asset
  2. app layer converts it to `DomainFeedSnapshot`
  3. app layer calls `FeedSnapshotImporter.import(cityId, feedId, snapshot)`
  4. app layer or search bootstrap owner calls `RoomDomainFeedSnapshotProvider.prepare(cityId, feedId)`
  5. after prepare, `getSnapshot(cityId)` may return non-null synchronously
- Production download/refresh remains future work via `data-remote` + WorkManager.
- This strategy satisfies offline-first MVP without requiring network bootstrap infrastructure.
- Real `rakvere.zip` itself must not be committed; bundled asset generation details are future pass work.
- Feed freshness/hash/version metadata is future and not part of PASS 24.

## PASS 25 Bundled Bootstrap Implementation Baseline

- PASS 25 implements a synthetic bundled JSON bootstrap in the app layer:
  - `app/src/main/assets/bootstrap/rakvere_bootstrap.json`
  - app DTOs (`BootstrapFeedDto`, `StopPointDto`, `RoutePatternDto`)
  - DTO -> `DomainFeedSnapshot` mapping without `core-domain` serialization annotations
  - `FeedBootstrapLoader` startup flow:
    1. read bundled asset
    2. decode DTO
    3. convert to `DomainFeedSnapshot`
    4. call `FeedSnapshotImporter.import(cityId, feedId, snapshot)`
    5. call `RoomDomainFeedSnapshotProvider.prepare(cityId, feedId)`
- `FeedBootstrapLoader.bootstrapIfNeeded()` is safe on repeated calls and handles missing asset as FeedNotReady-style no-crash return.
- `AndrobussApplication` performs pre-Hilt bootstrap on background dispatcher.
- `AppDatabase.create(context)` exists as temporary pre-Hilt factory.
- PASS 25 does not use real `rakvere.zip`, does not invoke parser in app production code, and does not add freshness/hash/version metadata.

## GTFS Data Legal Status for Production Bundled Assets

- Current app bundled asset is synthetic and is not real Rakvere production data.
- Recorded PASS 26 findings indicate an official GTFS endpoint exists at Peatus/UTR.
- Recorded PASS 26 findings indicate public transport registry open data is publicly available.
- Recorded PASS 26 findings indicate reuse in public channels/mobile apps requires source attribution.
- Recorded PASS 26 findings indicate public app-used data must not be older than 7 days from download.
- Therefore production real bundled asset is not automatically green-lit.
- Real asset generation may proceed only as:
  - dev/test asset, or
  - production asset with documented freshness/update policy, or
  - after explicit manual permission/confirmation.
- Raw `rakvere.zip` must not be committed.
- This pass documents constraints only and does not claim legal certainty is fully resolved.
- PASS 26 must resolve/document this legal/data-source status before PASS 26A real-asset generation.

## Real Rakvere Feed Technical Profile

Documented technical findings snapshot for planning (PASS 26 input baseline):

- `feed_info.txt` exists; publisher is Regionaal- ja Pollumajandusministeerium.
- `attributions.txt` is missing.
- `calendar_dates.txt` exists (45 rows).
- `frequencies.txt` is missing.
- `transfers.txt` is missing.
- `shapes.txt` exists.
- `block_id` is present on all 361 trips.
- No blank `arrival_time` / `departure_time` rows in current snapshot.
- No times over `24:00:00` in current snapshot.
- Snapshot profile counts: 4 routes / 361 trips / 1215 stops / 6292 `stop_times` rows.
- 98 stops have `stop_area = "Rakvere linn"` (out of 1215 total).
- `service_id` values may contain quoted commas (example style: `"Liinid_1,2,3,5-We"`).

## GTFS Fields Policy for MVP

| Field/file | MVP handling | Future handling | Reason |
| --- | --- | --- | --- |
| `stop_id`, `stop_name`, `stop_lat`, `stop_lon` | Use | Keep | Core stop identity and location baseline |
| `stop_area` | Use for explicit Rakvere city filtering policy | Keep/extend | Scope control for real-city asset extraction |
| `route_id`, `route_short_name` | Use | Keep/extend | Route identity and rider-facing labels |
| `trip_id`, `direction_id` | Use | Keep/extend | Pattern/trip mapping and direction context |
| `stop_sequence`, `arrival_time`, `departure_time` | Use | Keep/extend | Ordered routing and schedule semantics |
| `service_id` + `calendar.txt` | Use | Keep/extend | Base service calendar |
| `calendar_dates.txt` | Use | Keep/extend | Required service exceptions |
| `block_id` | Tolerate/read if present, ignore for routing | Evaluate later | Operational block linkage is not MVP routing input |
| `shapes.txt` | Ignore for MVP | Future map/geometry | Not needed for destination-first MVP |
| `continuous_pickup`, `continuous_drop_off` | Tolerate/ignore | Evaluate later | Not required by current routing scope |
| `trip_headsign_code`, `stop_headsign_code` | Tolerate/ignore for MVP | Evaluate later | Non-blocking metadata for current UX |
| Accessibility/bikes/colors fields | Ignore for MVP | Evaluate later | Not required for current direct-route baseline |
| `frequencies.txt`, `transfers.txt` | Future only | Implement in later passes | Out of current direct-route MVP scope |
| Unknown columns | Tolerate and ignore | Keep tolerance | Forward compatibility with feed variants |

## Parser Robustness Requirements Before Real Asset

- Parser must tolerate unknown columns without failure.
- Parser must correctly parse quoted CSV fields containing commas in `service_id`.
- Parser must correctly handle `calendar_dates` exception semantics.
- Parser must not require `attributions.txt`, `frequencies.txt`, or `transfers.txt`.
- Parser/domain mapping must preserve `StopPointId` from `stop_id` only.
- Parser/domain mapping must never derive stop IDs from display names.
- `stop_area` filtering policy for Rakvere must be explicit and tested before any real asset commit.

## Stop Area Filtering Policy

- `GtfsFeedParser` reads all stops present in `stops.txt`.
- `GtfsDomainMapper` maps all parsed stops into domain stop points.
- `stop_area` filtering is not parser responsibility.
- Rakvere MVP real-asset generation should apply explicit `stop_area = "Rakvere linn"` filtering policy.
- Filtering belongs in future asset-generation tooling, not in parser core.

## Real-derived Dev/Test Asset Policy

- Real-derived Rakvere dev/test profile asset lives under:
  - `app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json`
- PASS 30 promotes a real static runtime baseline for internal/MVP use:
  - primary runtime asset: `app/src/main/assets/bootstrap/rakvere_feed_20260428.json`
  - fallback runtime asset: `app/src/main/assets/bootstrap/rakvere_bootstrap.json` (synthetic)
- This is not a public-production freshness solution.
- Public/freely distributed production still requires downloader/update/freshness policy.
- Raw `rakvere.zip` remains forbidden in repository commits.
- Dev/test asset route-pattern IDs are representative GTFS `trip_id` values, not synthetic counter labels.
- Dev/test asset stop IDs are GTFS `stop_id` values only.
- Runtime quick-destination identity must still resolve from active snapshot `StopPoint.id` values.

## Future GTFS Realtime and Route Metadata Notes

### Loop / stop_sequence identity

- In loop routes, the same `stop_id` may appear multiple times in one trip.
- `stop_id` alone cannot identify a specific stop occurrence.
- `stop_sequence` is the disambiguating field.
- This confirms current Room storage design:
  - `PatternStopEntity` primary key = `cityId + feedId + patternId + sequence`.
- Do not change this schema.
- `RoutePattern` stop IDs must preserve exact order and duplicates.
- Do not de-duplicate pattern stop IDs.

### Future GTFS Realtime identity

- GTFS Realtime `TripUpdate` / `VehiclePosition` matching depends on static GTFS `trip_id` and `stop_sequence`.
- If realtime is added later, Room-loaded/static `trip_id` values must match realtime `trip_id` values exactly.
- Real-derived route-pattern IDs must not use synthetic labels such as `rakvere-dev-pattern-N`.
- PASS 26B dev/test asset uses representative GTFS `trip_id` values for route-pattern IDs.
- Full realtime support requires a later protected-surface pass with explicit `TripId` modelling.

### Future realtime freshness

- Realtime freshness is a future `data-remote` concern.
- Realtime cannot be supported with the current offline-only static/bundled pipeline.
- Future realtime requires network polling, freshness enforcement, and likely WorkManager or a dedicated runtime fetch layer.
- This is not implemented now.

### Future realtime enum note

- If GTFS Realtime parser is implemented later, do not design it around deprecated `ADDED` semantics.
- Use current GTFS Realtime semantics such as `NEW` / `DUPLICATED` where applicable.
- This is not implemented now.

### Future Peatus.ee / Digitransit GraphQL route metadata

- Peatus.ee / Digitransit GraphQL may be useful later as a route-metadata and city-onboarding helper.
- It is not MVP runtime source.
- It is not canonical routing identity.
- It is not a replacement for static GTFS.
- It is not implemented now.
- The endpoint must be revalidated before any future implementation pass.

Potential future uses:
- discover city routes by city/operator/`route_short_name`
- inspect route `longName` / `desc`
- inspect pattern `directionId` / `headsign`
- compare pattern stop order against static GTFS-derived `RoutePattern` values
- help build `CityRouteProfile` per city
- validate manual route-variant data such as Linnaliinid ZIP-derived profiles
- derive user-facing route labels such as "kaudu Keskvaljak" or "Pohjakeskus suund"
- group E-R / L-P date-based schedule variants in a future profiling pass

Future pass candidate:
- `PASS_CITY_PROFILE_01 — PEATUS_GRAPHQL_ROUTE_METADATA_DISCOVERY`

Explicit non-scope:
- block transfer remains future-only
- frequency-based trips remain future-only
- service alerts remain future-only
- vehicle positions remain future-only
- pathways and fare attributes remain out of current MVP
- Peatus GraphQL integration remains future-only

## PASS 17 Metadata Discovery Note

- Real `rakvere.zip` `stops.txt` was inspected in a temp folder for conservative Rakvere POI stop-name discovery.
- Discovery output is used only for metadata hints (`preferredStopGroupNames`), not parser/runtime behavior.
- Downloaded ZIP/data files remain outside the repository and must not be committed.

## Feed Freshness and Update Architecture (PASS_FEED_01)

### Freshness policy

- Public production must not present GTFS data as current when it is older than 7 days from successful download/import.
- Official daily update cadence means stale-warning UX may appear earlier, but 7 days is the hard technical limit.
- `rakvere_feed_20260428.json` is an internal/MVP static baseline, not long-term public-production freshness truth.
- Synthetic fallback asset is fallback/demo only and must not silently masquerade as current public data.
- Public mode should prefer last-known-good real feed when still valid.
- If no valid real feed exists, app should show stale/unavailable state instead of silently switching to synthetic truth.

### Metadata model decision

- Freshness/provenance fields are infrastructure metadata, not routing-domain model fields.
- `DomainFeedSnapshot` remains pure routing snapshot with:
  - `cityId`
  - `stopPoints`
  - `routePatterns`
- Do not add `downloadedAt`, `sourceUrl`, `feedHash`, or similar fields into `DomainFeedSnapshot`.
- Future persistence target is `data-local` metadata record (for example `FeedMetadataEntity`) linked to `cityId + feedId`.
- Planned metadata fields:
  - `cityId`
  - `feedId`
  - `sourceUrl`
  - `sourceLicense` or `licenseUrl`
  - `publisher` or `feedPublisherName`
  - `attributionText`
  - `downloadedAt`
  - `importedAt`
  - `feedHash`
  - `feedVersion`
  - `validFrom`
  - `validUntil`
  - `staleAfter`
  - `isActive`
  - optional activation status field

### Candidate import and activation policy

- Candidate feed must not overwrite current active feed directly.
- Required lifecycle:
  1. download candidate
  2. unzip/read candidate
  3. parse candidate
  4. validate candidate
  5. import under new scoped `feedId`
  6. activate only after validation/import success
- Activation must be atomic from reader perspective.
- Any failed download/import/validation keeps current active snapshot unchanged.
- Previous active feed remains last-known-good until replacement is verified.
- Feed identity remains scoped by `cityId + feedId + localId`.
- Rakvere city feed and broader county/context feed remain separate `feedId` scopes until dedicated merge/namespacing pass.

### Minimum candidate validation

- download integrity / transport success
- unzip/read success
- parser completion
- routing-critical files present and readable
- `stops` / `routes` / `trips` / `stop_times` non-empty
- `calendar` / `calendar_dates` produce usable service window
- `feed_info` parsed when present
- `validUntil` / feed-end is not already expired when available
- attribution/source metadata captured
- deterministic `feedHash` calculated
- incomplete import can be rolled back or ignored without replacing active feed

### data-remote / data-local / app boundary

- `data-remote` future boundary:
  - source request
  - HTTP download
  - ZIP handling
  - basic integrity checks
  - parser orchestration handoff for candidate feed
- `data-local` future boundary:
  - Room persistence
  - scoped feed storage tables
  - feed metadata persistence
  - active feed selection/activation
  - provider prepare/load from active feed scope
- `app` future boundary:
  - manual refresh trigger
  - later WorkManager scheduling trigger
  - stale/unavailable user-state wiring
  - user-visible refresh/update notification
- Boundary rules:
  - `data-remote` must not create UI state
  - `data-local` must not depend on `feature-search`
  - `core-domain` stays Android-free
  - `DomainFeedSnapshot` stays metadata-free

### Room migration policy

- Feed metadata persistence requires a dedicated Room schema migration pass (`PASS_FEED_02`).
- Migration from current schema to metadata-enabled schema is a protected surface and must be explicit + tested.
- PASS_FEED_01 does not change entities/DAO/database versions.
- Room migration must not be bundled together with downloader or WorkManager implementation.
- `allowMainThreadQueries` remains forbidden.

### Update trigger roadmap

- `PASS_FEED_01` (current): docs-only architecture decision.
- `PASS_FEED_02`: Room migration + metadata entity + tests.
- `PASS_FEED_03`: manual/foreground downloader + candidate import pipeline.
- `PASS_FEED_04`: WorkManager periodic update scheduling.
- Later:
  - county/city feed merge-namespacing pass if required
  - user-facing freshness UI polish
  - official source URL refresh if data portal changes

## Expected Full Pipeline (Future)

1. Discover/select feed set by city mapping metadata.
2. Download feed(s) and collect provenance metadata.
3. Validate integrity and required schema files.
4. Parse/map to canonical domain.
5. Persist to Room cache.
6. Expose offline-first query surfaces.
7. Optionally layer realtime by city adapter.

## Not Implemented Yet

- Network downloader/update checks.
- ZIP ingestion from live sources in runtime.
- feed freshness/version metadata.
- app runtime wiring that prepares and consumes Room snapshot provider.
- Shapes/fares/transfers handling.
- Realtime ingestion.
- Production feed-to-city adapter orchestration.

## Pipeline Rules

- Static GTFS remains canonical base.
- Multi-feed ingestion is expected (not one guaranteed national ZIP).
- StopPoint precision must be preserved through mapping.
- `calendar_dates` override behavior remains mandatory.
