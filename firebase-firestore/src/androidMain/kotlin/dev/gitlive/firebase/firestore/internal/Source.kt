package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.NativeSource
import dev.gitlive.firebase.firestore.Source

internal fun Source.toAndroidSource() = when (this) {
    Source.CACHE -> NativeSource.CACHE
    Source.SERVER -> NativeSource.SERVER
    Source.DEFAULT -> NativeSource.DEFAULT
}
