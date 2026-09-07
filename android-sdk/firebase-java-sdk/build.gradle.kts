plugins {
    id("dev.gitlive.relocated-jvm-artifact")
}

// The JVM port of the Firebase Android SDK used by the jvm target; relocated the same way as the AARs.
relocatedArtifact {
    original.set("dev.gitlive:firebase-java-sdk:0.6.3")
}
