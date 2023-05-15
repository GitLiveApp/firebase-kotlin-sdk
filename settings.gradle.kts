include(
    "firebase-app",
    "firebase-auth",
    "firebase-common",
    "firebase-config",
    "firebase-database",
    "firebase-firestore",
    "firebase-functions",
    "firebase-installations",
    "firebase-perf",
    "firebase-crashlytics"
)

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion
        kotlin("native.cocoapods") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}
