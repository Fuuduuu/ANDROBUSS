# PASS_G01_GOVERNANCE_BOOTSTRAP

## Goal
Bootstrap compact governance docs so future passes can use machine-readable state, invariant registry, and a standard debug/pass workflow without changing runtime behavior.

## Files changed

Created:
- `docs/PROJECT_STATE.yml`
- `docs/INVARIANTS.md`
- `docs/DEBUG_PLAYBOOK.md`
- `docs/PASS_TEMPLATE.md`
- `docs/audit/PASS_G01_GOVERNANCE_BOOTSTRAP.md`

Updated (minimal):
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`

## Files intentionally not changed

- All Kotlin/source files
- All Gradle/build files
- CI/workflow files (`.github/`)
- Room/DAO/AppDatabase/provider/runtime code
- Existing large docs history/archives

## Invariants added

Added `docs/INVARIANTS.md` with:
- INV-001..INV-009
- Existing test links where available
- `TODO_TEST_REQUIRED` markers where coverage is not yet explicit

## Validation commands

- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`

## Risks

- `PROJECT_STATE.yml` is not yet CI-validated (planned governance follow-up).
- Core-domain Android-free guarantee still lacks a dedicated explicit test marker.
- Feed contract move and scoped Room key enforcement remain implementation work for PASS 22B.

## Recommended next governance pass

- `G02 — PROJECT_STATE_VALIDATION_HOOKS`
