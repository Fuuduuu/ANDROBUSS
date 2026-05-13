plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvmToolchain(17)
}

detekt {
    config.setFrom(
        "$rootDir/config/detekt/detekt-base.yml",
        "$rootDir/config/detekt/detekt-core-domain.yml",
    )
    buildUponDefaultConfig = false
}

dependencies {
    testImplementation(kotlin("test"))
}
