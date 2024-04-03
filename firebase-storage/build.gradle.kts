import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

/*
 * Copyright (c) 2023 GitLive Ltd. Use of this source code is governed by the Apache 2.0 license.
 */

version = project.property("firebase-storage.version") as String

plugins {
    id("com.android.library")
    kotlin("native.cocoapods")
    kotlin("multiplatform")
}

android {
    val minSdkVersion: Int by project
    val compileSdkVersion: Int by project

    compileSdk = compileSdkVersion
    namespace = "dev.gitlive.firebase.storage"

    defaultConfig {
        minSdk = minSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }
    packaging {
        resources.pickFirsts.add("META-INF/kotlinx-serialization-core.kotlin_module")
        resources.pickFirsts.add("META-INF/AL2.0")
        resources.pickFirsts.add("META-INF/LGPL2.1")
    }
    lint {
        abortOnError = false
    }
}

val supportIosTarget = project.property("skipIosTarget") != "true"

kotlin {

    targets.configureEach {
        compilations.configureEach {
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }

    @Suppress("OPT_IN_USAGE")
    androidTarget {
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        publishAllLibraryVariants()
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm {
        compilations.getByName("main") {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        compilations.getByName("test") {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    if (supportIosTarget) {
        iosArm64()
        iosX64()
        iosSimulatorArm64()
        cocoapods {
            ios.deploymentTarget = "11.0"
            framework {
                baseName = "FirebaseStorage"
            }
            noPodspec()
            pod("FirebaseStorage") {
                version = "10.23.0"
                extraOpts += listOf("-compiler-option", "-fmodules")
            }
        }
    }

    js(IR) {
        useCommonJs()
        nodejs {
            testTask(
                Action {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            )
        }
        browser {
            testTask(
                Action {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            )
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                val apiVersion: String by project
                val languageVersion: String by project
                this.apiVersion = apiVersion
                this.languageVersion = languageVersion
                progressiveMode = true
                if (name.lowercase().contains("ios")) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }

        getByName("commonMain") {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(project(":test-utils"))
            }
        }

        getByName("androidMain") {
            dependencies {
                api("com.google.firebase:firebase-storage")
            }
        }
    }
}

if (project.property("firebase-storage.skipIosTests") == "true") {
    tasks.forEach {
        if (it.name.contains("ios", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-storage.skipJsTests") == "true") {
    tasks.forEach {
        if (it.name.contains("js", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
