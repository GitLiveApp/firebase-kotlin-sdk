import de.undercouch.gradle.tasks.download.Download
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    kotlin("multiplatform") version "1.3.70" apply false
    id("de.undercouch.download").version("3.4.3")
}

buildscript {
    repositories {
        jcenter()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.2")
        classpath("de.undercouch:gradle-download-task:4.0.4")
    }
}

val targetSdkVersion by extra(28)
val minSdkVersion by extra(14)



tasks {
    val downloadFirebaseZipFile by creating(Download::class) {
        onlyIfModified(true)
        src("https://github.com/firebase/firebase-ios-sdk/releases/download/6.17.0/Firebase-6.17.0.zip")
        dest(File("$buildDir", "Firebase-6.17.0.zip"))
        overwrite(true)

    }

    val unzipFirebase by creating(Copy::class) {
        dependsOn(downloadFirebaseZipFile)
        from(zipTree(downloadFirebaseZipFile.dest))
        into("$buildDir")
        outputs.upToDateWhen { File("$buildDir").exists() }
    }

    val copyIOSFirebaseAuthFramework by creating(Copy::class){
        doFirst {
            project.delete(files("$rootDir/firebase-auth/src/iosMain/c_interop/modules/"))
        }
        dependsOn(unzipFirebase)
        from("$rootDir/$buildDir/Firebase/FirebaseAuth/FirebaseAuth.framework")
        into("$rootDir/firebase-auth/src/iosMain/c_interop/modules/FirebaseAuth.framework")
    }

    val copyIOSFirebaseDatabaseFramework by creating(Copy::class){
        doFirst {
            project.delete(files("$rootDir/firebase-database/src/iosMain/c_interop/modules/"))
        }
        dependsOn(unzipFirebase)
        from("$rootDir/$buildDir/Firebase/FirebaseDatabase/FirebaseDatabase.framework")
        into("$rootDir/firebase-database/src/iosMain/c_interop/modules/FirebaseDatabase.framework")
    }

    val copyIOSFirebaseFirestoreFramework by creating(Copy::class){
        doFirst {
            project.delete(files("$rootDir/firebase-firestore/src/iosMain/c_interop/modules/"))
        }
        dependsOn(unzipFirebase)
        from("$rootDir/$buildDir/Firebase/FirebaseFirestore/FirebaseFirestore.framework")
        into("$rootDir/firebase-firestore/src/iosMain/c_interop/modules/FirebaseFirestore.framework")
    }

    val copyIOSFirebaseFunctionsFramework by creating(Copy::class){
        doFirst {
            project.delete(files("$rootDir/firebase-functions/src/iosMain/c_interop/modules/"))
        }
        dependsOn(unzipFirebase)
        from("$rootDir/$buildDir/Firebase/FirebaseFunctions/FirebaseFunctions.framework")
        into("$rootDir/firebase-functions/src/iosMain/c_interop/modules/FirebaseFunctions.framework")
    }

    val copyIOSFirebaseAppFramework by creating(Copy::class){
        
        doFirst {
            project.delete(files("$rootDir/firebase-app/src/iosMain/c_interop/modules/"))
        }
        
        dependsOn(unzipFirebase)
        from("$rootDir/$buildDir/Firebase/FirebaseAnalytics/FirebaseCore.framework")
        into("$rootDir/firebase-app/src/iosMain/c_interop/modules/FirebaseCore.framework")
    }

}

subprojects {


    group = "dev.gitlive"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/gitliveapp/firebase-java")
            credentials {
                username = project.property("gpr.user") as String
                password = project.property("gpr.key") as String
            }
        }
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


        val publishToNpm by creating(Exec::class) {

            dependsOn(
                copyPackageJson,
                copyJS,
                copySourceMap,
                copyReadMe
            )
            
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
        // create the projects node_modules if they don't exist
        if(!File("$buildDir/node_module").exists()) {
            mkdir("$buildDir/node_module")
        }

        dependencies {
            "commonMainImplementation"(kotlin("stdlib-common"))
            "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.4")
            "jsMainImplementation"(kotlin("stdlib-js"))
            "jsMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.4")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.4")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.4")
            "iosMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.4")
        }
    }

    apply(plugin="maven-publish")
    apply(plugin="signing")
    
    configure<PublishingExtension> {
        
        repositories {
            maven {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = project.property("sonatypeUsername") as String
                    password = project.property("sonatypePassword") as String
                }
            }
        }

        publications.all {
            this as MavenPublication

            pom {

                url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")

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

            }

        }

    }
}

