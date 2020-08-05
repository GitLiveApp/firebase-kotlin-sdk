version = project.property("firebase-firestore.version") as String

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
        setMultiDexEnabled(true)
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
        pickFirst("META-INF/kotlinx-serialization-runtime.kotlin_module")
        pickFirst("META-INF/AL2.0")
        pickFirst("META-INF/LGPL2.1")
        pickFirst("androidsupportmultidexversion.txt")
    }
    lintOptions {
        isAbortOnError = false
    }
}

kotlin {
    js {
        val main by compilations.getting {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
        nodejs()
        browser()
    }
    android {
        publishLibraryVariants("release", "debug")
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
                api("com.google.firebase:firebase-firestore:21.4.3")
                implementation("com.android.support:multidex:1.0.3")
            }
        }

        val jsMain by getting {}
        val iosMain by getting {}

        configure(listOf(iosArm64, iosX64)) {
            compilations.getByName("main") {
                source(sourceSets.get("iosMain"))
                val firebasefirestore by cinterops.creating {
                    packageName("cocoapods.FirebaseFirestore")
                    defFile(file("$projectDir/src/iosMain/c_interop/FirebaseFirestore.def"))
                    compilerOpts("-F${rootProject.buildDir}/Firebase/FirebaseFirestore")
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
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
