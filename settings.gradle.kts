include(
    "firebase-analytics",
    "firebase-app",
    "firebase-auth",
    "firebase-common",
    "firebase-common-internal",
    "firebase-config",
    "firebase-crashlytics",
    "firebase-database",
    "firebase-firestore",
    "firebase-functions",
    "firebase-installations",
    "firebase-messaging",
    "firebase-perf",
    "firebase-storage",
    "test-utils"
)

// Relocated republications of the Firebase Android SDK artifacts (see android-sdk/README.md).
// Project names carry a "relocated-" prefix so they never collide with the modules of the same artifact id.
file("android-sdk").listFiles()
    ?.filter { it.isDirectory && File(it, "build.gradle.kts").exists() }
    ?.sortedBy { it.name }
    ?.forEach { dir ->
        val path = ":android-sdk:relocated-${dir.name}"
        include(path)
        project(path).projectDir = dir
    }

pluginManagement {
    includeBuild("convention-plugin-test-option")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
