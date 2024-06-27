import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

version = project.property("firebase-firestore.version") as String

plugins {
    id("com.android.library")
    kotlin("native.cocoapods")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("testOptionsConvention")
}

android {
    val minSdkVersion: Int by project
    val compileSdkVersion: Int by project

    compileSdk = compileSdkVersion
    namespace = "dev.gitlive.firebase.firestore"

    defaultConfig {
        minSdk = minSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions.configureTestOptions()
    packaging {
        resources.pickFirsts.add("META-INF/kotlinx-serialization-core.kotlin_module")
        resources.pickFirsts.add("META-INF/AL2.0")
        resources.pickFirsts.add("META-INF/LGPL2.1")
        resources.pickFirsts.add("androidsupportmultidexversion.txt")
    }
    lint {
        abortOnError = false
    }
}

val supportIosTarget = project.property("skipIosTarget") != "true"

kotlin {
    explicitApi()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    if (this is KotlinJvmCompilerOptions) {
                        jvmTarget = JvmTarget.JVM_17
                    }
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    @Suppress("OPT_IN_USAGE")
    androidTarget {
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        publishAllLibraryVariants()
    }

    jvm()

    if (supportIosTarget) {
        iosArm64()
        iosX64()
        iosSimulatorArm64()
        cocoapods {
            ios.deploymentTarget = "12.0"
            framework {
                baseName = "FirebaseFirestore"
            }
            noPodspec()
            // As of Firebase 10.17 Firestore has moved all ObjC headers to FirebaseFirestoreInternal and the kotlin cocoapods plugin does not handle this well
            // Adding it manually seems to resolve the issue
            pod("FirebaseFirestoreInternal") {
                version = libs.versions.firebase.cocoapods.get()
            }
            pod("FirebaseFirestore") {
                version = libs.versions.firebase.cocoapods.get()
                extraOpts += listOf("-compiler-option", "-fmodules")
                useInteropBindingFrom("FirebaseFirestoreInternal")
            }
        }
    }

    js(IR) {
        useCommonJs()
        nodejs {
            testTask {
                useKarma {
                    useChromeHeadless()
                    // Explicitly specify Mocha here since it seems to be throwing random errors otherwise
                    useMocha {
                        timeout = "180s"
                    }
                }
            }
        }
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    // Explicitly specify Mocha here since it seems to be throwing random errors otherwise
                    useMocha {
                        timeout = "180s"
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
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                if (name.lowercase().contains("ios")) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                    optIn("kotlinx.cinterop.BetaInteropApi")
                }
            }
        }

        getByName("commonMain") {
            dependencies {
                api(project(":firebase-app"))
                api(project(":firebase-common"))
                implementation(project(":firebase-common-internal"))
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(project(":test-utils"))
            }
        }

        getByName("androidMain") {
            dependencies {
                api(libs.google.firebase.firestore)
            }
        }

        getByName("jvmMain") {
            kotlin.srcDir("src/androidMain/kotlin")
        }

    }
}

if (project.property("firebase-firestore.skipIosTests") == "true") {
    tasks.forEach {
        if (it.name.contains("ios", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-firestore.skipJvmTests") == "true") {
    tasks.forEach {
        if (it.name.contains("jvm", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-firestore.skipJsTests") == "true") {
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