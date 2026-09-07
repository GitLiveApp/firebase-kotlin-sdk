/*
 * Aggregate tasks and conventions for the relocated Firebase Android SDK artifacts.
 * Each subproject republishes one original artifact with its com.google.firebase classes moved to the
 * dev.gitlive.firebase.android package (see README.md in this directory).
 */

// Artifacts outside the Firebase group that merely contain a few com.google.firebase classes: the original stays a
// dependency and only those classes are republished relocated.
extra["extractOnlyArtifacts"] = setOf("play-services-basement")

// Firebase artifacts whose original is kept on the classpath next to the relocated copy, because unrelocated
// Play Services / datatransport libraries reference them and their packages do not clash with this SDK.
extra["keepOriginalArtifacts"] = setOf("firebase-annotations", "firebase-encoders", "firebase-encoders-json", "firebase-encoders-proto")
extra["keepOriginalPackages"] = setOf("com.google.firebase.annotations", "com.google.firebase.encoders")

// Play Services artifacts republished fully relocated because they contain or reference Firebase classes; their
// originals are excluded from every transitive graph like the Firebase group itself.
extra["relocatedArtifactsOutsideFirebase"] = setOf(
    "com.google.android.gms:play-services-measurement-api",
    "com.google.android.gms:play-services-measurement-sdk-api",
)

tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes every relocated Firebase Android SDK artifact to Maven Central"
    dependsOn(subprojects.map { "${it.path}:publish" })
}

tasks.register("publishAllToMavenLocal") {
    group = "publishing"
    description = "Publishes every relocated Firebase Android SDK artifact to the local Maven repository"
    dependsOn(subprojects.map { "${it.path}:publishToMavenLocal" })
}

tasks.register("verifyNoUnrelocatedReferences") {
    group = "verification"
    description = "Fails if any dependency of a relocated artifact still references the original Firebase package"
    dependsOn(subprojects.map { "${it.path}:verifyRelocation" })
}
