/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version "1.3.70"
    `maven-publish`
}
repositories {
    mavenCentral()
    google()
}

version = "0.1.1-dev"

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
}

kotlin {
    js {
        val main by compilations.getting {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }

    val iosArm32 = iosArm32()
    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios")

    // jvm {
    //     val main by compilations.getting {
    //         kotlinOptions {
    //             jvmTarget = "1.8"
    //         }
    //     }
    //     val test by compilations.getting {
    //         kotlinOptions {
    //             jvmTarget = "1.8"
    //         }
    //     }
    // }

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
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.20.0")
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                api("com.google.firebase:firebase-common:19.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        // val jvmMain by getting {
        //     dependencies {
        //         api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
        //     }
        // }

        val jsMain by getting {
            dependencies {
//                implementation(npm("firebase", "6.2.3"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
            }
        }

        val iosMain by getting {
            dependsOn(commonMain)
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:0.20.0")
            }
        }
        configure(listOf(iosArm32, iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
            }
        }

        cocoapods {
            summary = "Firebase Core for iOS (plus community support for macOS and tvOS)"
            homepage = "https://github.com/TeamHubApp/firebase-kotlin-multiplatform-sdk"
            //pod("FirebaseCore", "~> 6.3.1")
        }
    }
}

tasks {
    val copyPackageJson by registering(Copy::class) {
        from(file("package.json"))
        into(file("$buildDir/node_module"))
    }

    val copyJS by registering {
        doLast {
            val from = File("$buildDir/classes/kotlin/js/main/${project.name}.js")
            val into = File("$buildDir/node_module/${project.name}.js")
            into.createNewFile()
            into.writeText(from.readText()
                .replace("require('firebase-", "require('@teamhubapp/firebase-")
//                .replace("require('kotlinx-serialization-kotlinx-serialization-runtime')", "require('@teamhubapp/kotlinx-serialization-runtime')")
            )
        }
    }


    val copySourceMap by registering(Copy::class) {
        from(file("$buildDir/classes/kotlin/js/main/${project.name}.js.map"))
        into(file("$buildDir/node_module"))
    }

    val publishToNpm by registering(Exec::class) {
        doFirst {
            mkdir("$buildDir/node_module")
        }

        dependsOn(copyPackageJson, copyJS, copySourceMap)
        workingDir("$buildDir/node_module")
        if(Os.isFamily(Os.FAMILY_WINDOWS)) {
            commandLine("cmd", "/c", "npm publish --registry  http://localhost:4873")
        } else {
            commandLine("npm", "publish", "--registry  http://localhost:4873")
        }
    }
}