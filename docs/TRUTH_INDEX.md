# TRUTH_INDEX

Canonical truths for this repository:

Truth hierarchy (highest to lowest for decision-making):
1. Passing validation / CI
2. Git commit history
3. `docs/PROJECT_STATE.yml`
4. `docs/TRUTH_INDEX.md`
5. `docs/INVARIANTS.md`
6. `docs/ACTIVE_SCOPE_LOCK.md` (if present)
7. `docs/audit/PASS_*.md`
8. `docs/CURRENT_STATE.md`
9. Diagrams
10. Chat history (temporary workbench only)

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
- ANDROBUSS is destination-first.
- The primary rider question is `Kuhu soovid minna?`
- Riders must not need stop IDs, route IDs, or direction jargon to start a query.
- StopPoint complexity remains internal; rider UX must hide this complexity.
- Home screen is list-first, not map-first.
- Map is a secondary helping surface and input aid, not the primary cognitive burden or routing engine.
- Initial result view should show only 1-3 best options.
- Full timetable and full stop list belong to detail/progressive disclosure, not home or primary results.
- Empty states must be explanatory and action-oriented.
- Accessibility is an MVP requirement, not polish.
- Destination target resolution is metadata-based (`CITY_PLACE_METADATA`) and does not perform nearest-stop/routing/map logic.
- Rakvere `preferredStopGroupNames` must be sourced from real `rakvere.zip` `stops.txt` discovery, not synthetic fixture names.
- `rakvere-smoke` stop names (`Jaam`, `Keskpeatus`, `Spordikeskus`) are synthetic fixture names and must not be treated as real Rakvere POI names.
- Place-to-stop candidate mapping uses preferred stop-group names as unresolved name-level candidates; it does not fabricate `StopGroupId` or `StopPointId`.
- If a Rakvere POI has no confident real `stop_name` match, `preferredStopGroupNames` must remain empty.
- `StopCandidateEnricher` populates `StopCandidate.stopPointIds` only from `VerifiedStopPointCandidate.stopPointId`.
- `StopCandidateEnricher` must never derive `StopPointId` from stop-group names, display names, place names, manual text, or coordinates.
- `DestinationEnrichmentOrchestrator` coordinates destination-side enrichment only.
- `DestinationEnrichmentOrchestrator` must not select a single verified stop-point candidate.
- `DestinationEnrichmentResult.Enriched.isAmbiguous=true` means caller/ViewModel must handle disambiguation before route-query preparation.
- Direct route query preparation may call bridge only when destination is non-ambiguous, origin `StopPointId` is explicit, and `RoutePattern` list is supplied.
- Ambiguous destination must not be routed.
- Direct route query preparation uses exact-one destination policy (`single()`), never hidden `first()` selection.
- PASS 20 parser-to-search integration uses only parser-derived `StopPoint.id` values as route IDs.
- PASS 20 proves parser-to-search integration at fixture level only.
- `MappedGtfsFeed.stopPoints` can seed `InMemoryStopPointIndex`.
- `MappedGtfsFeed.routePatterns` can be supplied to `DirectRouteQueryPreparationUseCase`.
- GTFS `stop_id` and `tripId`-derived `RoutePatternId("pattern:${tripId}")` are treated as feed/city-local IDs for persistence purposes.
- Room storage identity is scoped:
  - stop point key: `cityId + feedId + stopId`
  - route pattern key: `cityId + feedId + patternId`
  - pattern stop key: `cityId + feedId + patternId + sequence`
- `PatternStop` identity key must include sequence; repeated `stopId` values inside one pattern remain valid and must not be collapsed.
- `docs/PROJECT_STATE.yml` is schema-checked by `tools/validate_project_state.py` and by CI.
- `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` live in `core-domain` as parser-agnostic feed boundary contracts.
- `DomainFeedSnapshot.stopPoints` and `DomainFeedSnapshot.routePatterns` must come from the same feed snapshot/version.
- `InMemoryDomainFeedSnapshot` remains in `feature-search` as single-city in-memory implementation.
- `FeedSnapshotImporter` lives in `data-local` and writes `DomainFeedSnapshot` into Room by scoped `cityId + feedId` replacement.
- `FeedSnapshotImporter` accepts domain snapshot types only; parser types (`MappedGtfsFeed`, `GtfsFeedParser`, `GtfsDomainMapper`) are test-only.
- MVP feed bootstrap source is a bundled APK asset, not a live network download.
- `RoomDomainFeedSnapshotProvider` lives in `data-local` and uses load-then-serve behavior:
  - `prepare(cityId, feedId)` performs Room IO via suspend loader
  - `getSnapshot(cityId)` serves in-memory cache only (no DAO calls)
- `prepare(cityId, feedId)` must complete before `getSnapshot(cityId)` can return a non-null snapshot.
- Empty Room / unprepared provider is a valid first-launch state named `FeedNotReady`.
- `FeedNotReady` is not an error and must not be rendered as an empty search result.
- Active feed policy for MVP is last-prepared feed per city.
- `FeedSnapshotImporter` writes Room data only; it does not read assets or parse GTFS.
- `RoomDomainFeedSnapshotProvider` prepares/serves snapshots only; it does not import or download feeds.
- Network refresh/downloader/WorkManager remain future, not MVP baseline.
- PASS 23 integration tests prove parser fixture output can flow through:
  - parser/mapper -> `DomainFeedSnapshot` -> Room importer -> Room provider -> feature-search route query preparation
- `feature-search` production code must not depend on `core-gtfs` parser implementation.
- `testImplementation(project(":core-gtfs"))` is allowed for integration tests (`feature-search`, `data-local`) and must not leak into production dependencies.
- `feature-search` and `data-local` remain independent in production dependencies.
- Stop-candidate enrichment does not upgrade `StopCandidate.confidence`; confidence still describes how the original name-level candidate was produced.
- Presence of `stopPointIds` indicates resolution happened through verified candidates.
- Multiple same-name `StopPoint` matches must remain preserved through enrichment.
- Origin enrichment is not implemented yet.
- `StopCandidateEnricher` is production code but is not yet wired into app/ViewModel runtime flow.
- `DestinationEnrichmentOrchestrator` does not call `DirectRouteQueryBridge`; bridge usage remains a separate step.
- `DirectRouteQueryPreparationUseCase` is separate from enrichment and does not call `DestinationEnrichmentOrchestrator`.
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
- MVP excludes ticketing/accounts/login requirements, live vehicle map, heavy map-first UI, and multimodal clutter.

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
