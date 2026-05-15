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
        "$rootDir/config/detekt/detekt-city-adapters.yml",
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
    testImplementation(kotlin("test"))
}
