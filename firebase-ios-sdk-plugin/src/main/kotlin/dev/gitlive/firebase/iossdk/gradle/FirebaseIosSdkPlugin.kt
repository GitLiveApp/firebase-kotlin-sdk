package dev.gitlive.firebase.iossdk.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the `firebaseIosSdk { }` extension used inside a module's `kotlin { }` block. All the actual
 * download/cinterop/link wiring happens lazily when that block is evaluated (see [FirebaseIosSdkExtension]) -
 * this plugin's only job is to make the extension available to look up, defaulted to the Firebase iOS SDK
 * version this plugin itself was built against ([FIREBASE_IOS_SDK_VERSION]/[FIREBASE_IOS_SDK_SHA256], generated
 * at build time from this repo's own `gradle/libs.versions.toml`).
 */
class FirebaseIosSdkPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("firebaseIosSdk", FirebaseIosSdkExtension::class.java, project)
        extension.version.convention(FIREBASE_IOS_SDK_VERSION)
        extension.checksum.convention(FIREBASE_IOS_SDK_SHA256)
    }
}
