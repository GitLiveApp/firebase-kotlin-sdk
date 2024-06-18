/*
 * Copyright (c) 2023 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// this project is used only in tests to share common code. publishing is disabled in the root build.gradle.kts

version = "0.0.1"

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

android {
    val minSdkVersion: Int by project
    val compileSdkVersion: Int by project

    compileSdk = compileSdkVersion
    namespace = "dev.gitlive.firebase.testUtils"

    defaultConfig {
        minSdk = minSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

kotlin {

    targets.configureEach {
        compilations.configureEach {
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }

    @Suppress("OPT_IN_USAGE")
    androidTarget {
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

    val supportIosTarget = project.property("skipIosTarget") != "true"

    if (supportIosTarget) {
        iosArm64()
        iosX64()
        iosSimulatorArm64()
    }

    js(IR) {
        useCommonJs()
        nodejs()
        browser()
    }

    sourceSets {
        all {
            languageSettings.apply {
                this.apiVersion = libs.versions.settings.api.get()
                this.languageVersion = libs.versions.settings.language.get()
                progressiveMode = true
                if (name.lowercase().contains("ios")) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                    optIn("kotlinx.cinterop.BetaInteropApi")
                }
            }
        }

        getByName("commonMain") {
            dependencies {
                api(kotlin("test"))
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.coroutines.test)
            }
        }

        getByName("jvmMain") {
            kotlin.srcDir("src/androidMain/kotlin")
        }

        getByName("jsMain") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
