/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

repositories {
    mavenCentral()
    google()
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
}

kotlin {
    js {
        val main by compilations.getting {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }
//    js("reactnative") {
//        val main by compilations.getting {
//            kotlinOptions {
//                moduleKind = "commonjs"
//            }
//        }
//    }
    android {
        publishLibraryVariants("release", "debug")
    }

    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":firebase-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-common:19.2.0")
            }
        }
//        val iosMain by creating

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
                val firebasecore by cinterops.creating {
                    packageName("cocoapods.FirebaseCore")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseCore.def"))
                    //includeDirs("$projectDir/../native/Avalon/Pods/FirebaseCore/Firebase/Core/Public")
                    compilerOpts("-F$projectDir/src/iosMain/c_interop/modules")
                }
            }
        }

        cocoapods {
            summary = ""
            homepage = ""
            //pod("FirebaseCore", "~> 6.3.1")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/gitliveapp/packages")
            credentials {
                username = project.property("gpr.user") as String
                password = project.property("gpr.key") as String
            }
        }
    }
    publications {
        register("gpr", MavenPublication::class) {
            from(components["kotlin"])
        }

    }
}