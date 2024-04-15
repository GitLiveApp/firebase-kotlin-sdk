package dev.gitlive.firebase.internal

val EncodedObject.ios: Map<Any?, *> get() = getRaw().mapKeys { (key, _) -> key }

@PublishedApi
internal actual fun Any.asNativeMap(): Map<*, *>? = this as? Map<*, *>
