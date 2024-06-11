@file:JvmName("AndroidEncodedObject")

package dev.gitlive.firebase.internal

val EncodedObject.android: Map<String, Any?> get() = getRaw()

@PublishedApi
internal actual fun Any.asNativeMap(): Map<*, *>? = this as? Map<*, *>
