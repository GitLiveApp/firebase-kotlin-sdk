package dev.gitlive.firebase.firestore

sealed interface LocalCacheSettings {

    data class Persistent internal constructor(val sizeBytes: Long) : LocalCacheSettings {

        companion object {
            fun newBuilder(): Builder = Builder()
        }

        class Builder internal constructor() {
            var sizeBytes: Long = FirebaseFirestoreSettings.DEFAULT_CACHE_SIZE_BYTES
            fun build(): Persistent = Persistent(sizeBytes)
        }
    }
    data class Memory internal constructor(val garbaseCollectorSettings: MemoryGarbageCollectorSettings) : LocalCacheSettings {

        companion object {
            fun newBuilder(): Builder = Builder()
        }

        class Builder internal constructor() {

            var gcSettings: MemoryGarbageCollectorSettings = MemoryGarbageCollectorSettings.Eager.newBuilder().build()

            fun build(): Memory = Memory(gcSettings)
        }
    }
}

typealias PersistentCacheSettings = LocalCacheSettings.Persistent
typealias MemoryCacheSettings = LocalCacheSettings.Memory

sealed interface MemoryGarbageCollectorSettings {
    data object Eager : MemoryGarbageCollectorSettings {

        fun newBuilder(): Builder = Builder()

        class Builder internal constructor() {
            fun build(): Eager = Eager
        }
    }
    data class LRUGC internal constructor(val sizeBytes: Long) : MemoryGarbageCollectorSettings {

        companion object {
            fun newBuilder(): Builder = Builder()
        }

        class Builder internal constructor() {
            var sizeBytes: Long = FirebaseFirestoreSettings.DEFAULT_CACHE_SIZE_BYTES
            fun build(): LRUGC = LRUGC(sizeBytes)
        }
    }
}

typealias MemoryEagerGcSettings = MemoryGarbageCollectorSettings.Eager
typealias MemoryLruGcSettings = MemoryGarbageCollectorSettings.LRUGC

fun memoryCacheSettings(builder: LocalCacheSettings.Memory.Builder.() -> Unit): LocalCacheSettings.Memory =
    LocalCacheSettings.Memory.newBuilder().apply(builder).build()

fun memoryEagerGcSettings(builder: MemoryGarbageCollectorSettings.Eager.Builder.() -> Unit) =
    MemoryGarbageCollectorSettings.Eager.newBuilder().apply(builder).build()

fun memoryLruGcSettings(builder: MemoryGarbageCollectorSettings.LRUGC.Builder.() -> Unit) =
    MemoryGarbageCollectorSettings.LRUGC.newBuilder().apply(builder).build()

fun persistentCacheSettings(builder: LocalCacheSettings.Persistent.Builder.() -> Unit) =
    LocalCacheSettings.Persistent.newBuilder().apply(builder).build()
