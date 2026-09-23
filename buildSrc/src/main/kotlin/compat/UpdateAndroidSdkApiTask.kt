package compat

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.URL

/** Downloads the Firebase Android SDK `api.txt` files this module is compared against. */
abstract class UpdateAndroidSdkApiTask : DefaultTask() {

    /** Paths inside the firebase-android-sdk repository, e.g. `firebase-installations/api.txt`. */
    @get:Input
    abstract val sources: ListProperty<String>

    /** Git ref (tag, branch or commit) of firebase-android-sdk to download from. */
    @get:Input
    abstract val ref: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun download() {
        val dir = outputDirectory.get().asFile.also { it.mkdirs() }
        for (source in sources.get()) {
            val url = "https://raw.githubusercontent.com/firebase/firebase-android-sdk/${ref.get()}/$source"
            val text = URL(url).openStream().use { it.readBytes().decodeToString() }
            val target = dir.resolve(source.substringBefore('/') + ".api.txt")
            target.writeText("// Source: $url\n$text")
            logger.lifecycle("Downloaded $url -> ${target.relativeTo(project.rootDir)}")
        }
    }
}
