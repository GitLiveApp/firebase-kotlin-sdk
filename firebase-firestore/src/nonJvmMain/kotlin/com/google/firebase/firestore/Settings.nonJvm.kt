/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import dev.gitlive.firebase.firestore.internal.CACHE_SIZE_UNLIMITED
import dev.gitlive.firebase.firestore.internal.DEFAULT_CACHE_SIZE_BYTES
import dev.gitlive.firebase.firestore.internal.DEFAULT_GRPC_FLOW_CONTROL_WINDOW
import dev.gitlive.firebase.firestore.internal.DEFAULT_HOST
import dev.gitlive.firebase.firestore.internal.MINIMUM_CACHE_BYTES

public actual class FirebaseFirestoreSettings private constructor(
    public actual val host: String,
    public actual val isSslEnabled: Boolean,
    public actual val cacheSettings: LocalCacheSettings?,
    public actual val grpcFlowControlWindow: Int,
) {
    /** The queue or executor the platform SDK calls listeners on, when the dev.gitlive layer configures one; platform-specific. */
    internal var callbackContext: Any? = null

    override fun equals(other: Any?): Boolean = other is FirebaseFirestoreSettings &&
        other.host == host && other.isSslEnabled == isSslEnabled && other.cacheSettings == cacheSettings && other.grpcFlowControlWindow == grpcFlowControlWindow

    override fun hashCode(): Int = listOf(host, isSslEnabled, cacheSettings, grpcFlowControlWindow).hashCode()

    override fun toString(): String = "FirebaseFirestoreSettings{host=$host, sslEnabled=$isSslEnabled, cacheSettings=$cacheSettings, grpcFlowControlWindow=$grpcFlowControlWindow}"

    public actual companion object {
        public actual val CACHE_SIZE_UNLIMITED: Long = dev.gitlive.firebase.firestore.internal.CACHE_SIZE_UNLIMITED
        public actual val DEFAULT_GRPC_FLOW_CONTROL_WINDOW: Int = dev.gitlive.firebase.firestore.internal.DEFAULT_GRPC_FLOW_CONTROL_WINDOW
    }

    public actual class Builder {
        public actual var host: String
        public actual var isSslEnabled: Boolean
        public actual var grpcFlowControlWindow: Int
        private var cacheSettings: LocalCacheSettings?

        public actual constructor() {
            host = DEFAULT_HOST
            isSslEnabled = true
            grpcFlowControlWindow = DEFAULT_GRPC_FLOW_CONTROL_WINDOW
            cacheSettings = null
        }

        public actual constructor(settings: FirebaseFirestoreSettings) {
            host = settings.host
            isSslEnabled = settings.isSslEnabled
            grpcFlowControlWindow = settings.grpcFlowControlWindow
            cacheSettings = settings.cacheSettings
        }

        public actual fun setHost(host: String): Builder = apply { this.host = host }

        public actual fun setSslEnabled(value: Boolean): Builder = apply { isSslEnabled = value }

        public actual fun setLocalCacheSettings(cacheSettings: LocalCacheSettings): Builder = apply { this.cacheSettings = cacheSettings }

        public actual fun setGrpcFlowControlWindow(grpcFlowControlWindow: Int): Builder = apply { this.grpcFlowControlWindow = grpcFlowControlWindow }

        public actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(host, isSslEnabled, cacheSettings, grpcFlowControlWindow)
    }
}

/** The size of a persistent or LRU cache, or the SDK's default when the settings have none. */
internal val LocalCacheSettings.sizeBytesOrDefault: Long
    get() = when (this) {
        is PersistentCacheSettings -> sizeBytes
        is MemoryCacheSettings -> (garbageCollectorSettings as? MemoryLruGcSettings)?.sizeBytes ?: CACHE_SIZE_UNLIMITED
        else -> CACHE_SIZE_UNLIMITED
    }

private fun requireCacheSize(sizeBytes: Long) {
    require(sizeBytes == CACHE_SIZE_UNLIMITED || sizeBytes >= MINIMUM_CACHE_BYTES) { "Cache size must be set to at least $MINIMUM_CACHE_BYTES bytes" }
}

public actual class PersistentCacheSettings private constructor(public actual val sizeBytes: Long) : LocalCacheSettings {
    override fun equals(other: Any?): Boolean = other is PersistentCacheSettings && other.sizeBytes == sizeBytes

    override fun hashCode(): Int = sizeBytes.hashCode()

    override fun toString(): String = "PersistentCacheSettings{sizeBytes=$sizeBytes}"

    public actual class Builder internal constructor() {
        private var sizeBytes = DEFAULT_CACHE_SIZE_BYTES

        public actual fun setSizeBytes(sizeBytes: Long): Builder = apply {
            requireCacheSize(sizeBytes)
            this.sizeBytes = sizeBytes
        }

        public actual fun build(): PersistentCacheSettings = PersistentCacheSettings(sizeBytes)
    }

    public actual companion object {
        public actual fun newBuilder(): Builder = Builder()
    }
}

public actual class MemoryCacheSettings private constructor(public actual val garbageCollectorSettings: MemoryGarbageCollectorSettings) : LocalCacheSettings {
    override fun equals(other: Any?): Boolean = other is MemoryCacheSettings && other.garbageCollectorSettings == garbageCollectorSettings

    override fun hashCode(): Int = garbageCollectorSettings.hashCode()

    override fun toString(): String = "MemoryCacheSettings{gcSettings=$garbageCollectorSettings}"

    public actual class Builder internal constructor() {
        private var gcSettings: MemoryGarbageCollectorSettings = MemoryEagerGcSettings.newBuilder().build()

        public actual fun setGcSettings(gcSettings: MemoryGarbageCollectorSettings): Builder = apply { this.gcSettings = gcSettings }

        public actual fun build(): MemoryCacheSettings = MemoryCacheSettings(gcSettings)
    }

    public actual companion object {
        public actual fun newBuilder(): Builder = Builder()
    }
}

public actual class MemoryEagerGcSettings private constructor() : MemoryGarbageCollectorSettings {
    override fun equals(other: Any?): Boolean = other is MemoryEagerGcSettings

    override fun hashCode(): Int = 0

    override fun toString(): String = "MemoryEagerGcSettings{}"

    public actual class Builder internal constructor() {
        public actual fun build(): MemoryEagerGcSettings = MemoryEagerGcSettings()
    }

    public actual companion object {
        public actual fun newBuilder(): Builder = Builder()
    }
}

public actual class MemoryLruGcSettings private constructor(public actual val sizeBytes: Long) : MemoryGarbageCollectorSettings {
    override fun equals(other: Any?): Boolean = other is MemoryLruGcSettings && other.sizeBytes == sizeBytes

    override fun hashCode(): Int = sizeBytes.hashCode()

    override fun toString(): String = "MemoryLruGcSettings{sizeBytes=$sizeBytes}"

    public actual class Builder internal constructor() {
        private var sizeBytes = DEFAULT_CACHE_SIZE_BYTES

        public actual fun setSizeBytes(sizeBytes: Long): Builder = apply {
            requireCacheSize(sizeBytes)
            this.sizeBytes = sizeBytes
        }

        public actual fun build(): MemoryLruGcSettings = MemoryLruGcSettings(sizeBytes)
    }

    public actual companion object {
        public actual fun newBuilder(): Builder = Builder()
    }
}
