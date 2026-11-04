package dev.gitlive.firebase.iossdk.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File
import javax.inject.Inject

/**
 * Configures a module to link against frameworks from the official Firebase iOS SDK release zip, for every
 * Apple Kotlin/Native target already declared in the enclosing `kotlin { }` block - without CocoaPods, which
 * no longer carries Firebase 13+.
 *
 * ```
 * kotlin {
 *     iosArm64()
 *     iosSimulatorArm64()
 *
 *     firebaseIosSdk {
 *         framework(FirebaseIosFramework.Core)
 *     }
 * }
 * ```
 *
 * [version] and [checksum] default to exactly the Firebase iOS SDK release this plugin itself was built and
 * tested against - there's nothing to configure for the common case. They're exposed as overridable properties
 * only for advanced use (e.g. trying a newer Firebase release before this plugin has been updated to match).
 */
abstract class FirebaseIosSdkExtension @Inject constructor(private val project: Project) {

    abstract val version: Property<String>
    abstract val checksum: Property<String>

    private val requests = mutableListOf<FrameworkRequest>()

    /** @see framework */
    fun framework(
        framework: FirebaseIosFramework,
        packageName: String = framework.frameworkName,
        linkOnly: Boolean = false,
        productFolder: String = framework.productFolder,
    ) = framework(framework.frameworkName, packageName, linkOnly, productFolder)

    /**
     * Requests that [name] (e.g. "FirebaseAuth") be made available to every Apple target in this module. Every
     * other `.xcframework` Firebase bundled alongside it in the same release-zip product folder - its
     * transitive dependencies (GoogleUtilities, FirebaseCoreInternal, and so on) - is linked too, since that is
     * how Firebase itself packages a "product" for manual installation; only [name] gets Kotlin bindings
     * generated for it.
     *
     * Prefer the [FirebaseIosFramework] overload for any framework it lists; this one exists for a framework
     * Firebase ships that isn't in that enum yet.
     *
     * @param packageName the Kotlin package the generated cinterop bindings are placed under. Defaults to
     *   [name] itself. Callers migrating off the `kotlin.native.cocoapods` plugin should pass the exact
     *   `cocoapods.<PodName>` package CocoaPods used to generate, so existing source code - and any public API
     *   that exposes these types - keeps compiling unmodified.
     * @param linkOnly when true, only [name]'s own binary is linked and no cinterop/package is generated for it
     *   (no sibling discovery either) - [packageName] is ignored. An escape hatch for a framework that needs
     *   linking from outside any other request's product folder; Firebase's own FirebaseFirestore/
     *   FirebaseFirestoreInternal pair - the one case `useInteropBindingFrom` handled under the CocoaPods plugin
     *   - doesn't actually need this, since both live in the same product folder already.
     * @param productFolder overrides which `Firebase/<productFolder>/` folder in the release zip this
     *   framework is read from, for the rare case Firebase reorganizes the zip layout in a future release.
     */
    fun framework(
        name: String,
        packageName: String = name,
        linkOnly: Boolean = false,
        productFolder: String = name,
    ) {
        requests += FrameworkRequest(name, packageName, linkOnly, productFolder)
    }

    /** Called once, after the `firebaseIosSdk { }` configure block has run, to do the actual Gradle wiring. */
    internal fun wire(kotlin: KotlinMultiplatformExtension) {
        check(requests.isNotEmpty()) { "firebaseIosSdk { } was configured without requesting any framework() " }

        val extractedDir = FirebaseIosSdkDownloader.ensureExtracted(
            project.gradle.gradleUserHomeDir,
            version.get(),
            checksum.get(),
        )

        val appleTargets = kotlin.targets
            .withType(KotlinNativeTarget::class.java)
            .mapNotNull { target -> appleSliceForTarget(target.targetName)?.let { target to it } }

        for ((target, slice) in appleTargets) {
            swiftTargetTriple(target.targetName)?.let { triple ->
                val searchPaths = swiftRuntimeLibrarySearchPaths(triple)
                target.binaries.all {
                    searchPaths.forEach { path -> linkerOpts("-L$path") }
                }
            }
            for (request in requests) {
                wireFrameworkForTarget(extractedDir, target, slice, request)
            }
        }
    }

    private fun wireFrameworkForTarget(
        extractedDir: File,
        target: KotlinNativeTarget,
        slice: AppleSlice,
        request: FrameworkRequest,
    ) {
        if (request.linkOnly) {
            val frameworkDir = File(
                extractedDir,
                "Firebase/${request.productFolder}/${request.name}.xcframework/${slice.libraryIdentifier}",
            )
            target.binaries.all {
                linkerOpts("-F${frameworkDir.absolutePath}", "-framework", request.name)
            }
            return
        }

        val productDir = File(extractedDir, "Firebase/${request.productFolder}")
        check(productDir.isDirectory) {
            "firebaseIosSdk: expected a Firebase product folder at $productDir for framework '${request.name}' " +
                "(version ${version.get()}) - Firebase may have reorganized the release zip's layout; override " +
                "productFolder for this framework."
        }

        // Link every xcframework Firebase bundled alongside the requested one - this *is* its transitive
        // dependency graph (GoogleUtilities, FirebaseCoreInternal, etc. for FirebaseCore's own folder, and
        // similarly abseil/grpc/leveldb/nanopb for FirebaseFirestore's), which Firebase's own zip packaging
        // already resolved, matching what pod install depended on previously.
        val siblingFrameworks = productDir.listFiles { file -> file.isDirectory && file.name.endsWith(".xcframework") }
            .orEmpty()
            .map { it.name.removeSuffix(".xcframework") }
            .filter { File(productDir, "$it.xcframework/${slice.libraryIdentifier}").isDirectory }
            .sorted()
        check(siblingFrameworks.contains(request.name)) {
            "firebaseIosSdk: '${request.name}.xcframework' with slice '${slice.libraryIdentifier}' was not " +
                "found under $productDir - override productFolder, or this Firebase release may not support " +
                "the '${target.targetName}' target."
        }

        val searchPaths = siblingFrameworks.joinToString(" ") {
            "-F${File(productDir, "$it.xcframework/${slice.libraryIdentifier}").absolutePath}"
        }
        val linkFlags = siblingFrameworks.joinToString(" ") { "-framework $it" }

        // Keyed by target as well as framework name: every Apple target shares this module's single build
        // directory, so a path keyed only by framework name would have each target's configuration-time write
        // overwrite the last, leaving every target's cinterop task reading whichever target happened to be
        // configured last.
        val generatedDefFile = project.layout.buildDirectory
            .dir("firebaseIosSdk/defs")
            .get()
            .asFile
            .apply { mkdirs() }
            .resolve("${request.name}-${target.targetName}.def")
        generatedDefFile.writeText(
            """
            language = Objective-C
            package = ${request.packageName}
            modules = ${request.name}
            compilerOpts = $searchPaths -fmodules
            linkerOpts = $searchPaths $linkFlags
            """.trimIndent() + "\n",
        )

        target.compilations.getByName("main").cinterops.create(request.name) {
            defFile(generatedDefFile)
        }
    }
}

private data class FrameworkRequest(
    val name: String,
    val packageName: String,
    val linkOnly: Boolean,
    val productFolder: String,
)

/** Entry point, mirroring how `kotlin.native.cocoapods`'s own `cocoapods { }` block is used. */
fun KotlinMultiplatformExtension.firebaseIosSdk(configure: FirebaseIosSdkExtension.() -> Unit) {
    val extension = project.extensions.getByType(FirebaseIosSdkExtension::class.java)
    extension.configure()
    extension.wire(this)
}
