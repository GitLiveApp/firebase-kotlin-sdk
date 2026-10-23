plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly: at runtime the plugin uses the Kotlin Gradle plugin that the main build already loads.
    compileOnly(libs.kotlin.gradle.plugin)
}
