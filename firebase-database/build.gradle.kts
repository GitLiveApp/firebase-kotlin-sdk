version = project.property("firebase-database.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

repositories {
    mavenCentral()
    google()
    jcenter()
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
    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }
    packagingOptions {
        pickFirst("META-INF/kotlinx-serialization-runtime.kotlin_module")
        pickFirst("META-INF/AL2.0")
        pickFirst("META-INF/LGPL2.1")
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
    iosX64("ios")
    cocoapods {
        summary = "Firebase SDK For Kotlin"
        homepage = "https://github.com/GitLiveApp/firebase-kotlin-sdk"
        ios.deploymentTarget = "8.0"
        frameworkName = "firebase-database"
        pod("FirebaseDatabase", "~> 6.30.0")
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
                api("com.google.firebase:firebase-database:19.3.1")
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
