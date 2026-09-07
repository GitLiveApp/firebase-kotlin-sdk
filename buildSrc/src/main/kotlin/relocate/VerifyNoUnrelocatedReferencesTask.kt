package relocate

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Scans the runtime classpath of a relocated artifact for third-party jars that still reference the
 * original `com.google.firebase` package. Such artifacts must be relocated too (add an `android-sdk/<artifactId>` project).
 */
abstract class VerifyNoUnrelocatedReferencesTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val classpath: ConfigurableFileCollection

    @get:Input
    abstract val forbiddenPrefixes: ListProperty<String>

    /** Artifact ids whose original classes are expected on the classpath (extract-only and keep-original artifacts). */
    @get:Input
    abstract val ignoredArtifacts: ListProperty<String>

    /** Package prefixes (slashed, e.g. `com/google/firebase/encoders/`) whose originals are kept and may be referenced. */
    @get:Input
    abstract val allowedPackages: ListProperty<String>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val prefixes = forbiddenPrefixes.get()
        val offenders = classpath.files
            .filter { it.isFile && it.name.endsWith(".jar") }
            .filterNot { it.path.contains("/android-sdk/") } // our own relocated outputs
            .filterNot { jar -> ignoredArtifacts.get().any { jar.path.contains("$it-") || jar.path.contains("/$it/") } }
            .mapNotNull { jar -> findClassReferences(jar, prefixes, allowedPackages.get()).takeIf { it.isNotEmpty() }?.let { jar.name to it } }
        reportFile.get().asFile.also { it.parentFile.mkdirs() }.writeText(
            if (offenders.isEmpty()) "ok\n" else offenders.joinToString("\n") { (jar, classes) -> "$jar: ${classes.joinToString()}" } + "\n",
        )
        if (offenders.isNotEmpty()) {
            throw GradleException(
                "These dependencies still reference the original Firebase package and must be relocated as well " +
                    "(add android-sdk/<artifactId>/build.gradle.kts):\n" +
                    offenders.joinToString("\n") { (jar, classes) -> "  $jar -> ${classes.joinToString()}" },
            )
        }
    }
}
