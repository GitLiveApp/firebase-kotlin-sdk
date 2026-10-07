package utils

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The default KMP hierarchy plus two intermediate source sets used by the `com.google.firebase` compatibility layer:
 *
 * - `nonJvmMain` (js, apple): shared pure-Kotlin implementations of Android SDK types that Android/JVM get from the
 *   real Play Services classes (e.g. `com.google.android.gms.tasks.Task`).
 * - `nonAndroidMain` (jvm, js, apple): shared pure-Kotlin implementations for modules that `firebase-java-sdk` does not
 *   provide at all, so the JVM gets real classes rather than header stubs (e.g. `RemoteMessage`).
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.applyFirebaseHierarchy() {
    applyDefaultHierarchyTemplate {
        common {
            group("nonJvm") {
                withJs()
                group("apple") {
                    withApple()
                }
            }
            group("nonAndroid") {
                withJvm()
                withJs()
                group("apple") {
                    withApple()
                }
            }
        }
    }
}
