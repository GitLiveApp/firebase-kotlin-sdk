package utils

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The default KMP hierarchy plus two intermediate source sets used by the `com.google.firebase` compatibility layer:
 *
 * - `nonJsMain` (android, jvm, apple): Android SDK APIs that can be mapped onto the mobile/desktop SDKs but not onto
 *   the JS SDK live here, so android+ios projects keep source compatibility for them.
 * - `nonJvmMain` (js, apple): shared pure-Kotlin implementations of Android SDK types that Android/JVM get from the
 *   real Play Services classes (e.g. `com.google.android.gms.tasks.Task`).
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
                group("apple") {
                    withApple()
                }
            }
        }
    }
}
