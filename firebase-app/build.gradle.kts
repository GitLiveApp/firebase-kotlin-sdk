/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */
version = project.property("firebase-app.version") as String


plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

android {
}

kotlin {
    js {
        useCommonJs()
        nodejs()
        browser {
            testTask {
                enabled = true
                useKarma()
            }
        }
    }

    android()

    iosArm64()

    iosX64 {
        binaries {
            getTest("DEBUG").apply {
                linkerOpts("-ObjC")
                linkerOpts("-framework", "FirebaseCore")
                linkerOpts("-framework", "FirebaseCoreDiagnostics")
                linkerOpts("-framework", "GoogleUtilities")
                linkerOpts("-framework", "GoogleDataTransport")
                linkerOpts("-framework", "GoogleUtilities")
                linkerOpts("-framework", "FBLPromises")
                linkerOpts("-framework", "nanopb")
                linkerOpts("-F$buildDir/bin/iosX64/debugTest/Frameworks")
            }
        }
    }


    cocoapods {
        summary = "Firebase SDK For Kotlin"
        homepage = "https://github.com/GitLiveApp/firebase-kotlin-sdk"
        ios.deploymentTarget = "8.0"
        frameworkName = "firebase-app"
        pod("FirebaseCore")
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":firebase-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-common:19.3.1")
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
                implementation("androidx.test:core:1.3.0")
            }
        }
    }
}