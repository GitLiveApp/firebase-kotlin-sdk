package relocate

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipFile

/**
 * Takes an AAR or JAR of the Firebase Android SDK and produces the same artifact with all
 * `com.google.firebase.*` classes moved to [RELOCATED_PACKAGE_PREFIX], so that this SDK can declare its own
 * `com.google.firebase.*` classes on Android without clashing.
 */
abstract class RelocateArtifactTask : DefaultTask() {

    /** The original AAR or JAR (a single file). */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val originalArtifact: ConfigurableFileCollection

    /** Dotted package prefixes (without trailing dot) to relocate, and their replacement. */
    @get:Input
    abstract val prefixMappings: MapProperty<String, String>

    /** For AARs: the android namespace declared in the original manifest, verified before relocating. */
    @get:Input
    @get:Optional
    abstract val expectedNamespace: Property<String>

    /** Drop `@kotlin.Metadata` instead of remapping it (the relocated classes are then seen as Java classes). */
    @get:Input
    abstract val stripKotlinMetadata: Property<Boolean>

    /** Keep only the classes under the relocated packages (see [RelocatedArtifactExtension.extractRelocatedClassesOnly]). */
    @get:Input
    abstract val onlyRelocatedClasses: Property<Boolean>

    @get:OutputFile
    abstract val classesJar: RegularFileProperty

    @get:OutputFile
    abstract val manifestFile: RegularFileProperty

    @get:OutputFile
    abstract val proguardFile: RegularFileProperty

    @get:OutputDirectory
    abstract val resDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val assetsDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val jniDirectory: DirectoryProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    init {
        stripKotlinMetadata.convention(false)
        onlyRelocatedClasses.convention(false)
        prefixMappings.convention(DEFAULT_RELOCATIONS)
    }

    @TaskAction
    fun relocate() {
        val original = originalArtifact.files.singleOrNull()
            ?: throw GradleException("Expected exactly one original artifact, got ${originalArtifact.files}")
        val relocation = PackageRelocation(prefixMappings.get())
        val resDir = resDirectory.get().asFile.also { it.deleteRecursively(); it.mkdirs() }
        val assetsDir = assetsDirectory.get().asFile.also { it.deleteRecursively(); it.mkdirs() }
        val jniDir = jniDirectory.get().asFile.also { it.deleteRecursively(); it.mkdirs() }
        val manifest = manifestFile.get().asFile.also { it.parentFile.mkdirs() }
        val proguard = proguardFile.get().asFile.also { it.parentFile.mkdirs() }
        val classes = classesJar.get().asFile.also { it.parentFile.mkdirs() }

        val report: JarRelocationReport
        if (onlyRelocatedClasses.get()) {
            val input = if (original.name.endsWith(".aar")) {
                File(temporaryDir, "classes.jar").also { extracted ->
                    ZipFile(original).use { aar -> aar.getEntry("classes.jar")?.let { entry -> aar.getInputStream(entry).use { extracted.writeBytes(it.readBytes()) } } }
                }
            } else {
                original
            }
            manifest.writeText("")
            proguard.writeText("")
            report = relocateJar(input, classes, relocation, stripKotlinMetadata.get(), onlyRelocatedClasses = true)
        } else if (original.name.endsWith(".aar")) {
            var manifestText: String? = null
            var proguardText = ""
            val extractedClasses = File(temporaryDir, "classes.jar")
            ZipFile(original).use { aar ->
                for (entry in aar.entries()) {
                    if (entry.isDirectory) continue
                    val bytes = aar.getInputStream(entry).use { it.readBytes() }
                    when {
                        entry.name == "classes.jar" -> extractedClasses.writeBytes(bytes)
                        entry.name == "AndroidManifest.xml" -> manifestText = String(bytes)
                        entry.name == "proguard.txt" -> proguardText = String(bytes)
                        entry.name.startsWith("res/") -> File(resDir, entry.name.removePrefix("res/")).also { it.parentFile.mkdirs() }.writeBytes(bytes)
                        entry.name.startsWith("assets/") -> File(assetsDir, entry.name.removePrefix("assets/")).also { it.parentFile.mkdirs() }.writeBytes(bytes)
                        entry.name.startsWith("jni/") -> File(jniDir, entry.name.removePrefix("jni/")).also { it.parentFile.mkdirs() }.writeBytes(bytes)
                        entry.name.startsWith("libs/") && entry.name.endsWith(".jar") ->
                            throw GradleException("${original.name} bundles a local jar (${entry.name}); relocating nested jars is not supported yet")
                        else -> Unit // R.txt, licenses, aar-metadata: regenerated by AGP or not needed
                    }
                }
            }
            val manifestXml = manifestText ?: throw GradleException("${original.name} has no AndroidManifest.xml")
            val declaredPackage = Regex("""package="([^"]*)"""").find(manifestXml)?.groupValues?.get(1)
            val expected = expectedNamespace.orNull ?: throw GradleException("relocatedArtifact.namespace must be set for AAR artifacts")
            if (declaredPackage != expected) {
                throw GradleException("${original.name} declares package '$declaredPackage' in its manifest but relocatedArtifact.namespace is '$expected'")
            }
            manifest.writeText(relocation.mapManifest(manifestXml))
            proguard.writeText(relocation.mapString(proguardText))
            report = if (extractedClasses.exists()) {
                relocateJar(extractedClasses, classes, relocation, stripKotlinMetadata.get())
            } else {
                emptyJar(classes)
                JarRelocationReport(0, emptyList(), emptyList())
            }
        } else {
            manifest.writeText("")
            proguard.writeText("")
            report = relocateJar(original, classes, relocation, stripKotlinMetadata.get())
        }

        reportFile.get().asFile.also { it.parentFile.mkdirs() }.writeText(
            buildString {
                appendLine("original: ${original.name}")
                appendLine("relocated classes: ${report.relocatedClasses}")
                appendLine("kept (native) classes: ${report.keptNativeClasses.joinToString().ifEmpty { "none" }}")
                appendLine("kotlin metadata: ${report.remappedKotlinMetadata} remapped, ${report.strippedKotlinMetadata} stripped (unreadable)")
                appendLine("unrelocated references: ${report.unrelocatedReferences.joinToString().ifEmpty { "none" }}")
            },
        )
        if (report.unrelocatedReferences.isNotEmpty()) {
            throw GradleException("Relocation of ${original.name} left references to the original package in: ${report.unrelocatedReferences.take(10)}")
        }
        logger.lifecycle("Relocated ${report.relocatedClasses} classes from ${original.name}")
    }

    private fun emptyJar(file: File) {
        java.util.zip.ZipOutputStream(file.outputStream()).use { }
    }
}
