plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Loaded here (rather than compileOnly) so the relocation plugins can use the AGP, KGP and publishing APIs at runtime.
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.vanniktech.publish.plugin)
    implementation(libs.asm.commons)
    implementation(libs.kotlin.metadata.jvm)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("relocatedAndroidArtifact") {
            id = "dev.gitlive.relocated-android-artifact"
            implementationClass = "relocate.RelocatedAndroidArtifactPlugin"
        }
        create("relocatedJvmArtifact") {
            id = "dev.gitlive.relocated-jvm-artifact"
            implementationClass = "relocate.RelocatedJvmArtifactPlugin"
        }
    }
}

// kotlin-metadata-jvm matches the project's Kotlin version, which is newer than the Kotlin embedded in Gradle.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xskip-metadata-version-check")
}
