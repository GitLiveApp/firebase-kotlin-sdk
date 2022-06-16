/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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

val skipIosTarget: Boolean = project.property("skipIosTarget") == "true"
val skipMacOsTarget: Boolean = project.property("skipMacOsTarget") == "true"
val skipTvOsTarget: Boolean = project.property("skipTvOsTarget") == "true"

kotlin {

    android {
        publishAllLibraryVariants()
    }

    fun nativeTargetConfig(): KotlinNativeTarget.() -> Unit = {
        val nativeFrameworkPaths = konanTarget.firebaseCoreFrameworksPaths(rootProject.project("firebase-app").projectDir).plus(
            konanTarget.carthageXcFrameworksPaths(
                projectDir,
                listOf(
                    "FirebaseABTesting",
                    "FirebaseRemoteConfig"
                )
            )
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

    if (!skipIosTarget) {
        ios(configure = nativeTargetConfig())
        iosSimulatorArm64(configure = nativeTargetConfig())
    }

    if (!skipMacOsTarget) {
        macosArm64(configure = nativeTargetConfig())
        macosX64(configure = nativeTargetConfig())
    }

    if (!skipTvOsTarget) {
        tvos(configure = nativeTargetConfig())
        tvosSimulatorArm64(configure = nativeTargetConfig())
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

    setupHierarchicalSourceSets()

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

        val commonTest by getting

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-config")
            }
        }

        val jsMain by getting
    }
}

if (project.property("firebase-config.skipIosTests") == "true") {
    tasks.forEach {
        if (it.name.contains("ios", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-config.skipMacOsTests") == "true") {
    tasks.forEach {
        if (it.name.contains("macos", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-config.skipTvOsTests") == "true") {
    tasks.forEach {
        if (it.name.contains("tvos", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
