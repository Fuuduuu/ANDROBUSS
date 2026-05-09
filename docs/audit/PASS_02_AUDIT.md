# PASS_02_AUDIT

Pass: PASS 02 — REPO_SKELETON_AND_BUILD  
Type: skeleton

## Module List Created

- `app`
- `core-domain`
- `core-gtfs`
- `core-routing`
- `data-local`
- `data-remote`
- `feature-map`
- `feature-search`
- `feature-stop-board`
- `feature-route-detail`
- `feature-favourites`
- `feature-alerts`
- `city-adapters`

## Architecture Checks

- Pure Kotlin modules confirmed: `core-domain`, `core-gtfs`, `core-routing` use Kotlin JVM plugin only.
- Android modules confirmed: app + feature modules + data-local + data-remote + city-adapters use Android plugins.
- Module dependency direction follows `docs/CODEBASE_IMPACT_MAP.md`.

## Forbidden Logic Checks

- No GTFS parser implementation added.
- No domain model classes added.
- No Room schema/entities/DAOs/AppDatabase added.
- No ViewModels or feature screens added.
- No WorkManager job implementation added.
- No Retrofit/OkHttp client implementation added.
- No city adapter logic added.
- No ticketing/backend logic added.

## Dependency Checks

- Maps SDK not added.
- Room schema not added.
- GTFS parser code not added.

## CI

- `.github/workflows/ci.yml` added.
- CI runs `./gradlew build` and `./gradlew lint`.

## Build and Lint Results

- `./gradlew build`: PASS
- `./gradlew lint`: PASS
- `./gradlew projects`: PASS
- `./gradlew dependencies`: PASS

Java environment used for final validation:

- `JAVA_HOME`: `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`
- `java -version`: `openjdk version "17.0.19" 2026-04-21` (Temurin 17.0.19+10)
- `.\gradlew.bat --version`: Gradle 8.10.2, Launcher JVM 17.0.19 (Eclipse Adoptium)

## Validation Finalization Notes

- `local.properties` is ignored by `.gitignore` and not present in Git status output.
- Pure Kotlin core modules confirmed again: `core-domain`, `core-gtfs`, `core-routing` do not apply Android plugin.
- No Maps SDK dependency declarations found in Gradle config files.
- No Retrofit/OkHttp client implementation files added.
- No Room schema/entities/DAOs/AppDatabase files added.
- No GTFS parser implementation files added.
- No ViewModel classes, Worker jobs, or city-specific adapter logic files added.

Expected CI behavior:

- GitHub Actions workflow sets Java 17 and runs `./gradlew build` + `./gradlew lint`.
