plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "ee.androbus.data.local"
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

detekt {
    config.setFrom(
        "$rootDir/config/detekt/detekt-base.yml",
        "$rootDir/config/detekt/detekt-data-local.yml",
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

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.room.compiler)

    testImplementation(kotlin("test"))
    testImplementation(project(":core-gtfs"))
    testImplementation(project(":feature-search"))
    testImplementation(project(":core-routing"))
    testImplementation(libs.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}
