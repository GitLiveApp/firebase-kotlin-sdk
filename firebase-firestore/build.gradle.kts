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
        useCommonJs()
        nodejs()
        browser {
            testTask {
                enabled = true
                useKarma()
            }
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }

    ios()
    cocoapods {
        summary = "Firebase SDK For Kotlin"
        homepage = "https://github.com/GitLiveApp/firebase-kotlin-sdk"
        ios.deploymentTarget = "8.0"
        frameworkName = "firebase-firestore"
        pod("FirebaseFirestore")
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
                api("com.google.firebase:firebase-firestore:21.5.0")
                implementation("com.android.support:multidex:1.0.3")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
