version = "0.1.0"

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
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
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
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-database:17.0.0")
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
                val firebaseDatabase by cinterops.creating {
                    packageName("cocoapods.FirebaseDatabase")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseDatabase.def"))
                    compilerOpts("-F$projectDir/../build/Firebase/FirebaseDatabase")
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

