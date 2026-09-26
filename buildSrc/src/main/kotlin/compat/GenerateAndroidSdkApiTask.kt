package compat

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Generates the `api.txt` of a Firebase Android SDK module that is not open source from its published classes (see
 * [AndroidSdkApiJarDumper]), in the format of the files [UpdateAndroidSdkApiTask] downloads.
 */
abstract class GenerateAndroidSdkApiTask : DefaultTask() {

    /** The classes jars of the SDK artifacts, as resolved from the Android compile classpath. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val jars: ConfigurableFileCollection

    /** The coordinates of [jars], recorded in the file header. */
    @get:Input
    abstract val coordinates: ListProperty<String>

    /** Internal package prefixes whose classes are dumped, e.g. `com/google/firebase/auth/`. */
    @get:Input
    abstract val packages: ListProperty<String>

    @get:Input
    abstract val header: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val text = AndroidSdkApiJarDumper.dump(jars.files.toList(), packages.get())
        val target = outputFile.get().asFile.also { it.parentFile.mkdirs() }
        target.writeText("// Source: ${header.get()} ${coordinates.get().joinToString(", ")}\n$text")
        logger.lifecycle("Generated ${target.relativeTo(project.rootDir)} from ${coordinates.get().joinToString(", ")}")
    }
}
