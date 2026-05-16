# PASS_FEED_01_FEED_FRESHNESS_AND_UPDATE_ARCHITECTURE_DOCS

## Scope

Docs-only architecture decision pass for feed freshness/update lifecycle.

This pass updates architecture/governance documentation only.

## Inputs

### External research summary (Pro)

- Do not implement downloader yet.
- Do not implement WorkManager yet.
- Public GTFS usage needs conservative 7-day hard freshness limit.
- Source/license/attribution metadata must be captured.
- Failed candidate update must not replace active snapshot.

### Architecture audit summary (Claude)

- Keep `DomainFeedSnapshot` metadata-free.
- Store freshness/provenance metadata in infrastructure layer (`data-local`) rather than domain model.
- Prove manual downloader/import path before periodic scheduling.
- Separate Room migration pass from downloader/WorkManager pass.

### Repo state at pass start

- HEAD: `08c02a1` (`docs(governance): fix UI-02 drift counter`)
- Branch: `main`
- Working tree: clean
- Validator: `PROJECT_STATE.yml validation PASSED` (stale accepted-commit warning allowed)

## Decisions

1. **Freshness policy**
- Public production has a hard 7-day freshness limit from successful download/import.
- Static bundled baseline is internal/MVP use and not long-term public-production freshness truth.
- If no valid real feed exists, stale/unavailable state is preferred over silent synthetic masquerading.

2. **Metadata location**
- `DomainFeedSnapshot` remains pure (`cityId`, `stopPoints`, `routePatterns`).
- Freshness/source/activation metadata belongs to future `data-local` metadata persistence.

3. **Activation/swap policy**
- Candidate feed lifecycle is download -> parse -> validate -> import under new `feedId` -> atomic activation.
- Failed candidate import/validation never replaces current active feed.
- Last-known-good active feed remains available until replacement succeeds.

4. **Minimum candidate validation**
- Integrity, unzip/read, parser completion, required data non-empty checks.
- Service-window usability check from calendar data.
- Capture attribution/source metadata.
- Compute deterministic `feedHash`.
- Incomplete candidate import is rolled back/ignored.

5. **Boundary split**
- `data-remote`: download/ZIP/integrity/candidate handoff.
- `data-local`: persistence/metadata/active-feed selection/provider load.
- `app`: manual refresh trigger, stale-state UX, later scheduling trigger.

6. **Room migration policy**
- Metadata schema is deferred to dedicated `PASS_FEED_02`.
- Room migration is protected surface and must be explicit/tested.
- No schema/entity/DAO edits in PASS_FEED_01.

7. **Pass sequence**
- PASS_FEED_01: docs-only decisions.
- PASS_FEED_02: Room metadata schema migration.
- PASS_FEED_03: manual downloader/import path.
- PASS_FEED_04: WorkManager periodic scheduling.

## Files Changed

- `docs/GTFS_PIPELINE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/ROADMAP.md`
- `docs/CURRENT_STATE.md`
- `docs/PROJECT_STATE.yml`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_FEED_01_FEED_FRESHNESS_AND_UPDATE_ARCHITECTURE_DOCS.md`

## Forbidden Scope Respected

- No Kotlin source changes.
- No Room schema/entity/DAO/database version changes.
- No downloader implementation.
- No WorkManager implementation.
- No `DomainFeedSnapshot` model changes.
- No raw GTFS ZIP committed.
- No build/Gradle/lockfile changes.

## Explicit Non-Goals

- implement downloader
- implement WorkManager periodic updates
- modify app bootstrap behavior
- modify Room schema
- add metadata fields into `DomainFeedSnapshot`
- commit raw GTFS ZIP assets

## Validation

Commands run after docs updates:

- `py -3 tools/validate_project_state.py`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- boundary grep checks requested by pass prompt

## Next Recommended Pass

- `PASS_FEED_02_ROOM_SCHEMA_MIGRATION_AND_FEED_METADATA_ENTITY`
