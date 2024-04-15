package dev.gitlive.firebase.firestore

sealed class LocalCacheSettings {

    internal companion object {
        // Firestore cache defaults to 100MB
        const val DEFAULT_CACHE_SIZE = 100L*1024L*1024L
    }

    data class Persistent(val sizeBytes: Long) : LocalCacheSettings() {

        companion object {
            fun newBuilder(): Builder = BuilderImpl()
        }

        interface Builder {
            var sizeBytes: Long
            fun build(): Persistent
        }

        private class BuilderImpl(
            override var sizeBytes: Long = DEFAULT_CACHE_SIZE
        ) : Builder {
            override fun build(): Persistent = Persistent(sizeBytes)
        }
    }
    data class Memory(val garbaseCollectorSettings: GarbageCollectorSettings) : LocalCacheSettings() {

        companion object {
            fun newBuilder(): Builder = BuilderImpl()
        }

        interface Builder {

            var gcSettings: GarbageCollectorSettings

            fun build(): Memory
        }

        private class BuilderImpl(
            override var gcSettings: GarbageCollectorSettings = GarbageCollectorSettings.Eager
        ) : Builder {
            override fun build(): Memory = Memory(gcSettings)
        }
    }
}

sealed class GarbageCollectorSettings {
    data object Eager : GarbageCollectorSettings() {

        fun newBuilder(): Builder = BuilderImpl()

        interface Builder {
            fun build(): Eager
        }

        private class BuilderImpl : Builder {
            override fun build(): Eager = Eager
        }
    }
    data class LRUGC(val sizeBytes: Long) : GarbageCollectorSettings() {

        companion object {
            fun newBuilder(): Builder = BuilderImpl()
        }

        interface Builder {
            var sizeBytes: Long
            fun build(): LRUGC
        }

        private class BuilderImpl(
            override var sizeBytes: Long = LocalCacheSettings.DEFAULT_CACHE_SIZE
        ) : Builder {
            override fun build(): LRUGC = LRUGC(sizeBytes)
        }
    }
}

fun memoryCacheSettings(builder: LocalCacheSettings.Memory.Builder.() -> Unit): LocalCacheSettings.Memory =
    LocalCacheSettings.Memory.newBuilder().apply(builder).build()

fun memoryEagerGcSettings(builder: GarbageCollectorSettings.Eager.Builder.() -> Unit) =
    GarbageCollectorSettings.Eager.newBuilder().apply(builder).build()

fun memoryLruGcSettings(builder: GarbageCollectorSettings.LRUGC.Builder.() -> Unit) =
    GarbageCollectorSettings.LRUGC.newBuilder().apply(builder).build()

fun persistentCacheSettings(builder: LocalCacheSettings.Persistent.Builder.() -> Unit) =
    LocalCacheSettings.Persistent.newBuilder().apply(builder).build()