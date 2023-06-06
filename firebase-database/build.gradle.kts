/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

version = project.property("firebase-database.version") as String

plugins {
    id("com.android.library")
    kotlin("native.cocoapods")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.20"
}

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = property("targetSdkVersion") as Int
    defaultConfig {
        minSdk = property("minSdkVersion") as Int
        targetSdk = property("targetSdkVersion") as Int
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

    android {
        publishAllLibraryVariants()
    }

    if (supportIosTarget) {
        ios()
        iosSimulatorArm64()
        cocoapods {
            ios.deploymentTarget = "11.0"
            framework {
                baseName = "FirebaseDatabase"
            }
            noPodspec()
            pod("FirebaseDatabase") {
                version = "10.7.0"
            }
        }
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
                apiVersion = "1.8"
                languageVersion = "1.8"
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.coroutines.FlowPreview")
                optIn("kotlinx.serialization.InternalSerializationApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-database")
            }
        }

        if (supportIosTarget) {
            val iosMain by getting
            val iosSimulatorArm64Main by getting
            iosSimulatorArm64Main.dependsOn(iosMain)
            val iosTest by sourceSets.getting
            val iosSimulatorArm64Test by getting
            iosSimulatorArm64Test.dependsOn(iosTest)
        }

        val jsMain by getting {
            dependencies {
                api(npm("firebase", "9.4.1"))
            }
        }
    }
}

if (project.property("firebase-database.skipIosTests") == "true") {
    tasks.forEach {
        if (it.name.contains("ios", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-database.skipJsTests") == "true") {
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

