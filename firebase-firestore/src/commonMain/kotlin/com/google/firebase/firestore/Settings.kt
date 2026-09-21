/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

/**
 * The settings of a [FirebaseFirestore], mirroring `com.google.firebase.firestore.FirebaseFirestoreSettings` from the
 * Firebase Android SDK: built with [Builder] (or the `firestoreSettings` extension) and applied with
 * `FirebaseFirestore.firestoreSettings` before the instance is used.
 *
 * Not mirrored: the deprecated `persistenceEnabled` and `cacheSizeBytes`; use [Builder.setLocalCacheSettings].
 */
public expect class FirebaseFirestoreSettings {
    /** The backend host. */
    public val host: String

    /** Whether the connection uses SSL. */
    public val isSslEnabled: Boolean

    /** The local cache configuration, or null for the SDK's default. */
    public val cacheSettings: LocalCacheSettings?

    /** The gRPC flow control window in bytes. */
    public val grpcFlowControlWindow: Int

    public companion object {
        /** A cache size that disables garbage collection. */
        public val CACHE_SIZE_UNLIMITED: Long

        /** The default gRPC flow control window. */
        public val DEFAULT_GRPC_FLOW_CONTROL_WINDOW: Int
    }

    public class Builder {
        public constructor()
        public constructor(settings: FirebaseFirestoreSettings)

        /** The backend host. */
        public var host: String

        /** Whether the connection uses SSL. */
        public var isSslEnabled: Boolean

        /** The gRPC flow control window in bytes. */
        public var grpcFlowControlWindow: Int

        public fun setHost(host: String): Builder
        public fun setSslEnabled(value: Boolean): Builder
        public fun setLocalCacheSettings(cacheSettings: LocalCacheSettings): Builder
        public fun setGrpcFlowControlWindow(grpcFlowControlWindow: Int): Builder
        public fun build(): FirebaseFirestoreSettings
    }
}

/** The configuration of the local cache: [PersistentCacheSettings] or [MemoryCacheSettings]. */
public interface LocalCacheSettings

/** A persistent (on-disk) local cache, mirroring the Android SDK's `PersistentCacheSettings`; built with [newBuilder]. */
public expect class PersistentCacheSettings : LocalCacheSettings {
    /** The size the cache is allowed to grow to before garbage collection, or `CACHE_SIZE_UNLIMITED`. */
    public val sizeBytes: Long

    public class Builder {
        /** The size the cache is allowed to grow to before garbage collection; at least 1 MB, or `CACHE_SIZE_UNLIMITED`. */
        public fun setSizeBytes(sizeBytes: Long): Builder
        public fun build(): PersistentCacheSettings
    }

    public companion object {
        public fun newBuilder(): Builder
    }
}

/** An in-memory local cache, mirroring the Android SDK's `MemoryCacheSettings`; built with [newBuilder]. */
public expect class MemoryCacheSettings : LocalCacheSettings {
    /** How the cache is garbage collected. */
    public val garbageCollectorSettings: MemoryGarbageCollectorSettings

    public class Builder {
        public fun setGcSettings(gcSettings: MemoryGarbageCollectorSettings): Builder
        public fun build(): MemoryCacheSettings
    }

    public companion object {
        public fun newBuilder(): Builder
    }
}

/** How an in-memory cache is garbage collected: [MemoryEagerGcSettings] or [MemoryLruGcSettings]. */
public interface MemoryGarbageCollectorSettings

/** Eager garbage collection: documents are dropped as soon as no listener uses them; the Android SDK's `MemoryEagerGcSettings`. */
public expect class MemoryEagerGcSettings : MemoryGarbageCollectorSettings {
    public class Builder {
        public fun build(): MemoryEagerGcSettings
    }

    public companion object {
        public fun newBuilder(): Builder
    }
}

/** Least-recently-used garbage collection up to a size, the Android SDK's `MemoryLruGcSettings`. */
public expect class MemoryLruGcSettings : MemoryGarbageCollectorSettings {
    /** The size the cache is allowed to grow to before garbage collection. */
    public val sizeBytes: Long

    public class Builder {
        /** The size the cache is allowed to grow to before garbage collection; at least 1 MB, or `CACHE_SIZE_UNLIMITED`. */
        public fun setSizeBytes(sizeBytes: Long): Builder
        public fun build(): MemoryLruGcSettings
    }

    public companion object {
        public fun newBuilder(): Builder
    }
}

/** The options of a snapshot listener, mirroring the Android SDK's `SnapshotListenOptions`. */
public class SnapshotListenOptions private constructor(
    /** Whether the listener is notified of metadata-only changes. */
    public val metadataChanges: MetadataChanges,
    /** Whether the listener listens to the backend or the cache only. */
    public val source: ListenSource,
) {
    override fun equals(other: Any?): Boolean = other is SnapshotListenOptions && other.metadataChanges == metadataChanges && other.source == source

    override fun hashCode(): Int = 31 * metadataChanges.hashCode() + source.hashCode()

    override fun toString(): String = "SnapshotListenOptions{metadataChanges=$metadataChanges, source=$source}"

    public class Builder {
        private var metadataChanges = MetadataChanges.EXCLUDE
        private var source = ListenSource.DEFAULT

        public fun setMetadataChanges(metadataChanges: MetadataChanges): Builder {
            this.metadataChanges = metadataChanges
            return this
        }

        public fun setSource(source: ListenSource): Builder {
            this.source = source
            return this
        }

        public fun build(): SnapshotListenOptions = SnapshotListenOptions(metadataChanges, source)
    }
}
