version = project.property("firebase-functions.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
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

    iosX64 {
        binaries {
            getTest("DEBUG").apply {
                linkerOpts("-ObjC")
                linkerOpts("-framework", "FirebaseFunctions")
                linkerOpts("-framework", "FirebaseCore")
                linkerOpts("-framework", "FirebaseCoreDiagnostics")
                linkerOpts("-framework", "GoogleUtilities")
                linkerOpts("-framework", "GoogleDataTransport")
                linkerOpts("-framework", "GTMSessionFetcher")
                linkerOpts("-framework", "nanopb")
                linkerOpts("-framework", "FBLPromises")
                linkerOpts("-F$buildDir/bin/iosX64/debugTest/Frameworks")
            }
        }

    }
    iosArm64()
    cocoapods {
        summary = "Firebase SDK For Kotlin"
        homepage = "https://github.com/GitLiveApp/firebase-kotlin-sdk"
        ios.deploymentTarget = "8.0"
        frameworkName = "firebase-functions"
        pod("FirebaseFunctions")
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-functions:19.1.0")
            }
        }
    }
}