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
        val main by compilations.getting {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
        nodejs()
        browser()
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios") {
        binaries {
            getTest("DEBUG").apply {
                linkerOpts("-F${rootProject.buildDir}/Firebase/FirebaseAnalytics")
                linkerOpts("-F${rootProject.buildDir}/Firebase/FirebaseAuth")
                linkerOpts("-F${rootProject.buildDir}/Firebase/GoogleSignIn")
                linkerOpts("-ObjC")
//                compilerOpts("-framework AppAuth")
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
        )
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
                api("com.google.firebase:firebase-auth:19.1.0")
            }
        }

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
                val firebaseAuth by cinterops.creating {
                    packageName("cocoapods.FirebaseAuth")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseAuth.def"))
                    compilerOpts("-F$projectDir/../build/Firebase/FirebaseAuth")
                    compilerOpts("-F$projectDir/../build/Firebase/GoogleSignIn")
                }
            }
        }

        cocoapods {
            summary = ""
            homepage = ""
        }

    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
