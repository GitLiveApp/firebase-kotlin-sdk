package compat

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Compares the module's `api/android/<module>.api` (binary-compatibility-validator dump) against the vendored Firebase
 * Android SDK `api.txt` files and writes/checks `api/android-sdk-compat.txt`.
 */
abstract class AndroidSourceCompatTask : DefaultTask() {

    @get:Input
    abstract val moduleName: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val bcvDump: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val androidSdkApiFiles: ConfigurableFileCollection

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val exclusionsFile: RegularFileProperty

    /** The dump of the Android header stubs written by `stripHeaderStubs` (they are removed before the BCV dump is taken). */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val headerStubDumps: ConfigurableFileCollection

    /** When set, the generated report is compared with this file instead of written to it. */
    @get:Input
    abstract val check: Property<Boolean>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    /**
     * One pattern per line (`*` wildcards, `#member` suffix for members); `//` starts a comment. A comment containing
     * `@hide` marks the member as hidden in the Android SDK, which excludes it from the count instead of counting it as omitted.
     */
    private fun RegularFileProperty.exclusions(): List<Exclusion> = orNull?.asFile?.takeIf { it.exists() }?.readLines().orEmpty()
        .mapNotNull { line ->
            val pattern = line.substringBefore("//").trim()
            if (pattern.isEmpty()) {
                null
            } else {
                Exclusion(
                    pattern = Regex(pattern.replace(".", "\\.").replace("$", "\\$").replace("*", ".*")),
                    hidden = line.substringAfter("//", "").contains("@hide"),
                )
            }
        }

    @TaskAction
    fun run() {
        val sdkFiles = androidSdkApiFiles.files.filter { it.isFile }.sortedBy { it.name }
        if (sdkFiles.isEmpty()) throw GradleException("No Android SDK api.txt files found; run updateAndroidSdkApi first")
        val ref = sdkFiles.firstNotNullOfOrNull { file ->
            file.useLines { lines -> lines.firstOrNull { it.startsWith("// Source: ") } }?.substringAfter("firebase-android-sdk/")?.substringBefore('/')
        } ?: "unknown"
        val androidSdk = sdkFiles.flatMap { AndroidSdkApiTxtParser.parse(it.readText()) }
        val ours = BcvApiParser.parse(bcvDump.get().asFile.readText()) +
            headerStubDumps.files.filter { it.isFile }.flatMap { BcvApiParser.parse(it.readText()) }
        val exclusions = exclusionsFile.exclusions()
        val report = SourceCompatReport.generate(moduleName.get(), ref, androidSdk, ours, exclusions)
        val target = reportFile.get().asFile
        if (check.get()) {
            val existing = target.takeIf { it.exists() }?.readText()
            if (existing != report) {
                val generated = project.layout.buildDirectory.file("android-sdk-compat.txt").get().asFile
                generated.parentFile.mkdirs()
                generated.writeText(report)
                throw GradleException(
                    "Android SDK source compatibility report ${target.relativeTo(project.rootDir)} is out of date. " +
                        "Run ./gradlew :${moduleName.get()}:androidSourceCompatDump and commit the result (generated report: $generated)",
                )
            }
        } else {
            target.parentFile.mkdirs()
            target.writeText(report)
            logger.lifecycle(report.lines().drop(2).first())
        }
    }
}
