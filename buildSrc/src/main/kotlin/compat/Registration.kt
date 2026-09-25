package compat

import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.attributes.Attribute

/**
 * Registers the Android SDK source-compatibility tasks for a module:
 *
 * - `updateAndroidSdkApi` downloads the given `api.txt` files from firebase-android-sdk (ref from the
 *   `androidSdk.apiRef` Gradle property) into `api/android-sdk/`, and generates the `api.txt` of the [generated] modules,
 *   which are not open source, from their published classes (see [AndroidSdkApiJarDumper]);
 * - `androidSourceCompatDump` writes `api/android-sdk-compat.txt` from `api/android/<module>.api`;
 * - `androidSourceCompatCheck` (part of `check`) fails if that report is out of date.
 *
 * `api/android-sdk/exclusions.txt` may list members (`com.google.firebase.FirebaseApp#getApplicationContext`), single
 * overloads (`com.google.firebase.Timestamp#<init>(Date)`) or classes (`com.google.firebase.provider.*`) that are
 * intentionally not mirrored; `//` starts a comment. Omitted members still count against the module's percentage unless
 * the comment contains `@hide` (hidden in the Android SDK), `@platform` (the signature involves an Android/JVM-only type)
 * or `@unavailable` (the feature does not exist in the pinned iOS or JS SDK).
 * The Android header stubs (removed from the compiled output, see `utils.stripHeaderStubs`) are read from the dump that
 * the Android compilation writes to `build/header-stubs/`.
 */
fun Project.registerAndroidSourceCompat(vararg androidSdkApiFiles: String, generated: List<GeneratedAndroidSdkApi> = emptyList()) {
    val sdkDir = layout.projectDirectory.dir("api/android-sdk")
    val ref = providers.gradleProperty("androidSdk.apiRef").orElse("main")
    val generateTasks = generated.map { api ->
        tasks.register("generateAndroidSdkApi${api.name.split('-').joinToString("") { it.replaceFirstChar(Char::uppercase) }}", GenerateAndroidSdkApiTask::class.java) {
            group = "api"
            description = "Generates api/android-sdk/${api.name}.api.txt from the published classes of ${api.artifacts.joinToString()}"
            val artifacts = provider {
                configurations.getByName("releaseCompileClasspath").incoming.artifactView {
                    attributes.attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
                }.artifacts.filter { artifact -> (artifact.id.componentIdentifier as? ModuleComponentIdentifier)?.let { "${it.group}:${it.module}" in api.artifacts } == true }
            }
            jars.from(artifacts.map { it.map { artifact -> artifact.file } })
            coordinates.set(artifacts.map { it.map { artifact -> artifact.id.componentIdentifier.displayName }.sorted() })
            packages.set(api.packages)
            header.set("generated from the published classes (the module is not open source) of")
            outputFile.set(sdkDir.file("${api.name}.api.txt"))
        }
    }
    tasks.register("updateAndroidSdkApi", UpdateAndroidSdkApiTask::class.java) {
        group = "api"
        description = "Downloads the Firebase Android SDK api.txt files this module is compared against"
        sources.set(androidSdkApiFiles.toList())
        this.ref.set(ref)
        outputDirectory.set(sdkDir)
        dependsOn(generateTasks)
    }
    val configure: AndroidSourceCompatTask.(Boolean) -> Unit = { checkMode ->
        group = "api"
        moduleName.set(project.name)
        bcvDump.set(layout.projectDirectory.file("api/android/${project.name}.api"))
        this.androidSdkApiFiles.from(fileTree(sdkDir) { include("*.api.txt") })
        sdkDir.file("exclusions.txt").takeIf { it.asFile.exists() }?.let { exclusionsFile.set(it) }
        headerStubDumps.from(layout.buildDirectory.file("header-stubs/compileReleaseKotlinAndroid.api"))
        dependsOn(provider { tasks.findByName("compileReleaseKotlinAndroid") })
        check.set(checkMode)
        reportFile.set(layout.projectDirectory.file("api/android-sdk-compat.txt"))
        mustRunAfter("androidApiDump")
        mustRunAfter(generateTasks)
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

/**
 * A Firebase Android SDK module that is not open source: its `api/android-sdk/<name>.api.txt` is generated from the
 * classes of [artifacts] (`group:module`) under [packages] (internal names, e.g. `com/google/firebase/auth/`).
 */
data class GeneratedAndroidSdkApi(val name: String, val artifacts: List<String>, val packages: List<String>)
