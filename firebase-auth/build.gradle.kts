version = project.property("firebase-auth.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    //id("com.quittle.android-emulator") version "0.2.0"
}

//buildscript {
//    repositories {
//        jcenter()
//        google()
//        gradlePluginPortal()
//    }
//    dependencies {
//        classpath("com.android.tools.build:gradle:3.6.1")
//    }
//}

android {
    compileSdkVersion(property("targetSdkVersion") as Int)
    defaultConfig {
        minSdkVersion(property("minSdkVersion") as Int)
        targetSdkVersion(property("targetSdkVersion") as Int)
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
        pickFirst("META-INF/kotlinx-serialization-runtime.kotlin_module")
        pickFirst("META-INF/AL2.0")
        pickFirst("META-INF/LGPL2.1")
    }
    lintOptions {
        isAbortOnError = false
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

kotlin {
    js {
        useCommonJs()
        nodejs()
        browser()
    }
    android {
        publishLibraryVariants("release", "debug")
    }

    ios()
    iosX64("ios")
    cocoapods {
        summary = "Firebase SDK For Kotlin"
        homepage = "https://github.com/GitLiveApp/firebase-kotlin-sdk"
        ios.deploymentTarget = "8.0"
        frameworkName = "firebase-auth"
        pod("FirebaseAuth", "~> 6.30.0")
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
                api("com.google.firebase:firebase-auth:19.3.2")
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
