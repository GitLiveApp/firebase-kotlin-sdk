/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

<<<<<<< ours
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

version = project.property("firebase-common.version") as String
=======
version = "0.2.8"
>>>>>>> theirs

plugins {
    id("com.android.library")
    kotlin("multiplatform")
<<<<<<< ours
    kotlin("plugin.serialization") version "1.5.31"
=======
    kotlin("native.cocoapods")
<<<<<<< ours
    kotlin("plugin.serialization") version "1.6.0"
>>>>>>> theirs
=======
    kotlin("plugin.serialization") version "1.3.72"
>>>>>>> theirs
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
        isAbortOnError = false
    }
}

kotlin {
<<<<<<< ours

=======
    js {
        compilations.all {
            kotlinOptions {
                moduleKind = "umd"
            }
        }
        nodejs()
    }
>>>>>>> theirs
    android {
        publishAllLibraryVariants()
    }

    val supportIosTarget = project.property("skipIosTarget") != "true"

<<<<<<< ours
    if (supportIosTarget) {
        ios()
        iosSimulatorArm64()
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
=======
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        val test by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
        )
>>>>>>> theirs
    }

    sourceSets {
        all {
            languageSettings.apply {
                apiVersion = "1.5"
                languageVersion = "1.5"
                progressiveMode = true
                optIn("kotlin.Experimental")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")
            }
        }

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-common")
            }
        }

        if (supportIosTarget) {
            val iosMain by getting
            val iosSimulatorArm64Main by getting
            iosSimulatorArm64Main.dependsOn(iosMain)

            val iosTest by sourceSets.getting
            val iosSimulatorArm64Test by sourceSets.getting
            iosSimulatorArm64Test.dependsOn(iosTest)
        }
<<<<<<< ours

        val jsMain by getting {
=======
        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
            }
            kotlin.srcDir("src/androidMain/kotlin")
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
            kotlin.srcDir("src/androidTest/kotlin")
        }
        val iosMain by getting {
>>>>>>> theirs
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

