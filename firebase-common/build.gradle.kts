/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

version = project.property("firebase-common.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.6.10"
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
        getByName("androidTest").java.srcDir(file("src/androidAndroidTest/kotlin"))
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

val skipIosTarget: Boolean = project.property("skipIosTarget") == "true"
val skipMacOsTarget: Boolean = project.property("skipMacOsTarget") == "true"
val skipTvOsTarget: Boolean = project.property("skipTvOsTarget") == "true"

kotlin {

    android {
        publishAllLibraryVariants()
    }

    if (!skipIosTarget) {
        ios()
        iosSimulatorArm64()
    }

    if (!skipMacOsTarget) {
        macosArm64()
        macosX64()
    }

    if (!skipTvOsTarget) {
        tvos()
        tvosSimulatorArm64()
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
                apiVersion = "1.6"
                languageVersion = "1.6"
                progressiveMode = true
                optIn("kotlin.Experimental")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
            }
        }

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-common")
            }
        }

        val iosMain by getting
        val iosSimulatorArm64Main by getting
        val macosArm64Main by getting
        val macosX64Main by getting
        val tvosMain by getting
        val tvosSimulatorArm64Main by getting

        val nativeMain by creating {
            dependsOn(commonMain)

            iosMain.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
            macosX64Main.dependsOn(this)
            tvosMain.dependsOn(this)
            tvosSimulatorArm64Main.dependsOn(this)
        }

        val iosTest by getting
        val iosSimulatorArm64Test by getting
        val macosArm64Test by getting
        val macosX64Test by getting
        val tvosTest by getting
        val tvosSimulatorArm64Test by getting

        val nativeTest by creating {
            dependsOn(commonMain)

            iosTest.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
            macosArm64Test.dependsOn(this)
            macosX64Test.dependsOn(this)
            tvosTest.dependsOn(this)
            tvosSimulatorArm64Test.dependsOn(this)
        }

        val jsMain by getting {
            dependencies {
                api(npm("firebase", "9.4.1"))
            }
        }
    }
}

if (project.property("firebase-common.skipIosTests") == "true") {
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

