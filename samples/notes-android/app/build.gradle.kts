// The Android app module. Lines marked "KMP:" change when the app is ported: the Firebase dependencies move to the shared
// module's commonMain, as the Firebase Kotlin SDK modules of the same names, and this module depends on `:shared`.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // The Google Services plugin is not applied: Firebase is configured in code (see NotesFirebase), which keeps the
    // sample self-contained. An app that applies it keeps doing so here, on the Android app, after the port.
}

android {
    namespace = "dev.gitlive.notes"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.gitlive.notes"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // KMP: implementation(project(":shared")) replaces the Firebase dependencies below, which move to
    // shared/build.gradle.kts as `commonMain.dependencies { implementation("dev.gitlive:firebase-<module>:3.0.0-beta1") }`,
    // one per module, without the BoM. On Android each Firebase Kotlin SDK module depends on the same
    // com.google.firebase:firebase-<module>, so the app's classpath is unchanged.
    implementation(platform("com.google.firebase:firebase-bom:34.18.0")) // KMP: remove
    implementation("com.google.firebase:firebase-analytics") // KMP: dev.gitlive:firebase-analytics in shared commonMain
    implementation("com.google.firebase:firebase-auth") // KMP: dev.gitlive:firebase-auth
    implementation("com.google.firebase:firebase-config") // KMP: dev.gitlive:firebase-config
    implementation("com.google.firebase:firebase-crashlytics") // KMP: dev.gitlive:firebase-crashlytics
    implementation("com.google.firebase:firebase-firestore") // KMP: dev.gitlive:firebase-firestore
    implementation("com.google.firebase:firebase-functions") // KMP: dev.gitlive:firebase-functions
    implementation("com.google.firebase:firebase-installations") // KMP: dev.gitlive:firebase-installations
    implementation("com.google.firebase:firebase-messaging") // KMP: dev.gitlive:firebase-messaging
    implementation("com.google.firebase:firebase-perf") // KMP: dev.gitlive:firebase-perf
    implementation("com.google.firebase:firebase-storage") // KMP: dev.gitlive:firebase-storage

    // KMP: remove; dev.gitlive:firebase-app provides kotlinx.coroutines.tasks.await() in common code (and depends on this
    // library on Android, so Android keeps the real one).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
