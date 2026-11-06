package dev.gitlive.firebase.firestore

public enum class MetadataChanges {
    INCLUDE,
    EXCLUDE,
}

public enum class ListenSource {
    CACHE,
    DEFAULT,
}

public expect class SnapshotListenOptions {

    public class Builder {

        public constructor()

        public var listenSource: ListenSource
        public var metadataChanges: MetadataChanges

        public fun build(): SnapshotListenOptions
    }

    public val listenSource: ListenSource
    public val metadataChanges: MetadataChanges
}

public fun snapshotListenOptions(
    builder: SnapshotListenOptions.Builder.() -> Unit = {},
): SnapshotListenOptions = SnapshotListenOptions.Builder().apply(builder).build()
