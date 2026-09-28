import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import compat.registerAndroidSourceCompat
import utils.TargetPlatform
import utils.applyFirebaseHierarchy
import utils.stripHeaderStubs
import utils.supportsApple
import utils.toTargetPlatforms

/*
 * Copyright (c) 2026 GitLive Ltd. Use of this source code is governed by the Apache 2.0 license.
 */

val supportedPlatforms = (project.property("firebase-dataconnect.supportedTargets") as String).toTargetPlatforms()

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("testOptionsConvention")
    alias(libs.plugins.publish)
}

if (supportedPlatforms.contains(TargetPlatform.Android)) {
    android {
        val minSdkVersion: Int by project
        val compileSdkVersion: Int by project

        compileSdk = compileSdkVersion
        namespace = "dev.gitlive.firebase.dataconnect"

        defaultConfig {
            minSdk = minSdkVersion
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        testOptions.configureTestOptions(project)
        packaging {
            resources.pickFirsts.add("META-INF/kotlinx-serialization-core.kotlin_module")
            resources.pickFirsts.add("META-INF/AL2.0")
            resources.pickFirsts.add("META-INF/LGPL2.1")
        }
        lint {
            abortOnError = false
        }
    }
}

kotlin {
    explicitApi()
    applyFirebaseHierarchy()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        // com.google.firebase.Timestamp converts to kotlin.time.Instant; the UUID serializer of the dev.gitlive layer uses kotlin.uuid.Uuid.
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
        optIn.add("com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect")
    }
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    if (this is KotlinJvmCompilerOptions) {
                        jvmTarget = JvmTarget.JVM_17
                        // The Data Connect SDK is compiled without JVM default methods (interface defaults live in
                        // DefaultImpls classes); the header stubs, and this module's own code that calls them, must match.
                        freeCompilerArgs.add("-jvm-default=disable")
                    }
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    if (supportedPlatforms.contains(TargetPlatform.Android)) {
        @Suppress("OPT_IN_USAGE")
        androidTarget {
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
            unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
            publishAllLibraryVariants()
        }
    }

    if (supportedPlatforms.contains(TargetPlatform.Jvm)) {
        jvm()
    }

    if (supportedPlatforms.contains(TargetPlatform.Ios)) {
        iosArm64()
        iosX64()
        iosSimulatorArm64()
    }
    if (supportedPlatforms.contains(TargetPlatform.Tvos)) {
        tvosArm64()
        tvosSimulatorArm64()
    }
    if (supportedPlatforms.contains(TargetPlatform.Macos)) {
        macosArm64()
    }
    if (supportedPlatforms.supportsApple()) {
        // The Firebase iOS Data Connect SDK is Swift-only, which Kotlin/Native cannot import, so the Apple targets import the
        // local Swift package under apple/, which wraps it in an Objective-C API (the workaround JetBrains recommends for
        // Swift-only packages). The package depends on the Data Connect SDK, which depends on firebase-ios-sdk; the shared
        // Package.resolved keeps the latter at the version the other modules import.
        swiftPMDependencies {
            discoverClangModulesImplicitly = false
            iosMinimumDeploymentTarget.set(libs.versions.ios.deploymentTarget.get())
            tvosMinimumDeploymentTarget.set(libs.versions.tvos.deploymentTarget.get())
            macosMinimumDeploymentTarget.set(libs.versions.macos.deploymentTarget.get())
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            localSwiftPackage(
                directory = layout.projectDirectory.dir("apple/FirebaseDataConnectObjC"),
                products = listOf("FirebaseDataConnectObjC"),
            )
        }
    }

    if (supportedPlatforms.contains(TargetPlatform.Js)) {
        js(IR) {
            useCommonJs()
            nodejs {
                testTask {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            }
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                this.apiVersion = libs.versions.settings.api.get()
                this.languageVersion = libs.versions.settings.language.get()
                progressiveMode = true
                if (name.lowercase().contains("ios")
                    || name.lowercase().contains("apple")
                    || name.lowercase().contains("tvos")
                    || name.lowercase().contains("macos")
                    ) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }

        getByName("commonMain") {
            dependencies {
                api(project(":firebase-app"))
                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.datetime)
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(project(":test-utils"))
                implementation(libs.kotlinx.serialization.json)
            }
        }

        if (supportedPlatforms.contains(TargetPlatform.Android)) {
            getByName("androidMain") {
                dependencies {
                    api(libs.google.firebase.dataconnect)
                }
            }
        }

        // JS and Apple run the Firebase JS SDK and the Firebase iOS Data Connect SDK; variables and data cross to them as
        // JSON, encoded and decoded with kotlinx-serialization-json, which stays out of the common API. There is no JVM
        // target: firebase-java-sdk has no Data Connect.
        getByName("nonAndroidMain") {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

// The com.google.firebase.dataconnect classes are header stubs on Android, verified against the Data Connect SDK and stripped;
// on the other platforms they are the module's own classes over the platform SDK (there is no JVM target).
stripHeaderStubs(
    packageDirs = listOf("com/google/firebase/dataconnect"),
    androidReferenceJars = files({
        configurations.findByName("releaseCompileClasspath")?.incoming?.artifactView {
            attributes.attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
        }?.files ?: files()
    }),
    jvmReferenceJars = files(),
    jvmStubs = false,
)
registerAndroidSourceCompat("firebase-dataconnect/api.txt")

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "dev.gitlive",
        artifactId = "firebase-dataconnect",
        version = project.version.toString()
    )

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
