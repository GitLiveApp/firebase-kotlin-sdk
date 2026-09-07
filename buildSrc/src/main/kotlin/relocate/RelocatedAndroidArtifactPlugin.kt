package relocate

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute

/**
 * Turns a project into a relocated republication of one Firebase Android SDK AAR.
 *
 * ```
 * plugins { id("dev.gitlive.relocated-android-artifact") }
 * relocatedArtifact {
 *     original.set("com.google.firebase:firebase-installations")
 *     namespace.set("com.google.firebase.installations")
 * }
 * ```
 */
class RelocatedAndroidArtifactPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("relocatedArtifact", RelocatedArtifactExtension::class.java)
        val support = RelocatedArtifactSupport(project, extension)
        val relocate = support.registerRelocateTask()
        project.group = RELOCATED_GROUP

        // Registered before AGP and the publishing plugin so this runs before their own afterEvaluate hooks.
        project.afterEvaluate {
            val version = support.originalVersion()
            project.version = version
            val originalNamespace = support.originalNamespace()
            relocate.configure { expectedNamespace.set(originalNamespace) }
            project.extensions.configure(LibraryExtension::class.java) {
                namespace = support.relocation.mapDotted(originalNamespace)
            }
            project.dependencies.add("api", project.files(relocate.flatMap { it.classesJar }))
            support.mappedDependencies().forEach { project.dependencies.add("api", it) }
            support.configurePublishing {
                configure(AndroidSingleVariantLibrary(variant = "release", sourcesJar = false, publishJavadocJar = false))
                coordinates(RELOCATED_GROUP, project.relocatedArtifactId, version)
            }
        }

        project.pluginManager.apply("com.android.library")
        // No Kotlin sources, but the Kotlin plugin registers the attribute rules that let Android consumers resolve the
        // JVM variants of multiplatform dependencies (androidx.annotation, kotlin-stdlib, ...).
        project.pluginManager.apply("org.jetbrains.kotlin.android")
        project.pluginManager.apply("com.vanniktech.maven.publish")

        project.extensions.configure(LibraryExtension::class.java) {
            compileSdk = project.rootProject.extra("compileSdkVersion")
            defaultConfig.minSdk = project.rootProject.extra("minSdkVersion")
            compileOptions.sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
            compileOptions.targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
            defaultConfig.consumerProguardFiles(relocate.flatMap { it.proguardFile })
            lint.abortOnError = false
            lint.checkReleaseBuilds = false
        }
        project.extensions.configure(LibraryAndroidComponentsExtension::class.java) {
            onVariants(selector().withName("release")) { variant ->
                val runtimeClasses = variant.runtimeConfiguration.incoming.artifactView {
                    attributes.attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
                }.files
                support.registerVerifyTask(runtimeClasses)
            }
            onVariants { variant ->
                variant.sources.manifests.addGeneratedManifestFile(relocate, RelocateArtifactTask::manifestFile)
                variant.sources.res?.addGeneratedSourceDirectory(relocate, RelocateArtifactTask::resDirectory)
                variant.sources.assets?.addGeneratedSourceDirectory(relocate, RelocateArtifactTask::assetsDirectory)
                variant.sources.jniLibs?.addGeneratedSourceDirectory(relocate, RelocateArtifactTask::jniDirectory)
            }
        }
        project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
        project.tasks.configureEach {
            if (name.contains("ConsumerProguard", ignoreCase = true)) dependsOn(relocate)
        }
    }

    private fun Project.extra(name: String): Int = extensions.extraProperties.get(name) as Int
}
