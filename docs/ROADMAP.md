# ROADMAP

Pass status:

1. PASS 00 - REPO_BOOTSTRAP_DOCS: completed
2. PASS 01B - ANDROBUSS_ARCHITECTURE_REVIEW_FIXES: completed
3. PASS 02 - REPO_SKELETON_AND_BUILD: completed
4. PASS 03 - GTFS_SOURCE_DISCOVERY: completed
5. PASS 04 - GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING: completed
6. PASS 05 - CORE_DOMAIN_STOP_AND_PATTERN_MODELS: completed
7. PASS 05B - DOMAIN_NAMESPACE_AND_GUARDRAIL_CLEANUP: completed
8. PASS 06 - SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS: completed
9. PASS 07 - MINIMAL_GTFS_FIXTURE_PARSER: completed
10. PASS 08 - DIRECT_ROUTE_SEARCH_CORE: completed
11. PASS 08B - DOCS_AND_DIAGRAMS_SYNC: completed
12. PASS 09 - RAKVERE_CITY_ADAPTER_METADATA: completed
13. PASS 10 - DESTINATION_TARGET_MODEL_AND_PLACE_RESOLVER_SPEC: completed
14. PASS 11 - PLACE_TO_STOP_CANDIDATE_MAPPING_SPEC: completed
15. PASS 11B - STOP_CANDIDATE_CONFIDENCE_DOCS_FIX: completed
16. PASS 12 - ORIGIN_STOPPOINT_CANDIDATE_RESOLVER_SPEC: completed
17. PASS 13 - DIRECT_ROUTE_QUERY_BRIDGE_AND_PRECONDITION_SPEC: completed
18. PASS 14 - STOPPOINT_RESOLUTION_CONTRACT_AND_NAME_INDEX: completed
19. PASS 15 - STOPPOINT_RESOLUTION_INTEGRATION_AND_BRIDGE_WIRING: completed
20. PASS UX-01 - UX_BLUEPRINT_AND_MVP_SCOPE_SYNC (docs-only): completed
21. PASS 16 - STOP_CANDIDATE_ENRICHMENT_PRODUCTION: completed
22. PASS 16B - ENRICHMENT_DOCS_AND_DIAGRAMS_SYNC (docs-only): completed
23. PASS 17 - RAKVERE_REAL_GTFS_STOP_NAME_DISCOVERY_AND_METADATA_SPEC: completed
24. PASS 18 - DESTINATION_ENRICHMENT_ORCHESTRATOR_AND_AMBIGUITY_SPEC: completed
25. PASS 19 - DIRECT_ROUTE_QUERY_PREPARATION_USE_CASE: completed
26. PASS 20 - GTFS_FIXTURE_TO_SEARCH_PIPELINE_INTEGRATION_TEST: completed
27. PASS 20B - GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC (docs-only): completed
28. PASS 21 - DOMAIN_FEED_SNAPSHOT_AND_PROVIDER_CONTRACT: completed
29. PASS 22A - FEED_IDENTITY_AND_STORAGE_KEY_STRATEGY: completed
30. PASS 22B - FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_WITH_SCOPED_KEYS: completed
31. PASS 23 - FEED_SNAPSHOT_IMPORTER_AND_CI_TEST: completed
32. PASS 24 - FEED_BOOTSTRAP_AND_RUNTIME_WIRING_DECISION (docs-only): completed
33. PASS 25 - BUNDLED_FEED_BOOTSTRAP_SERIALIZATION_AND_APP_LAYER: completed

Next pass:

34. PASS 26A - REAL_RAKVERE_FEED_PROFILE_AND_PARSER_ROBUSTNESS_TESTS (completed)
   - cover quoted `service_id` commas, unknown-column tolerance, `calendar_dates` behavior, and explicit Rakvere `stop_area` filtering policy checks
   - keep WorkManager/downloader/realtime out unless explicitly approved

35. PASS 26B - REAL_RAKVERE_DEV_TEST_ASSET_ONLY (completed)
   - keep real-derived asset in test resources only
   - keep runtime default synthetic and keep real asset out of main APK assets

Next recommended pass:

36. PASS 27 - HILT_DI_BASELINE (completed)
   - app-owned Hilt modules for bootstrap dependencies
   - keep ViewModel/UI/WorkManager/network out of scope

Current governance checkpoint:

37. PASS_AUTO_03 - DRIFT_AND_BOUNDARY_CHECK (completed)
   - validate docs/state/boundaries before ViewModel/UI expansion

Next recommended technical pass:

38. PASS_28A - APP_SEARCH_VIEWMODEL_FEED_AND_DESTINATION_STATE (completed)
   - add app SearchViewModel feed/destination state baseline only
   - keep Compose/UI/navigation and route-query wiring out

39. PASS_28B - ROUTE_QUERY_WITH_EXPLICIT_ORIGIN (completed)
   - add explicit route query trigger and explicit origin requirement
   - keep FeedNotAvailable / DestinationNotReady / OriginNotProvided distinct from RouteNotFound
   - use DirectRouteQueryPreparationUseCase path from app ViewModel

40. PASS_AUTO_04_BOOTSTRAP_ROOM_FIRST_CHECK (completed)
   - harden bootstrap lifecycle: cache -> Room prepare -> bundled import fallback
   - avoid unnecessary bundled re-import on cold process restart when Room snapshot already exists

41. PASS_AUTO_05_EXTEND_DETEKT_BOUNDARY_COVERAGE (completed)
   - extend Detekt boundary-only coverage to app/data-local/feature-search/city-adapters
   - keep runtime/source behavior unchanged

42. PASS_28C_COMPOSE_SEARCH_SCREEN_BASELINE (completed)
   - first Compose search screen in app with explicit destination selection and explicit route search trigger
   - no navigation graph, GPS, network, realtime, or WorkManager scope

Current governance checkpoint:

43. PASS_AUTO_06_DRIFT_AND_UI_BASELINE_CHECK (completed)
   - docs-only drift/state sync after PASS 28C UI baseline acceptance
   - verify boundaries remain closed (navigation/GPS/network/realtime not opened)

Current policy pass:

44. PASS_UI_01_SEARCH_SCREEN_SMOKE_AND_POLISH (completed)
   - narrow first-screen UX polish/smoke hardening without opening navigation/GPS/network/realtime scope

45. PASS_29A_RAKVERE_QUICK_DESTINATION_RESOLUTION_READINESS (completed)
   - readiness test + docs pass only
   - verify proposed Rakvere quick labels against active synthetic runtime snapshot
   - prevent premature quick-destination UI wiring

Current policy pass:

46. PASS_29C_REAL_RUNTIME_FEED_POLICY_BEFORE_QUICK_DESTINATIONS (completed)
   - docs-only protected-surface policy audit for quick-destination unblock conditions
   - confirm metadata alone is insufficient while runtime snapshot remains synthetic
   - define legal/source/freshness/runtime prerequisites before quick-destination UI

Current technical pass:

47. PASS_30_REAL_RAKVERE_STATIC_RUNTIME_PROFILE_BASELINE (completed)
   - promote real-derived Rakvere static profile to runtime primary for internal/MVP use
   - keep synthetic runtime asset as fallback
   - keep public-production freshness solution explicitly unresolved

48. PASS_31_RAKVERE_QUICK_DESTINATIONS_UI_ONLY (completed)
   - add quick-destination UI chips in SearchScreen only
   - use label/queryText resolver path only (no StopPointId shortcuts)
   - keep SearchViewModel/SearchUiState and route-query logic unchanged

Current governance checkpoint:

49. PASS_AUTO_07_DRIFT_AND_UI_BOUNDARY_CHECK (current candidate)
   - docs-only drift reset and boundary verification after PASS 31
   - confirm no GPS/network/realtime/navigation scope expansion
   - reset drift counter before broader feature implementation

Next recommended technical pass:

50. PASS_32_ORIGIN_SELECTION_IMPROVEMENT_SCOPE_AUDIT
   - origin improvements must stay scope-audited before implementation
   - keep GPS/nearest/network/realtime/navigation out unless explicitly opened

Alternative next pass:

51. PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT
   - define downloader/update/freshness policy for public/freely distributed production
   - separate feed lifecycle policy from quick-destination UI pass

Alternative UI scope-audit pass:

52. PASS_UI_02_SEARCH_SCREEN_POLISH_SCOPE_AUDIT
   - scope-audit only for additional UI polish
   - no runtime/feed/network expansion

Future scope candidates (not active next pass):

53. PASS_29_ORIGIN_RESOLUTION_OR_SEARCH_UI_REFINEMENT
   - replace dev-only origin chips with proper origin resolution flow
   - refine first-screen UX states without opening realtime/network scope

54. PASS_RT_01 - GTFS_REALTIME_SCOPE_LOCK_AND_IDENTITY_MODEL
   - lock realtime identity rules (`trip_id` + `stop_sequence`) before implementation
   - keep realtime/network/workmanager out until dedicated runtime pass approval

55. PASS_CITY_PROFILE_01 - PEATUS_GRAPHQL_ROUTE_METADATA_DISCOVERY
   - evaluate Peatus.ee / Digitransit GraphQL as city-route metadata helper
   - keep static GTFS as canonical routing identity source

Likely following passes:

56. PASS 30+ - DOWNLOADER_WORKMANAGER_REFRESH_LIFECYCLE
57. PASS 31+ - FEED_FRESHNESS_HASH_VERSION_METADATA
58. PASS UI-01 - DESTINATION_FIRST_HOME_AND_RESULT_CARDS_IMPLEMENTATION (after production feed/provider/query path is stable)

Planning rule:
- Any UI pass must align with `PASS UX-01` destination-first, list-first MVP blueprint.

Governance track:
- G01 - GOVERNANCE_BOOTSTRAP_DOCS_ONLY (completed)
- G02 - PROJECT_STATE_VALIDATION_HOOKS (completed)
- G03 - AUDIT_INDEX_AND_READ_ORDER_SYNC (completed)
- DRIFT_CHECK_RULE_SYNC_PASS (completed)
- G04 - DOCS_HYGIENE_AND_ARCHIVE_PLAN
- G05 - GTFS_REALTIME_AND_PEATUS_GRAPHQL_FUTURE_NOTES (completed)

Build/tooling track:
- AUTO-01 - DETEKT_MODULE_BOUNDARIES (completed)
- AUTO-02 - DEPENDENCY_LOCKING (completed)
- AUTO-05 - EXTEND_DETEKT_BOUNDARY_COVERAGE (completed)
