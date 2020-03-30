plugins {
    kotlin("multiplatform") version "1.3.70" apply false
}

buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
    }
}

val targetSdkVersion by extra(28)
val androidToolsVersion by extra("29.0.1")
val minSdkVersion by extra(14)

subprojects {

    group = "dev.teamhub.firebase"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
    }

//    tasks.withType<KotlinCompile<*>> {
//        kotlinOptions.freeCompilerArgs += listOf(
//            "-Xuse-experimental=kotlin.Experimental",
//            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
//            "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
//        )
//    }

    afterEvaluate  {
        dependencies {
            "commonMainImplementation"(kotlin("stdlib-common"))
            "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.5")
            "jsMainImplementation"(kotlin("stdlib-js"))

            "jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.5")
            "androidMainImplementation"(kotlin("stdlib-jdk8"))
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.5")
            //"jvmMainImplementation"(kotlin("stdlib-jdk8"))
            //"jvmMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
            //"jvmMainApi"("app.teamhub:firebase-java:0.3.1")
            //"jvmMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.5")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.5")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.5")
        }
    }
}

