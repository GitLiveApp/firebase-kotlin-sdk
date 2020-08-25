/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

version = project.property("firebase-common.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version "1.4.0"
}

android {
    compileSdkVersion(property("targetSdkVersion") as Int)
    defaultConfig {
        minSdkVersion(property("minSdkVersion") as Int)
        targetSdkVersion(property("targetSdkVersion") as Int)
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
        pickFirst("META-INF/kotlinx-serialization-runtime.kotlin_module")
        pickFirst("META-INF/AL2.0")
        pickFirst("META-INF/LGPL2.1")
    }
    lintOptions {
        isAbortOnError = false
    }
}

kotlin {
    js {
        useCommonJs()
        nodejs()
    }
    android {
        publishLibraryVariants("release", "debug")
    }

    ios()
    iosX64("ios") {

    }

    sourceSets {
        commonMain {
            dependencies {
                api(("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
            }
        }
        commonTest {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))

            }
        }
        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-common:19.3.1")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.9")
            }
        }
        val androidTest by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.9")
            }
        }
        val androidAndroidTest by getting {
            dependencies {
                api(kotlin("test-junit"))
                api("junit:junit:4.13")
                api("androidx.test:core:1.2.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
                api("androidx.test.ext:junit:1.1.1")
                api("androidx.test:runner:1.2.0")
            }
        }
        val jsMain by getting {
            dependencies {
                api(npm("firebase", "7.14.0"))
                api(kotlin("stdlib-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                api(kotlin("test-js"))
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
