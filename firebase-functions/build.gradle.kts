<<<<<<< ours
/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

version = project.property("firebase-functions.version") as String
=======
version = "0.2.7"
>>>>>>> theirs

plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

android {
    compileSdk = property("targetSdkVersion") as Int
    defaultConfig {
        minSdk = property("minSdkVersion") as Int
        targetSdk = property("targetSdkVersion") as Int
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
        resources.pickFirsts.add("META-INF/kotlinx-serialization-core.kotlin_module")
        resources.pickFirsts.add("META-INF/AL2.0")
        resources.pickFirsts.add("META-INF/LGPL2.1")
    }
    lint {
        isAbortOnError = false
    }
}

<<<<<<< ours
val KonanTarget.archVariant: String
    get() = if (this is KonanTarget.IOS_X64 || this is KonanTarget.IOS_SIMULATOR_ARM64) {
        "ios-arm64_i386_x86_64-simulator"
    } else {
        "ios-arm64_armv7"
=======
kotlin {
    js {
        compilations.all {
            kotlinOptions {
                moduleKind = "umd"
            }
        }
        nodejs()
        browser()
>>>>>>> theirs
    }

kotlin {

    android {
        publishAllLibraryVariants()
    }
<<<<<<< ours

    val supportIosTarget = project.property("skipIosTarget") != "true"
    if (supportIosTarget) {
        fun nativeTargetConfig(): KotlinNativeTarget.() -> Unit = {
            val nativeFrameworkPaths = listOf(
                rootProject.project("firebase-app").projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build/iOS")
            ).plus(
                listOf(
                    "FirebaseAnalytics",
                    "FirebaseCore",
                    "FirebaseCoreDiagnostics",
                    "FirebaseInstallations",
                    "GoogleAppMeasurement",
                    "GoogleAppMeasurementIdentitySupport",
                    "GoogleDataTransport",
                    "GoogleUtilities",
                    "nanopb",
                    "PromisesObjC"
                ).map {
                    rootProject.project("firebase-app").projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build/$it.xcframework/${konanTarget.archVariant}")
                }
            ).plus(
                listOf(
                    "FirebaseFunctions",
                    "GTMSessionFetcher"
                ).map {
                    projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build/$it.xcframework/${konanTarget.archVariant}")
                }
            )

            binaries {
                getTest("DEBUG").apply {
                    linkerOpts(nativeFrameworkPaths.map { "-F$it" })
                    linkerOpts("-ObjC")
                }
            }

            compilations.getByName("main") {
                cinterops.create("FirebaseFunctions") {
                    compilerOpts(nativeFrameworkPaths.map { "-F$it" })
                    extraOpts = listOf("-compiler-option", "-DNS_FORMAT_ARGUMENT(A)=", "-verbose")
                }
            }
        }

        ios(configure = nativeTargetConfig())
        iosSimulatorArm64(configure = nativeTargetConfig())
    }

    js {
        useCommonJs()
        nodejs {
            testTask {
                useMocha {
                    timeout = "5s"
                }
            }
        }
        browser {
            testTask {
                useMocha {
                    timeout = "5s"
                }
            }
        }
=======
    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios")
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
        )
>>>>>>> theirs
    }

    sourceSets {
        all {
            languageSettings.apply {
                apiVersion = "1.5"
                languageVersion = "1.5"
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-functions")
            }
        }
<<<<<<< ours
=======
        val jvmMain by getting {
            kotlin.srcDir("src/androidMain/kotlin")
        }
        val iosMain by getting {}
        val jsMain by getting {}
>>>>>>> theirs

        if (supportIosTarget) {
            val iosMain by getting
            val iosSimulatorArm64Main by getting
            iosSimulatorArm64Main.dependsOn(iosMain)

            val iosTest by sourceSets.getting
            val iosSimulatorArm64Test by sourceSets.getting
            iosSimulatorArm64Test.dependsOn(iosTest)
        }

        val jsMain by getting
    }
}

if (project.property("firebase-functions.skipIosTests") == "true") {
    tasks.forEach {
        if (it.name.contains("ios", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
