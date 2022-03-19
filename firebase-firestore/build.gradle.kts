/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

version = project.property("firebase-firestore.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.5.31"
}

android {
    compileSdk = property("targetSdkVersion") as Int
    defaultConfig {
        minSdk = property("minSdkVersion") as Int
        targetSdk = property("targetSdkVersion") as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
        getByName("androidTest"){
            java.srcDir(file("src/androidAndroidTest/kotlin"))
            manifest.srcFile("src/androidAndroidTest/AndroidManifest.xml")
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
        resources.pickFirsts.add("androidsupportmultidexversion.txt")
    }
    lint {
        isAbortOnError = false
    }
}

val KonanTarget.archVariant: String
    get() = if (this is KonanTarget.IOS_X64 || this is KonanTarget.IOS_SIMULATOR_ARM64) {
        "ios-arm64_i386_x86_64-simulator"
    } else {
        "ios-arm64_armv7"
    }

kotlin {

    android {
        publishAllLibraryVariants()
    }

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
                    "abseil",
                    "BoringSSL-GRPC",
                    "FirebaseFirestore",
                    "gRPC-C++",
                    "gRPC-Core",
                    "leveldb-library"
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
                cinterops.create("FirebaseFirestore") {
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
                api("com.google.firebase:firebase-firestore")
            }
        }

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

if (project.property("firebase-firestore.skipIosTests") == "true") {
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
