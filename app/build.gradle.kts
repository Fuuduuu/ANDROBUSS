plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)

    implementation(project(":feature-map"))
    implementation(project(":feature-search"))
    implementation(project(":feature-stop-board"))
    implementation(project(":feature-route-detail"))
    implementation(project(":feature-favourites"))
    implementation(project(":feature-alerts"))

    implementation(project(":data-local"))
    implementation(project(":data-remote"))
    implementation(project(":city-adapters"))
}
