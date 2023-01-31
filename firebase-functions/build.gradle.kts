/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

version = project.property("firebase-functions.version") as String

plugins {
    id("com.android.library")
    kotlin("native.cocoapods")
    kotlin("multiplatform")
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
        ios {
            binaries {
                framework {
                    baseName = "FirebaseFunctions"
                }
            }
        }
//        iosSimulatorArm64 {
//            binaries {
//                framework {
//                    baseName = "FirebaseFunctions"
//                }
//            }
//        }
        cocoapods {
            ios.deploymentTarget = "11.0"
            framework {
                isStatic = true
            }
            noPodspec()
            pod("FirebaseFunctions") {
                version = "8.15.0"
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
                apiVersion = "1.7"
                languageVersion = "1.7"
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
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
                api("com.google.firebase:firebase-functions")
            }
        }

        if (supportIosTarget) {
            val iosMain by getting
//            val iosSimulatorArm64Main by getting
//            iosSimulatorArm64Main.dependsOn(iosMain)

            val iosTest by sourceSets.getting
//            val iosSimulatorArm64Test by sourceSets.getting
//            iosSimulatorArm64Test.dependsOn(iosTest)
        }

        val jsMain by getting
    }
}

if (project.property("firebase-functions.skipIosTests") == "true") {
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
