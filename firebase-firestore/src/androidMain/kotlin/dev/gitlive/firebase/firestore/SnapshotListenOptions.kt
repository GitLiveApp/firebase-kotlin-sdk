package dev.gitlive.firebase.firestore

import android.app.Activity
import com.google.android.gms.tasks.TaskExecutors
import java.util.concurrent.Executor
import com.google.firebase.firestore.ListenSource as AndroidListenSource
import com.google.firebase.firestore.MetadataChanges as AndroidMetadataChanges
import com.google.firebase.firestore.SnapshotListenOptions as AndroidSnapshotListenOptions

public actual data class SnapshotListenOptions(
    public actual val listenSource: ListenSource,
    public actual val metadataChanges: MetadataChanges,
    public val activity: Activity?,
    public val executor: Executor,
) {
    public actual class Builder actual constructor() {
        public actual var listenSource: ListenSource = ListenSource.DEFAULT
        public actual var metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE
        public var activity: Activity? = null

        public var executor: Executor = TaskExecutors.MAIN_THREAD

        public actual fun build(): SnapshotListenOptions = SnapshotListenOptions(
            listenSource = listenSource,
            metadataChanges = metadataChanges,
            activity = activity,
            executor = executor,
        )
    }

    public val android: AndroidSnapshotListenOptions = AndroidSnapshotListenOptions.Builder()
        .setSource(
            when (listenSource) {
                ListenSource.DEFAULT -> AndroidListenSource.DEFAULT
                ListenSource.CACHE -> AndroidListenSource.CACHE
            },
        )
        .setMetadataChanges(
            when (metadataChanges) {
                MetadataChanges.EXCLUDE -> AndroidMetadataChanges.EXCLUDE
                MetadataChanges.INCLUDE -> AndroidMetadataChanges.INCLUDE
            },
        )
        .setExecutor(executor)
        .apply {
            activity?.let {
                setActivity(it)
            }
        }
        .build()
}
