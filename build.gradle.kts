import de.undercouch.gradle.tasks.download.Download
import org.apache.tools.ant.taskdefs.condition.Os
import kotlin.system.exitProcess

plugins {
    kotlin("multiplatform") version "1.3.71" apply false
    id("de.undercouch.download").version("3.4.3")
    id("base")
}

buildscript {
    repositories {
        jcenter()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.2")
        classpath("de.undercouch:gradle-download-task:4.0.4")
    }
}

val targetSdkVersion by extra(28)
val minSdkVersion by extra(16)


tasks {
    val downloadIOSFirebaseZipFile by creating(Download::class) {
        src("https://github.com/firebase/firebase-ios-sdk/releases/download/6.17.0/Firebase-6.17.0.zip")
        dest(File(buildDir, "Firebase-6.17.0.zip"))
        if (System.getenv("token") != null) {
            username(System.getenv("token"))
        }
        overwrite(false)
    }

    val unzipIOSFirebase by creating(Copy::class) {
        if (!File("$buildDir/Firebase").exists()) {
            val zipFile = File(buildDir, "Firebase-6.17.0.zip")
            if (!zipFile.exists()) {
                dependsOn(downloadIOSFirebaseZipFile)
            }
            from(zipTree(zipFile))
            into(buildDir)
        }
        outputs.upToDateWhen { File("$buildDir/Firebase").isDirectory }
    }

    val publishJvm by creating(Exec::class) {
        var successfulRuns = 0
        subprojects {
            isIgnoreExitValue = true
            commandLine("./gradlew", ":$name:publishJvmPublicationToGitHubPackagesRepository")
            doLast {
                println("Project: $name Version: $version exec result: ${execResult.exitValue}")
                if(execResult.exitValue == 0) {
                    successfulRuns += 1
                }
            }
        }
        doLast {
            println("Total successful publications: $successfulRuns")
            if (successfulRuns == 0) {
                exitProcess(1)
            }
        }
    }

}

subprojects {

    group = "dev.gitlive"


    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/gitliveapp/packages")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    var shouldSign = true

    tasks.withType<Sign>().configureEach {
        onlyIf { shouldSign }
    }

    tasks {

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
            println("$buildDir/classes/kotlin/js/main/")
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
                        .replace("require('firebase-", "require('@gitlive/firebase-")
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
            commandLine("npm", "publish")
        }
    }

//    tasks.withType<KotlinCompile<*>> {
//        kotlinOptions.freeCompilerArgs += listOf(
//            "-Xuse-experimental=kotlin.Experimental",
//            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
//            "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
//        )
//    }

    afterEvaluate  {
        // create the projects node_modules if they don't exist
        if(!File("$buildDir/node_module").exists()) {
            mkdir("$buildDir/node_module")
        }

        if(Os.isFamily(Os.FAMILY_MAC)) {
            tasks.getByPath("compileKotlinIos").dependsOn(rootProject.tasks.named("unzipIOSFirebase"))
            tasks.getByPath("compileKotlinIosArm64").dependsOn(rootProject.tasks.named("unzipIOSFirebase"))
        } else {
            println("Skipping Firebase zip dowload")
        }

        tasks.named("publishToMavenLocal").configure {
            shouldSign = false
        }

        dependencies {
            "jvmMainImplementation"(kotlin("stdlib-jdk8"))
            "jvmMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
            "jvmMainApi"("dev.gitlive:firebase-java-sdk:0.4.0")
            "jvmMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.5") {
                exclude("com.google.android.gms")
            }
            "commonMainImplementation"(kotlin("stdlib-common"))
            "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.6")
            "jsMainImplementation"(kotlin("stdlib-js"))
            "jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.6")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.6")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.6")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.6")
            "commonTestImplementation"(kotlin("test-common"))
            "commonTestImplementation"(kotlin("test-annotations-common"))
            "commonTestImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.6")
            "commonTestImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.6")
            "jsTestImplementation"(kotlin("test-js"))
            "androidAndroidTestImplementation"(kotlin("test-junit"))
            "androidAndroidTestImplementation"("junit:junit:4.12")
            "androidAndroidTestImplementation"("androidx.test:core:1.2.0")
            "androidAndroidTestImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
            "androidAndroidTestImplementation"("androidx.test.ext:junit:1.1.1")
            "androidAndroidTestImplementation"("androidx.test:runner:1.1.0")
        }
    }

    apply(plugin="maven-publish")
    apply(plugin="signing")

    val javadocJar by tasks.creating(Jar::class) {
        archiveClassifier.value("javadoc")
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
            maven {
                name = "GitHubPackages"
                url  = uri("https://maven.pkg.github.com/gitliveapp/packages")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
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

}

