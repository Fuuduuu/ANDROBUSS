# PASS_G04_GOVERNANCE_STATE_SYNC_AFTER_AUTO_02

## Objective

Fix governance/docs drift found after the full project audit so state docs match accepted HEAD `3bb2fe1` after AUTO-02.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `3bb2fe1b1a86cd9dd74423670f6ef0669ffdad27`
- Working tree before edits: clean
- `py -3 tools/validate_project_state.py`: passed (warning-only stale-commit warning before sync)

## Drift Found

- `docs/PROJECT_STATE.yml` still pointed to `a704bbb` and `PASS_G03`.
- `docs/CURRENT_STATE.md` still reported latest accepted HEAD as `a704bbb`.
- `docs/ROADMAP.md` still showed G03/AUTO-02 as current candidates.
- `docs/AUDIT_INDEX.md` still marked G03 as candidate and lacked accepted AUTO-01/AUTO-02 rows.
- `docs/GTFS_PIPELINE.md` needed explicit legal/status caution before real Rakvere asset work.

## Fields/Statuses Updated

- `PROJECT_STATE` accepted baseline moved to `3bb2fe1b1a86cd9dd74423670f6ef0669ffdad27`.
- Accepted guardrails recorded:
  - PASS G03 accepted
  - PASS AUTO-01 accepted
  - DRIFT_CHECK_RULE_SYNC_PASS accepted
  - PASS AUTO-02 accepted
- Drift counter reset note added: `0 / 5` after sync.
- `CURRENT_STATE` and `ROADMAP` statuses aligned with accepted checkpoints.
- `AUDIT_INDEX` aligned with accepted commits and current G04 candidate entry.

## GTFS Legal-Status Caution Added

- Added explicit note that current bundled asset is synthetic.
- Added requirement to document source/license/attribution/APK-bundling permission before real Rakvere asset commit.
- Stated that `rakvere.zip` must not be committed.
- Marked PASS 26 as required legal/data-source decision checkpoint before PASS 26A.

## Source-Code Untouched Confirmation

- No Kotlin/source files changed.
- No Gradle/build/CI files changed.
- No runtime behavior changes.

## Validation Result

Executed:
- `py -3 tools/validate_project_state.py`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`

All passed for docs-only scope.

## Files Changed

- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/GTFS_PIPELINE.md`
- `docs/audit/PASS_G04_GOVERNANCE_STATE_SYNC_AFTER_AUTO_02.md`

## Next Recommended Pass

- `PASS 26 — GTFS_LEGAL_AND_REAL_RAKVERE_ASSET_DECISION`

## Drift Counter Note

- Drift-check cadence reset to `0 / 5` after this docs-only reconciliation pass.
