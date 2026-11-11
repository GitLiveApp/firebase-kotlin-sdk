package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SnapshotListenOptions

public class SnapshotListenOptionsBuilder {
    public constructor()

    public var listenSource: ListenSource = ListenSource.DEFAULT
    public var metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE

    internal fun build(): SnapshotListenOptions = SnapshotListenOptions.Builder().apply {
        setSource(listenSource)
        setMetadataChanges(metadataChanges)
    }.build()
}

public fun snapshotListenOptions(
    builder: SnapshotListenOptionsBuilder.() -> Unit = {},
): SnapshotListenOptions = SnapshotListenOptionsBuilder().apply(builder).build()
