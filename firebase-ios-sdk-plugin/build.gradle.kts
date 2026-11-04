plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.publish)
}

group = "dev.gitlive"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    testImplementation(kotlin("test"))
}

// Bakes this repo's own pinned Firebase iOS SDK version + checksum into the plugin at build time, so every
// consumer - this repo's 12 modules and external users alike - gets exactly the version this release of the
// Kotlin SDK was built and tested against, with nothing to configure. Bumping gradle/libs.versions.toml is the
// only thing that ever needs to happen; the plugin picks it up automatically the next time it's built.
val generateFirebaseIosSdkVersion by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/firebaseIosSdkVersion/kotlin")
    val version = libs.versions.firebase.ios.sdk.version
    val sha256 = libs.versions.firebase.ios.sdk.sha256
    inputs.property("version", version)
    inputs.property("sha256", sha256)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().asFile.resolve("dev/gitlive/firebase/iossdk/gradle/GeneratedFirebaseIosSdkVersion.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package dev.gitlive.firebase.iossdk.gradle

            // Generated at build time from gradle/libs.versions.toml - do not edit directly.
            internal const val FIREBASE_IOS_SDK_VERSION = "${version.get()}"
            internal const val FIREBASE_IOS_SDK_SHA256 = "${sha256.get()}"

            """.trimIndent(),
        )
    }
}

sourceSets {
    main {
        kotlin.srcDir(generateFirebaseIosSdkVersion)
    }
}

gradlePlugin {
    plugins {
        create("firebaseIosSdk") {
            id = "dev.gitlive.firebase-ios-sdk"
            displayName = "Firebase iOS SDK"
            description = "Vendors the official Firebase iOS SDK XCFrameworks and wires up Kotlin/Native cinterop " +
                "for them, without requiring CocoaPods (which stopped publishing Firebase 13+)."
            implementationClass = "dev.gitlive.firebase.iossdk.gradle.FirebaseIosSdkPlugin"
        }
    }
}

tasks.withType<Sign>().configureEach {
    onlyIf { !project.gradle.startParameter.taskNames.any { "MavenLocal" in it } }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "dev.gitlive",
        artifactId = "firebase-ios-sdk-gradle-plugin",
        version = project.version.toString()
    )

    pom {
        name.set("firebase-kotlin-sdk")
        description.set(
            "Downloads, verifies and wires up the official Firebase iOS SDK XCFrameworks for Kotlin/Native " +
                "cinterop, as a replacement for the kotlin.native.cocoapods plugin now that Firebase no longer " +
                "publishes to CocoaPods.",
        )
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
