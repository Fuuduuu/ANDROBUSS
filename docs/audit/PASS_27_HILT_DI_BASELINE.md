# PASS_27_HILT_DI_BASELINE

## Objective

Introduce app-level Hilt DI baseline for bootstrap dependencies before ViewModel/UI work, without changing parser/routing/search logic or Room schema.

## Repo guard result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `7f2669abbd1e74dfe6918937b7614efffff0be3b`
- Working tree before edits: clean
- `py -3 tools/validate_project_state.py`: passed

## DI graph implemented

- App-owned Hilt modules provide:
  - `AppDatabase`
  - `FeedSnapshotDao`
  - `FeedSnapshotImporter`
  - `RoomDomainFeedSnapshotLoader`
  - `RoomDomainFeedSnapshotProvider`
  - `FeedBootstrapLoader`
- `AndrobussApplication` is now `@HiltAndroidApp` and launches bootstrap using injected `FeedBootstrapLoader`.

## Hilt scopes

- All app DI providers are `@Singleton` and installed in `SingletonComponent`.

## Scope confirmations

- No ViewModel added.
- No Compose/UI added.
- No WorkManager/network/realtime changes.
- No Room schema/DAO/entity changes.
- No parser/routing/search logic changes.
- No Hilt annotations in `core-domain` / `core-gtfs` / `core-routing` / `feature-search`.

## Validation result

- `py -3 tools/validate_project_state.py` passed (`last_accepted_commit` warning only).
- `.\gradlew.bat :app:test` passed.
- `.\gradlew.bat :data-local:test` passed.
- `.\gradlew.bat :core-domain:test` passed.
- `.\gradlew.bat :feature-search:test` passed.
- `.\gradlew.bat detekt` passed.
- `.\gradlew.bat build` passed.
- `.\gradlew.bat test` passed.
- `git diff --check` passed (LF/CRLF warnings only).
- Boundary checks passed:
  - no parser types in `app/src/main` or `data-local/src/main`,
  - no `@Serializable` in `core-domain`,
  - no `allowMainThreadQueries` in `data-local`,
  - no Hilt annotations in `core-domain` / `core-gtfs` / `core-routing` / `feature-search`.

## Files changed

- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/gradle.lockfile`
- `app/src/main/kotlin/ee/androbus/app/AndrobussApplication.kt`
- `app/src/main/kotlin/ee/androbus/app/di/DatabaseModule.kt`
- `app/src/main/kotlin/ee/androbus/app/di/FeedModule.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AUDIT_INDEX.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/audit/PASS_27_HILT_DI_BASELINE.md`

## Drift counter note

- Drift counter advanced to `5 / 5`; a docs-only drift/boundary check is the recommended next governance step.

## Next recommended pass

- `PASS_AUTO_03_DRIFT_AND_BOUNDARY_CHECK`
