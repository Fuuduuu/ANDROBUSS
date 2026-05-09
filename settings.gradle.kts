pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ANDROBUSS"

include(
    ":app",
    ":core-domain",
    ":core-gtfs",
    ":core-routing",
    ":data-local",
    ":data-remote",
    ":feature-map",
    ":feature-search",
    ":feature-stop-board",
    ":feature-route-detail",
    ":feature-favourites",
    ":feature-alerts",
    ":city-adapters"
)
