package relocate

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.signing.Sign

/** The Maven groups whose artifacts must be relocated (rather than depended upon as-is). */
val RELOCATED_GROUPS = setOf("com.google.firebase")

/** Registry of special cases declared in `android-sdk/build.gradle.kts` (see there). */
class RelocationRegistry(private val project: Project) {
    private fun names(key: String): Set<String> {
        @Suppress("UNCHECKED_CAST")
        return project.rootProject.project(":android-sdk").extensions.extraProperties.let { if (it.has(key)) it.get(key) as Set<String> else emptySet() }
    }
    val extractOnlyArtifacts: Set<String> get() = names("extractOnlyArtifacts")
    val keepOriginalArtifacts: Set<String> get() = names("keepOriginalArtifacts")
    val keepOriginalPackages: Set<String> get() = names("keepOriginalPackages")
    /** `group:module` of artifacts outside the Firebase group that are republished fully relocated. */
    val relocatedArtifactsOutsideFirebase: Set<String> get() = names("relocatedArtifactsOutsideFirebase")
}

/** Shared wiring for the relocated-artifact plugins. */
class RelocatedArtifactSupport(private val project: Project, val extension: RelocatedArtifactExtension) {

    val relocation: PackageRelocation = PackageRelocation(DEFAULT_RELOCATIONS)

    val registry = RelocationRegistry(project)

    /** Whether this project republishes only the relocated classes of an artifact outside the Firebase group. */
    val extractOnly: Boolean get() = extension.extractRelocatedClassesOnly.getOrElse(project.relocatedArtifactId in registry.extractOnlyArtifacts)

    /** Resolves the original artifact (and, transitively, its dependency graph for POM derivation). */
    val originalConfiguration: Configuration = project.configurations.create("originalArtifact") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isVisible = false
        description = "The original Firebase Android SDK artifact that this project relocates"
    }

    private val originalModule: String
        get() = extension.original.get().split(":").let { parts ->
            require(parts.size in 2..3) { "relocatedArtifact.original must be 'group:artifact' or 'group:artifact:version', got '${extension.original.get()}'" }
            "${parts[0]}:${parts[1]}"
        }

    init {
        val libs = project.rootProject.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
        val bom = libs.findLibrary("firebase-bom").get()
        project.dependencies.add(originalConfiguration.name, project.dependencies.platform(bom))
        project.dependencies.addProvider(originalConfiguration.name, extension.original)
    }

    /** The original artifact file only (no transitive artifacts). */
    val originalArtifactFiles: FileCollection = originalConfiguration.incoming.artifactView {
        componentFilter { id -> id is ModuleComponentIdentifier && "${id.group}:${id.module}" == originalModule }
    }.files

    fun registerRelocateTask(): TaskProvider<RelocateArtifactTask> =
        project.tasks.register("relocate", RelocateArtifactTask::class.java) {
            group = "build"
            description = "Relocates the original Firebase Android SDK artifact to the $RELOCATED_PACKAGE_PREFIX package"
            originalArtifact.from(originalArtifactFiles)
            expectedNamespace.set(extension.namespace)
            onlyRelocatedClasses.set(project.provider { extractOnly })
            val out = project.layout.buildDirectory.dir("relocated")
            classesJar.set(out.map { it.file("classes.jar") })
            manifestFile.set(out.map { it.file("AndroidManifest.xml") })
            proguardFile.set(out.map { it.file("proguard.txt") })
            resDirectory.set(out.map { it.dir("res") })
            assetsDirectory.set(out.map { it.dir("assets") })
            jniDirectory.set(out.map { it.dir("jni") })
            reportFile.set(out.map { it.file("report.txt") })
        }

    /** The resolved component of the original artifact. */
    private fun originalComponent(): ResolvedComponentResult {
        val module = originalModule
        return originalConfiguration.incoming.resolutionResult.allComponents.firstOrNull { component ->
            (component.id as? ModuleComponentIdentifier)?.let { "${it.group}:${it.module}" } == module
        } ?: run {
            val result = originalConfiguration.incoming.resolutionResult
            val unresolved = result.allDependencies.filterIsInstance<org.gradle.api.artifacts.result.UnresolvedDependencyResult>()
                .joinToString("\n") { "  ${it.requested}: ${it.failure.message}" }
            throw GradleException(
                "Could not resolve $module in ${project.path}. Resolved components: ${result.allComponents.map { it.id.displayName }}" +
                    if (unresolved.isEmpty()) "" else "\nUnresolved:\n$unresolved",
            )
        }
    }

    fun originalVersion(): String = (originalComponent().id as ModuleComponentIdentifier).version

    /** The android namespace of the original AAR: the configured one, or the `package` of its manifest. */
    fun originalNamespace(): String = extension.namespace.orNull ?: run {
        val aar = originalArtifactFiles.singleFile
        val manifest = java.util.zip.ZipFile(aar).use { zip ->
            zip.getEntry("AndroidManifest.xml")?.let { entry -> zip.getInputStream(entry).use { String(it.readBytes()) } }
        } ?: throw GradleException("${aar.name} has no AndroidManifest.xml")
        Regex("""package="([^"]*)"""").find(manifest)?.groupValues?.get(1)
            ?: throw GradleException("${aar.name} declares no package in its manifest; set relocatedArtifact.namespace")
    }

    /**
     * The dependencies of the relocated artifact:
     * - in extract mode ([RelocatedArtifactExtension.extractRelocatedClassesOnly]) just the original artifact, which
     *   provides everything except the relocated classes;
     * - otherwise the original artifact's direct dependencies where every dependency that has an `android-sdk/<artifactId>`
     *   project is replaced by that project (kept as well for extract-only and keep-original ones), every remaining Firebase
     *   dependency is an error, and every other dependency is pinned to the version the BoM resolved with the clashing
     *   original Firebase artifacts excluded from its transitive graph (the keep-original ones are re-added explicitly).
     */
    fun mappedDependencies(): List<Dependency> {
        val component = originalComponent()
        val id = component.id as ModuleComponentIdentifier
        if (extractOnly) return listOf(project.dependencies.create("${id.group}:${id.module}:${id.version}"))
        val graph = originalConfiguration.incoming.resolutionResult.allComponents
            .mapNotNull { it.id as? ModuleComponentIdentifier }
        val keepOriginal = graph
            .filter { it.group in RELOCATED_GROUPS && it.module in registry.keepOriginalArtifacts }
            .map { project.dependencies.create("${it.group}:${it.module}:${it.version}") }
        val direct = component.dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .mapNotNull { it.selected.id as? ModuleComponentIdentifier }
            .filterNot { it.group == "com.google.firebase" && it.module == "firebase-bom" }
            .flatMap { dep ->
                val sibling = project.rootProject.findProject(":android-sdk:relocated-${dep.module}")
                val original = "${dep.group}:${dep.module}:${dep.version}"
                when {
                    sibling != null && dep.module in registry.extractOnlyArtifacts -> listOf(project.dependencies.create(original), project.dependencies.create(sibling))
                    sibling != null -> listOf(project.dependencies.create(sibling))
                    dep.group in RELOCATED_GROUPS -> throw GradleException(
                        "${project.path} depends on $original, which must be relocated too: add android-sdk/${dep.module}/build.gradle.kts",
                    )
                    else -> listOf(
                        (project.dependencies.create(original) as ModuleDependency).apply {
                            RELOCATED_GROUPS.forEach { group -> exclude(mapOf("group" to group)) }
                            registry.relocatedArtifactsOutsideFirebase.forEach { coordinates ->
                                exclude(mapOf("group" to coordinates.substringBefore(':'), "module" to coordinates.substringAfter(':')))
                            }
                        },
                    )
                }
            }
        return direct + keepOriginal
    }

    fun configurePublishing(configure: MavenPublishBaseExtension.() -> Unit) {
        project.extensions.configure(MavenPublishBaseExtension::class.java) {
            configure()
            publishToMavenCentral(automaticRelease = true)
            signAllPublications()
            pom {
                name.set("Firebase Android SDK (relocated)")
                description.set(
                    "The Firebase Android SDK artifact ${extension.original.get()} with its classes relocated to the " +
                        "$RELOCATED_PACKAGE_PREFIX package, for use by the Firebase Kotlin SDK.",
                )
                url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("Nicholas Bransby-Williams")
                        email.set("nbransby@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
                    connection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
                    developerConnection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
                }
            }
        }
        project.tasks.withType(Sign::class.java).configureEach {
            onlyIf { !project.gradle.startParameter.taskNames.any { "MavenLocal" in it } }
        }
    }

    fun registerVerifyTask(classpath: FileCollection): TaskProvider<VerifyNoUnrelocatedReferencesTask> =
        project.tasks.register("verifyRelocation", VerifyNoUnrelocatedReferencesTask::class.java) {
            group = "verification"
            description = "Checks that no dependency still references the original Firebase package"
            this.classpath.from(classpath)
            forbiddenPrefixes.set(relocation.forbiddenPrefixes)
            ignoredArtifacts.set(project.provider { (registry.extractOnlyArtifacts + registry.keepOriginalArtifacts).toList() })
            allowedPackages.set(project.provider { registry.keepOriginalPackages.map { it.replace('.', '/') + "/" } })
            reportFile.set(project.layout.buildDirectory.file("relocated/verify.txt"))
        }
}

/** The artifact id of a relocated project: the project name without the `relocated-` prefix, i.e. the original artifact id. */
val Project.relocatedArtifactId: String get() = name.removePrefix("relocated-")
