import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("native.cocoapods") apply false
    id("base")
    id("com.github.ben-manes.versions") version "0.42.0"
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${project.extra["gradlePluginVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("com.adarshr:gradle-test-logger-plugin:3.2.0")
    }
}

val compileSdkVersion by extra(34)
val minSdkVersion by extra(23)

tasks {
    register("updateVersions") {
        dependsOn(
            "firebase-app:updateVersion", "firebase-app:updateDependencyVersion",
            "firebase-auth:updateVersion", "firebase-auth:updateDependencyVersion",
            "firebase-common:updateVersion", "firebase-common:updateDependencyVersion",
            "firebase-config:updateVersion", "firebase-config:updateDependencyVersion",
            "firebase-database:updateVersion", "firebase-database:updateDependencyVersion",
            "firebase-firestore:updateVersion", "firebase-firestore:updateDependencyVersion",
            "firebase-functions:updateVersion", "firebase-functions:updateDependencyVersion",
            "firebase-installations:updateVersion", "firebase-installations:updateDependencyVersion",
            "firebase-perf:updateVersion", "firebase-perf:updateDependencyVersion",
            "firebase-storage:updateVersion", "firebase-storage:updateDependencyVersion"
        )
    }
}

subprojects {

    group = "dev.gitlive"
    
    apply(plugin = "com.adarshr.test-logger")

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

        val coroutinesVersion: String by project
        val firebaseBoMVersion: String by project

        dependencies {
            "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
            "androidMainImplementation"(platform("com.google.firebase:firebase-bom:$firebaseBoMVersion"))
            "commonTestImplementation"(kotlin("test-common"))
            "commonTestImplementation"(kotlin("test-annotations-common"))
            if (this@afterEvaluate.name != "firebase-crashlytics") {
                "jvmMainApi"("dev.gitlive:firebase-java-sdk:0.1.2")
                "jvmMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion") {
                    exclude("com.google.android.gms")
                }
                "jsTestImplementation"(kotlin("test-js"))
                "jvmTestImplementation"(kotlin("test-junit"))
                "jvmTestImplementation"("junit:junit:4.13.2")
            }
            "androidInstrumentedTestImplementation"(kotlin("test-junit"))
            "androidUnitTestImplementation"(kotlin("test-junit"))
            "androidInstrumentedTestImplementation"("junit:junit:4.13.2")
            "androidInstrumentedTestImplementation"("androidx.test:core:1.5.0")
            "androidInstrumentedTestImplementation"("androidx.test.ext:junit:1.1.5")
            "androidInstrumentedTestImplementation"("androidx.test:runner:1.5.2")
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
