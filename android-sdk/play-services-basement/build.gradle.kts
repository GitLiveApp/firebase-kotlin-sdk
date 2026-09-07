plugins {
    id("dev.gitlive.relocated-jvm-artifact")
}

// play-services-basement is shared with the rest of Play Services and stays as it is; only the
// com.google.firebase classes it contains (FirebaseException & co.) are republished relocated.
relocatedArtifact {
    original.set("com.google.android.gms:play-services-basement:18.9.0")
}
