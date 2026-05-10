# PASS_G02_PROJECT_STATE_VALIDATION_HOOKS

## Goal
Add a minimal local/CI validation hook for `docs/PROJECT_STATE.yml` schema without changing product/runtime behavior.

## Files changed

Created:
- `tools/validate_project_state.py`
- `docs/audit/PASS_G02_PROJECT_STATE_VALIDATION_HOOKS.md`

Updated (minimal):
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `.github/workflows/ci.yml`

## Files intentionally not changed

- Kotlin/source files
- Gradle/build files
- Room/DAO/AppDatabase/runtime feed provider code
- Parser/routing/search/UI behavior
- Docs archive/hygiene moves

## Validator rules

`tools/validate_project_state.py` checks:
- required top-level sections exist
- `project.name`, `project.branch`, `project.remote` fixed values
- non-empty `last_accepted_commit` and `last_known_good_commit`
- non-empty `validation.required_commands`
- non-empty `invariants.critical`
- each critical invariant has `id`, `rule`, and `test`

## Failure behavior

- Hard fail (`exit 1`) on missing schema elements or required values.
- Commit hash drift is warning-only:
  - `WARNING: PROJECT_STATE last_accepted_commit may be stale.`
  - validation still exits `0`.

## Validation commands

- `python tools/validate_project_state.py`
- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`

## Risks

- Parser is intentionally lightweight/regex-based and schema-specific (not full YAML parsing).
- `project.last_accepted_commit` may lag during in-progress work by design; script only warns.

## Recommended next pass

- `PASS 22B — FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_WITH_SCOPED_KEYS`
