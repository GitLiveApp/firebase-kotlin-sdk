package dev.gitlive.firebase.firestore

import kotlin.js.Json
import kotlin.js.json

public actual data class SnapshotListenOptions(
    public actual val listenSource: ListenSource,
    public actual val metadataChanges: MetadataChanges,
) {
    public actual class Builder actual constructor() {
        public actual var listenSource: ListenSource = ListenSource.DEFAULT
        public actual var metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE

        public actual fun build(): SnapshotListenOptions = SnapshotListenOptions(
            listenSource = listenSource,
            metadataChanges = metadataChanges,
        )
    }

    public val js: Json = json(
        "includeMetadataChanges" to when (metadataChanges) {
            MetadataChanges.INCLUDE -> true
            MetadataChanges.EXCLUDE -> false
        },
        "source" to when (listenSource) {
            ListenSource.DEFAULT -> "default"
            ListenSource.CACHE -> "cache"
        },
    )
}
