plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Loaded here (rather than compileOnly) so the helpers in utils/ can use the Kotlin Gradle plugin API at runtime;
    // the Kotlin plugin needs the Android plugin in the same class loader.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.asm.commons)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}
