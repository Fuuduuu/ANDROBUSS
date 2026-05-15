# PASS_29C_REAL_RUNTIME_FEED_POLICY_BEFORE_QUICK_DESTINATIONS

## Objective

Document the protected-surface policy that must be satisfied before real Rakvere quick destinations can be implemented in UI.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `0fca2fcbc66329de87d858bd6dc035ccdd4f7fdb`
- Working tree at start: clean
- `py -3 tools/validate_project_state.py` gate: passed (warning-only stale commit note before this pass updates)

## Files Read

- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/INVARIANTS.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_29A_RAKVERE_QUICK_DESTINATION_RESOLUTION_READINESS.md`
- `app/src/main/assets/bootstrap/rakvere_bootstrap.json`
- `app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/RakvereQuickDestinationReadinessTest.kt`
- `city-adapters/src/main/kotlin/ee/androbus/cityadapters/rakvere/RakvereCityAdapterMetadata.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/DestinationTargetResolver.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/destination/PlaceToStopCandidateResolver.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/InMemoryStopPointIndex.kt`

## Runtime Feed Policy Decision

- Real Rakvere quick destinations are blocked while active runtime snapshot remains synthetic (`Keskpeatus`, `Spordikeskus`, `Jaam`).
- Metadata alone does not unblock quick destinations when runtime stop set does not contain the intended labels.
- Test-only real profile (`app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json`) must not be treated as runtime truth.
- Production use of real runtime feed remains blocked until legal/source/attribution/freshness policy is explicitly accepted.
- Existing 7-day freshness constraint for public app data remains a blocking design constraint for production real static bundling without update policy.

## Metadata Resolver Policy

- City metadata may provide user-facing label/query intent only.
- Verified routing identity must still come from active runtime snapshot resolution path (`VerifiedStopPointCandidate.stopPointId`).
- Direct stop-ID shortcuts from quick buttons remain forbidden.
- Ambiguous resolution remains user-choice state; no auto-selection.

## Quick Destination Unblock Criteria

Quick destination UI is allowed only when all are true:

1. The quick label/query text is passed through normal destination flow (`SearchViewModel.onDestinationChanged(queryText)`).
2. Resolution yields a verified `StopPointId` from active runtime snapshot data.
3. Ambiguous outcome still requires explicit user selection.
4. UI constants do not inject `StopPointId` values.
5. Tests prove labels resolve in the runtime configuration actually used by the app.

## Source-code Untouched Confirmation

- No Kotlin/source/build/runtime/asset changes in this pass.
- No UI implementation, no resolver implementation, no runtime feed switch.

## Remaining Open Constraints

- Real runtime feed enablement path is still undecided (static real bundle policy vs downloader/update policy).
- Quick-destination UI must stay blocked until runtime feed policy is approved and test-proven.

## Validation Result

Commands run:

- `py -3 tools/validate_project_state.py`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`

Result: pass.

## Files Changed

- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_29C_REAL_RUNTIME_FEED_POLICY_BEFORE_QUICK_DESTINATIONS.md`

## Drift Counter Note

- Drift counter is now tracked as `3 / 5` (`PASS_UI_01` + `PASS_29A` accepted, `PASS_29C` current candidate).

## Next Recommended Pass

- `PASS_30_REAL_RUNTIME_FEED_ENABLEMENT_SCOPE_AUDIT`
