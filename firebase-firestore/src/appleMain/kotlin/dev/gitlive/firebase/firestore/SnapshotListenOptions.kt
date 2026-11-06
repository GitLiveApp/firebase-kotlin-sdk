package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRListenSource
import cocoapods.FirebaseFirestoreInternal.FIRSnapshotListenOptions

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

    public val ios: FIRSnapshotListenOptions = FIRSnapshotListenOptions().optionsWithSource(
        when (listenSource) {
            ListenSource.DEFAULT -> FIRListenSource.FIRListenSourceDefault
            ListenSource.CACHE -> FIRListenSource.FIRListenSourceCache
        },
    )
        .optionsWithIncludeMetadataChanges(
            when (metadataChanges) {
                MetadataChanges.INCLUDE -> true
                MetadataChanges.EXCLUDE -> false
            },
        )
}
