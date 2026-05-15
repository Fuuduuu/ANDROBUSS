plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    id("io.gitlab.arturbosch.detekt")
    id("kotlin-kapt")
}

android {
    namespace = "ee.androbus.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "ee.androbus.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

detekt {
    config.setFrom(
        "$rootDir/config/detekt/detekt-base.yml",
        "$rootDir/config/detekt/detekt-app.yml",
    )
    buildUponDefaultConfig = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    setSource(files("src/main/java", "src/main/kotlin"))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/resources/**")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.room.runtime)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(project(":core-domain"))
    implementation(project(":core-routing"))

    implementation(project(":feature-map"))
    implementation(project(":feature-search"))
    implementation(project(":feature-stop-board"))
    implementation(project(":feature-route-detail"))
    implementation(project(":feature-favourites"))
    implementation(project(":feature-alerts"))

    implementation(project(":data-local"))
    implementation(project(":data-remote"))
    implementation(project(":city-adapters"))

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(kotlin("test"))
    testImplementation(project(":data-local"))
    testImplementation(project(":core-domain"))

    kapt(libs.hilt.compiler)
}
