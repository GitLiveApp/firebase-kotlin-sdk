import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
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
    // js("reactnative") {
    //     val main by compilations.getting {
    //         kotlinOptions {
    //             moduleKind = "commonjs"
    //         }
    //     }
    // }
    android {
        publishLibraryVariants("release", "debug")
    }
    // jvm {
    //     val main by compilations.getting {
    //         kotlinOptions {
    //             jvmTarget = "1.8"
    //         }
    //     }
    // }

    val iosArm32 = iosArm32()
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
                api("com.google.firebase:firebase-common:19.3.0")
            }
        }

        // val jvmMain by getting {
        //     kotlin.srcDir("src/androidMain/kotlin")
        // }

        configure(listOf(iosArm32, iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
                val firebasecore by cinterops.creating {
                    packageName("cocoapods.FirebaseCore")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseCore.def"))
                    //includeDirs("$projectDir/../native/Avalon/Pods/FirebaseCore/Firebase/Core/Public")
                    compilerOpts("-F$projectDir/src/iosMain/c_interop/modules/FirebaseCore-6.0.4")
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
                .replace("require('kotlinx-serialization-kotlinx-serialization-runtime')", "require('@cachet/kotlinx-serialization-runtime')")
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