import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    `maven-publish`
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
    // jvm {
    //     val main by compilations.getting {
    //         kotlinOptions {
    //             jvmTarget = "1.8"
    //         }
    //     }
    // }
    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios")

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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.20.0")
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                api("com.google.firebase:firebase-database:19.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
            }
        }
        // val jvmMain by getting {
        //     dependencies {
        //         implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
        //     }
        //     kotlin.srcDir("src/androidMain/kotlin")
        // }
        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
            }
        }

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
                val firebaseDatabase by cinterops.creating {
                    packageName("cocoapods.FirebaseDatabase")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseDatabase.def"))
                    compilerOpts("-F$projectDir/src/iosMain/c_interop/modules/FirebaseDatabase-6.0.0")
                }
            }
        }

        cocoapods {
            summary = ""
            homepage = ""
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