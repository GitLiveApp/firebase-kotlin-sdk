import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import compat.GeneratedAndroidSdkApi
import compat.registerAndroidSourceCompat
import utils.TargetPlatform
import utils.applyFirebaseHierarchy
import utils.stripHeaderStubs
import utils.supportsApple
import utils.toTargetPlatforms

/*
 * Copyright (c) 2023 GitLive Ltd. Use of this source code is governed by the Apache 2.0 license.
 */

val supportedPlatforms = (project.property("firebase-analytics.supportedTargets") as String).toTargetPlatforms()

plugins {
    id("com.android.library")
    kotlin("native.cocoapods")
    kotlin("multiplatform")
    id("testOptionsConvention")
    alias(libs.plugins.publish)
}

if (supportedPlatforms.contains(TargetPlatform.Android)) {
    android {
        val minSdkVersion: Int by project
        val compileSdkVersion: Int by project

        compileSdk = compileSdkVersion
        namespace = "dev.gitlive.firebase.analytics"

        defaultConfig {
            minSdk = minSdkVersion
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        testOptions.configureTestOptions(project)
        packaging {
            resources.pickFirsts.add("META-INF/kotlinx-serialization-core.kotlin_module")
            resources.pickFirsts.add("META-INF/AL2.0")
            resources.pickFirsts.add("META-INF/LGPL2.1")
        }
        lint {
            abortOnError = false
        }
    }
}

kotlin {
    explicitApi()
    applyFirebaseHierarchy()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    if (this is KotlinJvmCompilerOptions) {
                        jvmTarget = JvmTarget.JVM_17
                    }
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    if (supportedPlatforms.contains(TargetPlatform.Android)) {
        @Suppress("OPT_IN_USAGE")
        androidTarget {
            instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
            unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
            publishAllLibraryVariants()
        }
    }

    if (supportedPlatforms.contains(TargetPlatform.Jvm)) {
        jvm()
    }

    if (supportedPlatforms.contains(TargetPlatform.Ios)) {
        iosArm64()
        iosX64()
        iosSimulatorArm64()
    }
    if (supportedPlatforms.contains(TargetPlatform.Tvos)) {
        tvosArm64()
        tvosX64()
        tvosSimulatorArm64()
    }
    if (supportedPlatforms.contains(TargetPlatform.Macos)) {
        macosArm64()
        macosX64()
    }
    if (supportedPlatforms.supportsApple()) {
        cocoapods {
            if (supportedPlatforms.contains(TargetPlatform.Ios)) {
                ios.deploymentTarget = libs.versions.ios.deploymentTarget.get()
            }
            if (supportedPlatforms.contains(TargetPlatform.Tvos)) {
                tvos.deploymentTarget = libs.versions.tvos.deploymentTarget.get()
            }
            if (supportedPlatforms.contains(TargetPlatform.Macos)) {
                osx.deploymentTarget = libs.versions.macos.deploymentTarget.get()
            }
            framework {
                baseName = "FirebaseAnalytics"
            }
            noPodspec()
            pod("FirebaseAnalytics") {
                version = libs.versions.firebase.cocoapods.get()
                extraOpts += listOf("-compiler-option", "-fmodules")
            }
        }
    }

    if (supportedPlatforms.contains(TargetPlatform.Js)) {
        js(IR) {
            useCommonJs()
            nodejs {
                testTask {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            }
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                    }
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                this.apiVersion = libs.versions.settings.api.get()
                this.languageVersion = libs.versions.settings.language.get()
                progressiveMode = true
                if (name.lowercase().contains("ios")
                    || name.lowercase().contains("apple")
                    || name.lowercase().contains("tvos")
                    || name.lowercase().contains("macos")
                    ) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }

        getByName("commonMain") {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(project(":test-utils"))
            }
        }

        if (supportedPlatforms.contains(TargetPlatform.Android)) {
            getByName("androidMain") {
                dependencies {
                    api(libs.google.firebase.analytics)
                }
            }
        }

        // The test written against the com.google.firebase.analytics API is shared by every test target but the JVM, where
        // analytics is a no-op and firebase-java-sdk's android.os.Bundle has none of the setters it uses.
        val androidSdkCompatTest = "src/androidSdkCompatTest/kotlin"
        if (supportedPlatforms.contains(TargetPlatform.Js) || supportedPlatforms.supportsApple()) {
            getByName("nonJvmTest") { kotlin.srcDir(androidSdkCompatTest) }
        }
        if (supportedPlatforms.contains(TargetPlatform.Android)) {
            getByName("androidUnitTest") { kotlin.srcDir(androidSdkCompatTest) }
            getByName("androidInstrumentedTest") { kotlin.srcDir(androidSdkCompatTest) }
        }
    }
}

// android.os.Bundle and the com.google.firebase.analytics classes are header stubs on Android (verified against the platform and
// the Analytics SDK). firebase-java-sdk has no analytics, so the JVM ships the module's own no-op implementation of the
// com.google.firebase.analytics classes and only stubs android.os.Bundle (whose members it mostly lacks: they are never called).
stripHeaderStubs(
    packageDirs = listOf("com/google/firebase", "android/os"),
    androidReferenceJars = files({
        val classpath = configurations.findByName("releaseCompileClasspath")?.incoming?.artifactView {
            attributes.attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
        }?.files ?: files()
        val bootClasspath = extensions.findByType<com.android.build.gradle.LibraryExtension>()?.bootClasspath ?: emptyList<File>()
        classpath + files(bootClasspath)
    }),
    jvmReferenceJars = files({ configurations.findByName("jvmCompileClasspath")?.files ?: files() }),
    jvmKeepClasses = listOf("com/google/firebase/analytics/"),
    jvmMissingMembers = listOf(
        "android/os/Bundle.putString(Ljava/lang/String;Ljava/lang/String;)V",
        "android/os/Bundle.putInt(Ljava/lang/String;I)V",
        "android/os/Bundle.putLong(Ljava/lang/String;J)V",
        "android/os/Bundle.putDouble(Ljava/lang/String;D)V",
        "android/os/Bundle.putBoolean(Ljava/lang/String;Z)V",
        "android/os/Bundle.putBundle(Ljava/lang/String;Landroid/os/Bundle;)V",
        "android/os/Bundle.putAll(Landroid/os/Bundle;)V",
        "android/os/Bundle.getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
        "android/os/Bundle.getInt(Ljava/lang/String;I)I",
        "android/os/Bundle.getLong(Ljava/lang/String;)J",
        "android/os/Bundle.getLong(Ljava/lang/String;J)J",
        "android/os/Bundle.getDouble(Ljava/lang/String;)D",
        "android/os/Bundle.getDouble(Ljava/lang/String;D)D",
        "android/os/Bundle.getBoolean(Ljava/lang/String;Z)Z",
        "android/os/Bundle.getBundle(Ljava/lang/String;)Landroid/os/Bundle;",
        "android/os/Bundle.size()I",
        "android/os/Bundle.isEmpty()Z",
        "android/os/Bundle.remove(Ljava/lang/String;)V",
        "android/os/Bundle.clear()V",
        "android/os/Bundle.<init>()V",
    ),
)
// firebase-analytics is not open source, so its api.txt is generated from the published classes rather than downloaded.
registerAndroidSourceCompat(
    generated = listOf(
        GeneratedAndroidSdkApi(
            name = "firebase-analytics",
            artifacts = listOf("com.google.android.gms:play-services-measurement-api"),
            packages = listOf("com/google/firebase/analytics/"),
        ),
    ),
)

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "dev.gitlive",
        artifactId = "firebase-analytics",
        version = project.version.toString()
    )

    pom {
        name.set("firebase-kotlin-sdk")
        description.set("The Firebase Kotlin SDK is a Kotlin-first SDK for Firebase. It's API is similar to the Firebase Android SDK Kotlin Extensions but also supports multiplatform projects, enabling you to use Firebase directly from your common source targeting iOS, Android or JS.")
        url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
        inceptionYear.set("2019")

        scm {
            url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
            connection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
            developerConnection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
            tag.set("HEAD")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk/issues")
        }

        developers {
            developer {
                name.set("Nicholas Bransby-Williams")
                email.set("nbransby@gmail.com")
            }
        }

        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
                comments.set("A business-friendly OSS license")
            }
        }
    }
}
