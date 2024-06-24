import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL
import java.io.InputStream

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.native.cocoapods) apply false
    alias(libs.plugins.test.logger.plugin) apply false
    alias(libs.plugins.ben.manes.versions) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.kotlinx.binarycompatibilityvalidator)
    alias(libs.plugins.dokka)
    id("base")
    id("testOptionsConvention")
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

val compileSdkVersion by extra(34)
val minSdkVersion by extra(23)

tasks {
    register("updateVersions") {
        dependsOn(
            "firebase-analytics:updateVersion", "firebase-analytics:updateDependencyVersion",
            "firebase-app:updateVersion", "firebase-app:updateDependencyVersion",
            "firebase-auth:updateVersion", "firebase-auth:updateDependencyVersion",
            "firebase-common:updateVersion", "firebase-common:updateDependencyVersion",
            "firebase-config:updateVersion", "firebase-config:updateDependencyVersion",
            "firebase-database:updateVersion", "firebase-database:updateDependencyVersion",
            "firebase-firestore:updateVersion", "firebase-firestore:updateDependencyVersion",
            "firebase-functions:updateVersion", "firebase-functions:updateDependencyVersion",
            "firebase-messaging:updateVersion", "firebase-messaging:updateDependencyVersion",
            "firebase-installations:updateVersion", "firebase-installations:updateDependencyVersion",
            "firebase-perf:updateVersion", "firebase-perf:updateDependencyVersion",
            "firebase-storage:updateVersion", "firebase-storage:updateDependencyVersion"
        )
    }
}

private val dokkaCopyrightMessage = "© 2024 GitLive Ltd."
private val dokkaHomepageUrl = "https://github.com/GitLiveApp/firebase-kotlin-sdk"

tasks.withType<AbstractDokkaTask>().configureEach {
    val version = project.property("firebase-app.version") as String
    moduleVersion.set(version)
    moduleName.set("Firebase Kotlin SDK")

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("documentation/gitlive-logo.png"), file("documentation/homepage.svg"))
        customStyleSheets = listOf(file("documentation/logo-styles.css"))
        footerMessage = dokkaCopyrightMessage
        homepageLink = dokkaHomepageUrl
    }
}

subprojects {

    group = "dev.gitlive"

    val nonDocumentationList = listOf("test-utils", "firebase-common", "firebase-common-internal")
    val skipDocumentation = nonDocumentationList.contains(project.name)
    if (!skipDocumentation) {
        apply(plugin = "org.jetbrains.dokka")
    }

    this.tasks.withType<DokkaTaskPartial>().configureEach {
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            footerMessage = dokkaCopyrightMessage
            separateInheritedMembers = false
            homepageLink = dokkaHomepageUrl
        }
        dokkaSourceSets {
            configureEach {
                documentedVisibilities.set(setOf(DokkaConfiguration.Visibility.PUBLIC))
                includes.setFrom("documentation.md")

                sourceLink {
                    localDirectory.set(projectDir.resolve("src"))
                    remoteUrl.set(URL("$dokkaHomepageUrl/tree/master/${project.name}/src"))
                }
            }
            if (this.names.contains("jsMain")) {
                named("jsMain") {
                    perPackageOption {
                        // External files for JS should not be documented since they will not be available
                        matchingRegex.set(".*.externals.*")
                        suppress.set(true)
                    }
                }
            }
        }
    }

    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "org.jmailen.kotlinter")

    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    tasks.withType<Sign>().configureEach {
        onlyIf { !project.gradle.startParameter.taskNames.any { "MavenLocal" in it } }
    }

    val skipPublishing = project.name == "test-utils" // skip publishing for test utils

    tasks {
        withType<Test> {
            testLogging {
                showExceptions = true
                exceptionFormat = TestExceptionFormat.FULL
                showStandardStreams = true
                showCauses = true
                showStackTraces = true
                events = setOf(
                    TestLogEvent.STARTED,
                    TestLogEvent.FAILED,
                    TestLogEvent.PASSED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.STANDARD_OUT,
                    TestLogEvent.STANDARD_ERROR
                )
            }
        }

        if (skipPublishing) return@tasks

        register<Exec>("updateVersion") {
            commandLine("npm", "--allow-same-version", "--prefix", projectDir, "version", "${project.property("${project.name}.version")}")
        }

        register<Copy>("updateDependencyVersion") {
            mustRunAfter("updateVersion")
            val from = file("package.json")
            from.writeText(
                from.readText()
                    .replace("version\": \"([^\"]+)".toRegex(), "version\": \"${project.property("${project.name}.version")}")
                    .replace("firebase-common\": \"([^\"]+)".toRegex(), "firebase-common\": \"${project.property("firebase-common.version")}")
                    .replace("firebase-app\": \"([^\"]+)".toRegex(), "firebase-app\": \"${project.property("firebase-app.version")}")
            )
        }
    }

    afterEvaluate  {
        dependencies {
            "commonMainImplementation"(libs.kotlinx.coroutines.core)
            "androidMainImplementation"(libs.kotlinx.coroutines.play.services)
            "androidMainImplementation"(platform(libs.firebase.bom))
            "commonTestImplementation"(kotlin("test-common"))
            "commonTestImplementation"(kotlin("test-annotations-common"))
            if (this@afterEvaluate.name != "firebase-crashlytics") {
                "jvmMainApi"(libs.gitlive.firebase.java.sdk)
                "jvmMainApi"(libs.kotlinx.coroutines.play.services) {
                    exclude("com.google.android.gms")
                }
                "jsTestImplementation"(kotlin("test-js"))
                "jvmTestImplementation"(kotlin("test-junit"))
                "jvmTestImplementation"(libs.junit)
            }
            "androidInstrumentedTestImplementation"(kotlin("test-junit"))
            "androidUnitTestImplementation"(kotlin("test-junit"))
            "androidInstrumentedTestImplementation"(libs.junit)
            "androidInstrumentedTestImplementation"(libs.androidx.test.core)
            "androidInstrumentedTestImplementation"(libs.androidx.test.junit)
            "androidInstrumentedTestImplementation"(libs.androidx.test.runner)
        }
    }

    if (skipPublishing) return@subprojects

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

    configure<PublishingExtension> {

        repositories {
            maven {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")

                credentials {
                    username = project.findProperty("sonatypeUsername") as String? ?: System.getenv("sonatypeUsername")
                    password = project.findProperty("sonatypePassword") as String? ?: System.getenv("sonatypePassword")
                }
            }
        }

        publications.all {
            this as MavenPublication
            artifact(javadocJar)

            pom {
                name.set("firebase-kotlin-sdk")
                description.set("The Firebase Kotlin SDK is a Kotlin-first SDK for Firebase. It's API is similar to the Firebase Android SDK Kotlin Extensions but also supports multiplatform projects, enabling you to use Firebase directly from your common source targeting iOS, Android or JS.")
                url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
                inceptionYear.set("2019")

                scm {
                    url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
                    connection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
                    developerConnection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
                    tag.set("HEAD")
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk/issues")
                }

                developers {
                    developer {
                        name.set("Nicholas Bransby-Williams")
                        email.set("nbransby@gmail.com")
                    }
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                        comments.set("A business-friendly OSS license")
                    }
                }
            }
        }

    }

    tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(tasks.withType(Sign::class.java))
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(java.util.Locale.ENGLISH).contains(it) }
        val versionMatch = "^[0-9,.v-]+(-r)?$".toRegex().matches(version)

        return (stableKeyword || versionMatch).not()
    }

    rejectVersionIf {
        isNonStable(candidate.version)
    }

    checkForGradleUpdate = true
    outputFormatter = "plain,html"
    outputDir = "build/dependency-reports"
    reportfileName = "dependency-updates"
}
// check for latest dependencies - ./gradlew dependencyUpdates -Drevision=release

tasks.register("devRunEmulatorTests") {
    doLast {
        EmulatorJobsMatrix().getTaskList(rootProject = rootProject).forEach { gradleTasks ->
            exec {
                executable = File(
                    project.rootDir,
                    if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew",
                )
                    .also { it.setExecutable(true) }
                    .absolutePath
                args = gradleTasks
                println("exec: ${this.commandLine.joinToString(separator = " ")}")
            }.apply { println("ExecResult: $this") }
        }
    }
}

tasks.register("ciEmulatorJobsMatrixSetup") {
    doLast {
        EmulatorJobsMatrix().createMatrixJsonFile(rootProject = rootProject)
    }
}

tasks.register("ciSdkManagerLicenses") {
    doLast {
        val sdkDirPath = getAndroidSdkPath(rootDir = rootDir)
        getSdkmanagerFile(rootDir = rootDir)?.let { sdkmanagerFile ->
            val yesInputStream = object : InputStream() {
                private val yesString = "y\n"
                private var counter = 0
                override fun read(): Int = yesString[counter % 2].also { counter++ }.code
            }
            exec {
                executable = sdkmanagerFile.absolutePath
                args = listOf("--list", "--sdk_root=$sdkDirPath")
                println("exec: ${this.commandLine.joinToString(separator = " ")}")
            }.apply { println("ExecResult: $this") }
            exec {
                executable = sdkmanagerFile.absolutePath
                args = listOf("--licenses", "--sdk_root=$sdkDirPath")
                standardInput = yesInputStream
                println("exec: ${this.commandLine.joinToString(separator = " ")}")
            }.apply { println("ExecResult: $this") }
        }
    }
}
