package compat

import org.gradle.api.Project

/**
 * Registers the Android SDK source-compatibility tasks for a module:
 *
 * - `updateAndroidSdkApi` downloads the given `api.txt` files from firebase-android-sdk (ref from the
 *   `androidSdk.apiRef` Gradle property) into `api/android-sdk/`;
 * - `androidSourceCompatDump` writes `api/android-sdk-compat.txt` from `api/android/<module>.api`;
 * - `androidSourceCompatCheck` (part of `check`) fails if that report is out of date.
 *
 * `api/android-sdk/exclusions.txt` may list members (`com.google.firebase.FirebaseApp#getApplicationContext`) or
 * classes (`com.google.firebase.provider.*`) that are intentionally not mirrored, and `api/android-sdk/typealiases.txt`
 * the classes that are `actual typealias`es to the relocated Android SDK classes (invisible in the dump but available).
 * `//` starts a comment in both files.
 */
fun Project.registerAndroidSourceCompat(vararg androidSdkApiFiles: String) {
    val sdkDir = layout.projectDirectory.dir("api/android-sdk")
    val ref = providers.gradleProperty("androidSdk.apiRef").orElse("main")
    tasks.register("updateAndroidSdkApi", UpdateAndroidSdkApiTask::class.java) {
        group = "api"
        description = "Downloads the Firebase Android SDK api.txt files this module is compared against"
        sources.set(androidSdkApiFiles.toList())
        this.ref.set(ref)
        outputDirectory.set(sdkDir)
    }
    val configure: AndroidSourceCompatTask.(Boolean) -> Unit = { checkMode ->
        group = "api"
        moduleName.set(project.name)
        bcvDump.set(layout.projectDirectory.file("api/android/${project.name}.api"))
        this.androidSdkApiFiles.from(fileTree(sdkDir) { include("*.api.txt") })
        sdkDir.file("exclusions.txt").takeIf { it.asFile.exists() }?.let { exclusionsFile.set(it) }
        sdkDir.file("typealiases.txt").takeIf { it.asFile.exists() }?.let { typealiasesFile.set(it) }
        check.set(checkMode)
        reportFile.set(layout.projectDirectory.file("api/android-sdk-compat.txt"))
        mustRunAfter("androidApiDump")
    }
    tasks.register("androidSourceCompatDump", AndroidSourceCompatTask::class.java) {
        description = "Writes api/android-sdk-compat.txt: which Firebase Android SDK APIs this module provides"
        configure(false)
    }
    val check = tasks.register("androidSourceCompatCheck", AndroidSourceCompatTask::class.java) {
        description = "Fails if api/android-sdk-compat.txt is out of date"
        configure(true)
        outputs.upToDateWhen { false }
    }
    tasks.matching { it.name == "check" }.configureEach { dependsOn(check) }
}
