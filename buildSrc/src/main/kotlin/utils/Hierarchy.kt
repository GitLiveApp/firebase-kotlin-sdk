package utils

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The default KMP hierarchy plus two intermediate source sets used by the `com.google.firebase` compatibility layer:
 *
 * - `nonJsMain` (android, jvm, apple): the targets whose Firebase SDKs exchange Kotlin collections, where
 *   `FirebaseFormat`'s plain values are the Kotlin ones (`PlainValues.nonJs.kt`); wasmJs binds to the JS SDK like js,
 *   so it is outside.
 * - `nonJvmMain` (js, wasmJs, apple): shared pure-Kotlin implementations of Android SDK types that Android/JVM get from
 *   the real Play Services classes (e.g. `com.google.android.gms.tasks.Task`).
 *
 * An API the JS SDK lacks is not split out into a source set: it is declared in common and its JS actual carries the
 * `@dev.gitlive.firebase.UnsupportedOnJs` marker.
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.applyFirebaseHierarchy() {
    applyDefaultHierarchyTemplate {
        common {
            group("nonJs") {
                withAndroidTarget()
                withJvm()
                group("apple") {
                    withApple()
                }
            }
            group("nonJvm") {
                withJs()
                withWasmJs()
                group("apple") {
                    withApple()
                }
            }
        }
    }
}
