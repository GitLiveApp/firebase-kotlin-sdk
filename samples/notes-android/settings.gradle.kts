// The build of the Android app. Lines marked "KMP:" are the ones that change when the app is ported to Kotlin
// Multiplatform with the Firebase Kotlin SDK; everything else stays as it is.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "notes-android"

include(":app") // KMP: include(":app", ":shared"); the Firebase code moves to the shared module
