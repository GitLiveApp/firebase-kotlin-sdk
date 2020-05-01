version = "0.1.1"

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
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
    android {
        publishLibraryVariants("release", "debug")
    }
    val iosArm64 = iosArm64()
    val iosX64 = iosX64("ios")
    jvm {
        val main by compilations.getting {
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
                api("com.google.firebase:firebase-firestore:19.0.2")
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("src/androidMain/kotlin")
        }
        val jsMain by getting {}
        val iosMain by getting {}

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
                val firebasefirestore by cinterops.creating {
                    packageName("cocoapods.FirebaseFirestore")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseFirestore.def"))
                    compilerOpts("-F$projectDir/../build/Firebase/FirebaseFirestore")
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
    sign(publishing.publications)
}
