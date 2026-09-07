plugins {
    id("dev.gitlive.relocated-android-artifact")
}

relocatedArtifact {
    original.set("com.google.firebase:firebase-database")
}
