/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

version = project.property("firebase-common.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

android {
    val minSdkVersion: Int by project
    val compileSdkVersion: Int by project

    compileSdk = compileSdkVersion
    namespace = "dev.gitlive.firebase.common"
    defaultConfig {
        minSdk = minSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
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
        ios()
        iosSimulatorArm64()
    }

    js(IR) {
        useCommonJs()
        nodejs {
            testTask(
                Action {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            )
        }
        browser {
            testTask(
                Action {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            )
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                val apiVersion: String by project
                val languageVersion: String by project
                this.apiVersion = apiVersion
                this.languageVersion = languageVersion
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
            }
        }

        getByName("commonMain") {
            val serializationVersion: String by project

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(project(":test-utils"))
            }
        }

        getByName("androidMain") {
            dependencies {
                api("com.google.firebase:firebase-common-ktx")
            }
        }

        getByName("androidInstrumentedTest") {
            dependencies {
                dependsOn(commonTest)
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

        getByName("jsMain") {
            dependencies {
                api(npm("firebase", "10.4.0"))
            }
        }

        getByName("jvmMain") {
            kotlin.srcDir("src/androidMain/kotlin")
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
            kotlin.srcDir("src/androidAndroidTest/kotlin")
        }
    }
}

if (project.property("firebase-common.skipIosTests") == "true") {
    tasks.forEach {
        if (it.name.contains("ios", true) && it.name.contains("test", true)) { it.enabled = false }
    }
}

if (project.property("firebase-common.skipJsTests") == "true") {
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

