plugins {
    kotlin("multiplatform") version "1.3.70" apply false
}

buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.2")
    }
}

val targetSdkVersion by extra(28)
val minSdkVersion by extra(14)

subprojects {

    group = "dev.gitlive"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
    }


    tasks {

        val copyReadMe by registering(Copy::class) {
            from(rootProject.file("README.md"))
            into(file("$buildDir/node_module"))
        }

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
                    .replace("require('firebase-", "require('@gitlive/firebase-")
//                .replace("require('kotlinx-serialization-kotlinx-serialization-runtime')", "require('@gitlive/kotlinx-serialization-runtime')")
                )
            }
        }

        val copySourceMap by registering(Copy::class) {
            from(file("$buildDir/classes/kotlin/js/main/${project.name}.js.map"))
            into(file("$buildDir/node_module"))
        }

        val publishToNpm by registering(Exec::class) {
            doFirst {
                if(!File("$buildDir/node_module").exists()) {
                    mkdir("$buildDir/node_module")
                }
            }

            val buildTask = project.tasks.names.find{
              it.equals("build")
            }

            dependsOn(buildTask, copyPackageJson, copyJS, copySourceMap, copyReadMe)
            workingDir("$buildDir/node_module")
            //commandLine("npm", "publish")
            commandLine("ls")
        }
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
            "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.4")
            "jsMainImplementation"(kotlin("stdlib-js"))
            "jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.4")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.4")
            "jvmMainImplementation"(kotlin("stdlib-jdk8"))
            "jvmMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
            "jvmMainApi"("dev.gitlive:firebase-java:0.3.1")
            "jvmMainApi"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.4")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.4")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.4")
        }
    }

}

