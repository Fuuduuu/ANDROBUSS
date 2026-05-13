# DRIFT_CHECK_RULE_SYNC_PASS

## Goal

Add an explicit governance rule that every 5 accepted pass/checkpoints requires a docs-only drift-check before the next large implementation pass.

## Files Read

- `docs/MEMORY_MAINTENANCE.md` (missing)
- `docs/PASS_QUEUE.md` (missing)
- `docs/AUDIT_INDEX.md`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`

## Files Changed

- `docs/PROJECT_STATE.yml`
- `docs/audit/DRIFT_CHECK_RULE_SYNC_PASS.md`

## Drift Rule Added

Added under `governance` in `docs/PROJECT_STATE.yml`:

- `drift_check_rule`
- `drift_check_must_verify` checklist:
  - CURRENT_STATE / ROADMAP / PROJECT_STATE alignment
  - AUDIT_INDEX recency
  - no duplicated facts
  - one canonical "next step"
  - TRUTH_INDEX / PROTECTED_SURFACES update need
  - no silent scope expansion
  - old docs not misleading future prompts
- `drift_found_action`:
  - run a small docs-only cleanup/sync pass before implementation continues

## Scope Confirmation

Docs-only change. No source/build/runtime/CI changes.

## Validation

- `py -3 tools/validate_project_state.py`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`

