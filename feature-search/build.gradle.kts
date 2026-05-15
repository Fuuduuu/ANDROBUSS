plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "ee.androbus.feature.search"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

detekt {
    config.setFrom(
        "$rootDir/config/detekt/detekt-base.yml",
        "$rootDir/config/detekt/detekt-feature-search.yml",
    )
    buildUponDefaultConfig = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    setSource(files("src/main/java", "src/main/kotlin"))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/resources/**")
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-routing"))
    implementation(project(":city-adapters"))
    implementation(platform(libs.compose.bom))

    testImplementation(kotlin("test"))
    testImplementation(project(":core-gtfs"))
}
