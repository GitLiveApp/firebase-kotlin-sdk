package dev.gitlive.firebase.internal

val EncodedObject.ios: Map<Any?, *> get() = raw.mapKeys { (key, _) -> key }

@PublishedApi
internal actual fun Any.asNativeMap(): Map<*, *>? = this as? Map<*, *>
