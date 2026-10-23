/**
 * Adds the Firebase Apple SDK to a module through the Kotlin Gradle Plugin's SwiftPM import:
 *
 * ```kotlin
 * plugins {
 *     kotlin("multiplatform")
 *     id("dev.gitlive.firebase-swiftpm")
 * }
 *
 * kotlin {
 *     iosArm64()
 *     // other targets
 *
 *     firebaseSwiftPM {
 *         products("FirebaseAuth")
 *     }
 * }
 * ```
 *
 * The Firebase version (`firebase-spm`) and the deployment targets come from `gradle/libs.versions.toml`.
 */
extensions.create("firebaseSwiftPM", FirebaseSwiftPMExtension::class.java, project)
