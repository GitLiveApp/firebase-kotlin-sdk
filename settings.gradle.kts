pluginManagement {
    repositories {
        repositories {
            mavenLocal()
            jcenter()
            gradlePluginPortal()
        }
    }
}
include("firebase-common", "firebase-app", "firebase-firestore", "firebase-database", "firebase-auth", "firebase-functions")
