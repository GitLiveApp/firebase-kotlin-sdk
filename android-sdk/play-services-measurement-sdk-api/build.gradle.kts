plugins {
    id("dev.gitlive.relocated-android-artifact")
}

// Contains the com.google.firebase.analytics classes and references firebase-common, so it is republished relocated
// (its com.google.android.gms classes keep their names; an app must not also depend on the original).
relocatedArtifact {
    original.set("com.google.android.gms:play-services-measurement-sdk-api:23.2.0")
}
