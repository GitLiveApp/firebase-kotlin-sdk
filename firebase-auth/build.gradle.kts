import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import compat.GeneratedAndroidSdkApi
import compat.registerAndroidSourceCompat
import utils.TargetPlatform
import utils.applyFirebaseHierarchy
import utils.stripHeaderStubs
import utils.supportsApple
import utils.toTargetPlatforms

/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

val supportedPlatforms = (project.property("firebase-auth.supportedTargets") as String).toTargetPlatforms()

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("testOptionsConvention")
    alias(libs.plugins.publish)
}

if (supportedPlatforms.contains(TargetPlatform.Android)) {
    android {
        val compileSdkVersion: Int by project

        compileSdk = compileSdkVersion
        namespace = "dev.gitlive.firebase.auth"

        defaultConfig {
            minSdk =
                23 // Auth has a MinSDK of 23. See https://github.com/firebase/firebase-android-sdk/issues/5927#issuecomment-2093466572
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        sourceSets.getByName("main").java.srcDir("src/androidMain/java")
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
        iosX64().enableKeychainForTests()
        iosSimulatorArm64().enableKeychainForTests()
    }
    if (supportedPlatforms.contains(TargetPlatform.Tvos)) {
        tvosArm64()
        tvosX64().enableKeychainForTests()
        tvosSimulatorArm64().enableKeychainForTests()
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
                baseName = "FirebaseAuth"
            }
            noPodspec()
            pod("FirebaseAuth") {
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
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                if (name.lowercase().contains("ios")
                    || name.lowercase().contains("apple")
                    || name.lowercase().contains("tvos")
                    || name.lowercase().contains("macos")
                ) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                    optIn("kotlinx.cinterop.BetaInteropApi")
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

        // The header stubs and the wrapper's Android code are shared by the Android and JVM targets (src/androidJvmMain);
        // androidMain and jvmMain only hold the seams where firebase-java-sdk differs from the Android SDK.
        if (supportedPlatforms.contains(TargetPlatform.Android)) {
            getByName("androidMain") {
                kotlin.srcDir("src/androidJvmMain/kotlin")
                dependencies {
                    api(libs.google.firebase.auth)
                }
            }
        }

        if (supportedPlatforms.contains(TargetPlatform.Jvm)) {
            getByName("jvmMain") {
                kotlin.srcDir("src/androidJvmMain/kotlin")
            }
            project.extensions.getByType<SourceSetContainer>().getByName("jvmMain").java.srcDir("src/jvmMain/java")
        }

        // The test written against the com.google.firebase.auth API is shared by every test target but the JVM: firebase-java-sdk
        // ports only part of the Android auth API (see api/jvm/firebase-java-sdk-missing.txt), so it does not compile there.
        val androidSdkCompatTest = "src/androidSdkCompatTest/kotlin"
        if (supportedPlatforms.contains(TargetPlatform.Js) || supportedPlatforms.supportsApple()) {
            getByName("nonJvmTest") { kotlin.srcDir(androidSdkCompatTest) }
        }

        if (supportedPlatforms.contains(TargetPlatform.Android)) {
            getByName("androidUnitTest") { kotlin.srcDir(androidSdkCompatTest) }
            getByName("androidInstrumentedTest") {
                kotlin.srcDir(androidSdkCompatTest)
                dependencies {
                    // phone auth registers an sms retriever via ContextCompat.registerReceiver(
                    // Context, BroadcastReceiver, IntentFilter, Int), which firebase-auth calls but
                    // does not pull in - without this the test apk resolves androidx.core 1.2.0 and
                    // PhoneAuthTest fails with a NoSuchMethodError
                    implementation(libs.androidx.core)
                }
            }
        }
    }
}

if (supportedPlatforms.supportsApple()) {
    tasks.create<Exec>("launchIosSimulator") {
        commandLine("open", "-a", "Simulator")
    }

    tasks.withType<KotlinNativeSimulatorTest>().configureEach {
        dependsOn("launchIosSimulator")
        standalone.set(false)
        device.set("booted")
    }
}

fun KotlinNativeTargetWithSimulatorTests.enableKeychainForTests() {
    testRuns.configureEach {
        executionSource.binary.linkerOpts(
            "-sectcreate",
            "__TEXT",
            "__entitlements",
            file("$projectDir/src/commonTest/resources/entitlements.plist").absolutePath
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

stripHeaderStubs(
    packageDirs = listOf("com/google/firebase"),
    androidReferenceJars = files({
        configurations.findByName("releaseCompileClasspath")?.incoming?.artifactView {
            attributes.attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
        }?.files ?: files()
    }),
    jvmReferenceJars = files({ configurations.findByName("jvmCompileClasspath")?.files ?: files() }),
    // Shipped: the static members this module's own code reaches through AuthStatics.java.
    keepClasses = listOf(
        "com/google/firebase/auth/AuthInternalsKt.class",
        "com/google/firebase/auth/AuthStatics.class",
    ),
    // firebase-java-sdk ports a subset of the Android SDK's auth API; the members it lacks are listed in the file.
    jvmMissingMembersFile = file("api/jvm/firebase-java-sdk-missing.txt"),
    // firebase-java-sdk has no AuthKt facade (Firebase.auth), so this module's own is shipped on the JVM.
    jvmKeepClasses = listOf("com/google/firebase/auth/AuthKt.class"),
)
// firebase-auth is not open source, so its api.txt is generated from the published classes rather than downloaded.
registerAndroidSourceCompat(
    generated = listOf(
        GeneratedAndroidSdkApi(
            name = "firebase-auth",
            artifacts = listOf("com.google.firebase:firebase-auth", "com.google.firebase:firebase-auth-interop"),
            packages = listOf("com/google/firebase/auth/"),
        ),
    ),
)

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "dev.gitlive",
        artifactId = "firebase-auth",
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
