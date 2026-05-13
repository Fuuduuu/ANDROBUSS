# PASS_AUTO_02_DEPENDENCY_LOCKING

## Objective

Enable Gradle dependency locking across the multi-module project so dependency drift is visible and reviewable in git diffs/CI.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS` (OK)
- Branch: `main` (OK)
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git` (OK)
- HEAD at start: `241439a9716294c472cd6ec9f881647810019862` (OK)
- Working tree clean before edits: yes (OK)
- `py -3 tools/validate_project_state.py`: passed (warning-only stale commit allowed)

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/PROJECT_STATE.yml`
- `docs/INVARIANTS.md`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- all module `build.gradle.kts`
- `.github/workflows/ci.yml`

## Gradle Locking Change

Root `build.gradle.kts` updated with:

- `allprojects { dependencyLocking { lockAllConfigurations() } }`

No dependency version updates were made intentionally.

## Generated Lockfiles

Generated via:

- `.\gradlew.bat dependencies --write-locks`
- `.\gradlew.bat build --write-locks`
- `.\gradlew.bat :data-local:test --write-locks`
- `.\gradlew.bat :data-local:generateDebugUnitTestLintModel --write-locks`
- `.\gradlew.bat :data-local:dependencies --configuration debugUnitTestRuntimeClasspath --write-locks`
- `.\gradlew.bat :data-local:dependencies --configuration releaseUnitTestRuntimeClasspath --write-locks`
- `.\gradlew.bat :data-local:build --write-locks --update-locks org.jetbrains.kotlin:kotlin-stdlib-jdk7`

Produced lockfiles:

- `settings-gradle.lockfile`
- `app/gradle.lockfile`
- `city-adapters/gradle.lockfile`
- `core-domain/gradle.lockfile`
- `core-gtfs/gradle.lockfile`
- `core-routing/gradle.lockfile`
- `data-local/gradle.lockfile`
- `data-remote/gradle.lockfile`
- `feature-alerts/gradle.lockfile`
- `feature-favourites/gradle.lockfile`
- `feature-map/gradle.lockfile`
- `feature-route-detail/gradle.lockfile`
- `feature-search/gradle.lockfile`
- `feature-stop-board/gradle.lockfile`

## CI Update

No CI workflow change required in this pass. Existing build/test/detekt pipeline remains unchanged.

## Validation

Commands executed:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat :data-local:test`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `.\gradlew.bat test`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `git diff --name-only | findstr /R "\.kt$"`

## No Source-Code Change Confirmation

No Kotlin source files were modified.

## Risks / Unknowns

- Dependency updates now require explicit lock refresh; accidental lock churn is possible if builds are run under materially different resolution conditions.
- Android Gradle unit-test/lint classpath locking needed targeted `data-local` lock refresh commands before full-root build stabilized.
- `PROJECT_STATE.yml` still reports a stale last accepted commit warning (non-blocking and expected by current validator policy).

## Recommended Next Pass

- `PASS 26 — REAL_RAKVERE_FEED_ASSET_OR_HILT_BOOTSTRAP_DECISION`
