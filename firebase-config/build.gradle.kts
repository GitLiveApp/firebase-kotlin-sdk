/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

version = project.property("firebase-config.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    //id("com.quittle.android-emulator") version "0.2.0"
}

android {
    compileSdk = property("targetSdkVersion") as Int
    defaultConfig {
        minSdk = property("minSdkVersion") as Int
        targetSdk = property("targetSdkVersion") as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
        getByName("androidTest"){
            java.srcDir(file("src/androidAndroidTest/kotlin"))
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
        abortOnError = false
    }
}

// Optional configuration
//androidEmulator {
//    emulator {
//        name("givlive_emulator")
//        sdkVersion(28)
//        abi("x86_64")
//        includeGoogleApis(true) // Defaults to false
//
//    }
//    headless(false)
//    logEmulatorOutput(false)
//}

val KonanTarget.archVariants: List<String>
    get() = if (this is KonanTarget.IOS_X64 || this is KonanTarget.IOS_SIMULATOR_ARM64) {
        listOf("ios-arm64_x86_64-simulator", "ios-arm64_i386_x86_64-simulator")
    } else {
        listOf("ios-arm64", "ios-arm64_arm7")
    }

kotlin {

    android {
        publishAllLibraryVariants()
    }

    val supportIosTarget = project.property("skipIosTarget") != "true"
    if (supportIosTarget) {
        fun nativeTargetConfig(): KotlinNativeTarget.() -> Unit = {
            val nativeFrameworkPaths = listOf(
                "FBLPromises",
                "FirebaseAnalytics",
                "FirebaseAnalyticsSwift",
                "FirebaseCore",
                "FirebaseCoreInternal",
                "FirebaseInstallations",
                "GoogleAppMeasurement",
                "GoogleAppMeasurementIdentitySupport",
                "GoogleUtilities",
                "nanopb"
            ).map { framework ->
                konanTarget.archVariants
                    .map { rootProject.project("firebase-app").projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build/$framework.xcframework/$it") }
                    .firstOrNull { it.exists() }
            }.plus(
                listOf(
                    "FirebaseABTesting",
                    "FirebaseRemoteConfig",
                    "FirebaseRemoteConfigSwift",
                    "FirebaseSharedSwift"
                ).map { framework ->
                    konanTarget.archVariants
                        .map { projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build/$framework.xcframework/$it") }
                        .firstOrNull { it.exists() }
                }
            )

            binaries {
                getTest("DEBUG").apply {
                    linkerOpts(nativeFrameworkPaths.map { "-F$it" })
                    linkerOpts("-ObjC")
                }
            }

            compilations.getByName("main") {
                cinterops.create("FirebaseRemoteConfig") {
                    compilerOpts(nativeFrameworkPaths.map { "-F$it" })
                    extraOpts = listOf("-compiler-option", "-DNS_FORMAT_ARGUMENT(A)=", "-verbose")
                }
            }
        }

        ios(configure = nativeTargetConfig())
        iosSimulatorArm64("ios", configure = nativeTargetConfig())
    }

    js {
        useCommonJs()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                apiVersion = "1.6"
                languageVersion = "1.6"
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
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
                api("com.google.firebase:firebase-config")
            }
        }

        if (supportIosTarget) {
            val iosMain by getting
            val iosTest by sourceSets.getting
        }

        val jsMain by getting
    }
}

if (project.property("firebase-config.skipIosTests") == "true") {
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
