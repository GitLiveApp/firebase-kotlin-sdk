package relocate

import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 * Turns a project into a relocated republication of one plain JAR (e.g. `dev.gitlive:firebase-java-sdk`, the JVM port
 * of the Firebase Android SDK, or `com.google.firebase:firebase-annotations`).
 *
 * ```
 * plugins { id("dev.gitlive.relocated-jvm-artifact") }
 * relocatedArtifact { original.set("dev.gitlive:firebase-java-sdk:0.6.3") }
 * ```
 */
class RelocatedJvmArtifactPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("relocatedArtifact", RelocatedArtifactExtension::class.java)
        val support = RelocatedArtifactSupport(project, extension)
        val relocate = support.registerRelocateTask()
        project.group = RELOCATED_GROUP

        project.afterEvaluate {
            val version = support.originalVersion()
            project.version = version
            support.mappedDependencies().forEach { project.dependencies.add("api", it) }
            support.configurePublishing {
                configure(JavaLibrary(javadocJar = JavadocJar.None(), sourcesJar = false))
                coordinates(RELOCATED_GROUP, project.relocatedArtifactId, version)
            }
            support.registerVerifyTask(project.configurations.getByName("runtimeClasspath"))
        }

        project.pluginManager.apply("java-library")
        project.pluginManager.apply("com.vanniktech.maven.publish")

        // Expose the relocated classes as a classes directory so both the `classes` (project-to-project) and `jar`
        // variants of the java-library contain them.
        val extract = project.tasks.register("extractRelocatedClasses", org.gradle.api.tasks.Copy::class.java) {
            dependsOn(relocate)
            from(project.zipTree(relocate.flatMap { it.classesJar }))
            into(project.layout.buildDirectory.dir("relocated/classes"))
        }
        val sourceSets = project.extensions.getByType(org.gradle.api.tasks.SourceSetContainer::class.java)
        (sourceSets.getByName("main").output.classesDirs as org.gradle.api.file.ConfigurableFileCollection)
            .from(extract.map { it.destinationDir })
        project.tasks.named("jar", Jar::class.java) { dependsOn(extract) }
    }
}
