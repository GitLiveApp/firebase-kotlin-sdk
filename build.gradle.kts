import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform") version "1.4.0" apply false
    kotlin("native.cocoapods") version "1.4.0"
    id("de.undercouch.download").version("3.4.3")
    id("base")
}

buildscript {
    repositories {
        jcenter()
        google()
        gradlePluginPortal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath("de.undercouch:gradle-download-task:4.0.4")
        classpath("com.adarshr:gradle-test-logger-plugin:2.0.0")
    }
}

val targetSdkVersion by extra(28)
val minSdkVersion by extra(16)

tasks {
    val updateVersions by registering {
        dependsOn(
            "firebase-app:updateVersion", "firebase-app:updateDependencyVersion",
            "firebase-auth:updateVersion", "firebase-auth:updateDependencyVersion",
            "firebase-common:updateVersion", "firebase-common:updateDependencyVersion",
            "firebase-database:updateVersion", "firebase-database:updateDependencyVersion",
            "firebase-firestore:updateVersion", "firebase-firestore:updateDependencyVersion",
            "firebase-functions:updateVersion", "firebase-functions:updateDependencyVersion"
        )
    }
}


subprojects {

    group = "dev.gitlive"

    apply(plugin="com.adarshr.test-logger")
    
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
    }

    tasks {

        val updateVersion by registering(Exec::class) {
            commandLine("npm", "--allow-same-version", "--prefix", projectDir, "version", "${project.property("${project.name}.version")}")
        }

        val updateDependencyVersion by registering(Copy::class) {
            mustRunAfter("updateVersion")
            val from = file("package.json")
            from.writeText(
                from.readText()
                    .replace("firebase-common\": \"([^\"]+)".toRegex(), "firebase-common\": \"${project.property("firebase-common.version")}")
                    .replace("firebase-app\": \"([^\"]+)".toRegex(), "firebase-app\": \"${project.property("firebase-app.version")}")
            )
        }

        val copyReadMe by registering(Copy::class) {
            from(rootProject.file("README.md"))
            into(file("$buildDir/node_module"))
        }

        val copyPackageJson by registering(Copy::class) {
            from(file("package.json"))
            into(file("$buildDir/node_module"))
        }

        val unzipJar by registering(Copy::class) {
            val zipFile = File("$buildDir/libs", "${project.name}-js-${project.version}.jar")
            from(this.project.zipTree(zipFile))
            into("$buildDir/classes/kotlin/js/main/")
        }

        val copyJS by registering {
            mustRunAfter("unzipJar", "copyPackageJson")
            doLast {
                val from = File("$buildDir/classes/kotlin/js/main/${rootProject.name}-${project.name}.js")
                val into = File("$buildDir/node_module/${project.name}.js")
                into.createNewFile()
                into.writeText(
                    from.readText()
                        .replace("require('firebase-kotlin-sdk-", "require('@gitlive/")
                    //                .replace("require('kotlinx-serialization-kotlinx-serialization-runtime')", "require('@gitlive/kotlinx-serialization-runtime')")
                )
            }
        }

        val copySourceMap by registering(Copy::class) {
            from(file("$buildDir/classes/kotlin/js/main/${project.name}.js.map"))
            into(file("$buildDir/node_module"))
        }

        val prepareForNpmPublish by registering {
            dependsOn(
                unzipJar,
                copyPackageJson,
                copySourceMap,
                copyReadMe,
                copyJS
            )
        }

        val publishToNpm by creating(Exec::class) {
            workingDir("$buildDir/node_module")
            isIgnoreExitValue = true
            if(Os.isFamily(Os.FAMILY_WINDOWS)) {
                commandLine("cmd", "/c", "npm publish")
            } else {
                commandLine("npm", "publish")
            }
        }

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
    }

    afterEvaluate  {

        val copyFrameworksForX64Test by tasks.registering(Copy::class) {
            val tree = fileTree("$buildDir/cocoapods/synthetic/iosX64/${project.name.replace('-','_')}/build/Release-iphonesimulator")

            from(tree) {
                include("**/*.framework/**")
                include("**/*.framework.DSYM/**")
                include("**/*.bundle")
            }
            eachFile {
                path = path.substring(1).substringAfter("/", path)
            }
            into ("$buildDir/bin/iosX64/debugTest/Frameworks")
        }

        val linkDebugTestIosX64 by tasks.getting {
            dependsOn(copyFrameworksForX64Test)
        }

        // create the projects node_modules if they don't exist
        if(!File("$buildDir/node_module").exists()) {
            mkdir("$buildDir/node_module")
        }

        // android {
        extensions.getByType(com.android.build.gradle.LibraryExtension::class).apply {
            compileSdkVersion(property("targetSdkVersion") as Int)
            defaultConfig {
                minSdkVersion(property("minSdkVersion") as Int)
                targetSdkVersion(property("targetSdkVersion") as Int)
            }
            sourceSets {
                getByName("main") {
                    manifest.srcFile("src/androidMain/AndroidManifest.xml")
                }
            }
            testOptions {
                unitTests.apply {
                    isIncludeAndroidResources = true
                }
            }
            packagingOptions {
                pickFirst("META-INF/kotlinx-serialization-runtime.kotlin_module")
                pickFirst("META-INF/AL2.0")
                pickFirst("META-INF/LGPL2.1")
                pickFirst("androidsupportmultidexversion.txt")
            }
            lintOptions {
                isAbortOnError = false
            }

            useLibrary("android.test.runner")
            useLibrary("android.test.base")
        }

        // kotlin {
        extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class).apply {

            android {
                publishLibraryVariants("release", "debug")
            }

            sourceSets {

                all {
                    languageSettings.useExperimentalAnnotation("kotlinx.serialization.InternalSerializationApi")
                    languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
                    languageSettings.useExperimentalAnnotation("kotlin.ExperimentalMultiplatform")
                    languageSettings.useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                    languageSettings.useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
                }

                val iosX64Main by getting {
                    kotlin.srcDir("src/iosMain/kotlin")
                }

                val iosX64Test by getting {
                    kotlin.srcDir("src/iosTest/kotlin")
                }
                // no tests for Arm64 at the moment

                val commonTest by getting {
                    dependencies {
                        implementation(kotlin("test"))
                        implementation(kotlin("test-annotations-common"))
                    }
                }
            }
        }

        tasks.withType<Sign>().configureEach {
            onlyIf { !project.gradle.startParameter.taskNames.contains("publishToMavenLocal")
            }
        }
    }

    apply(plugin="maven-publish")
    apply(plugin="signing")

    configure<SigningExtension> {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        val publishing = extensions.getByType(PublishingExtension::class)
        sign(publishing.publications)
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

}

