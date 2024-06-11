package dev.gitlive.firebase.internal

import kotlin.js.Json
import kotlin.js.json

val EncodedObject.js: Json get() = json(*getRaw().entries.map { (key, value) -> key to value }.toTypedArray())

@PublishedApi
internal actual fun Any.asNativeMap(): Map<*, *>? {
    val json = when (this) {
        is Number -> null
        is Boolean -> null
        is String -> null
        is Map<*, *> -> {
            if (keys.all { it is String }) {
                json(*mapKeys { (key, _) -> key as String }.toList().toTypedArray())
            } else {
                null
            }
        }
        is Collection<*> -> null
        is Array<*> -> null
        else -> {
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            this as Json
        }
    } ?: return null
    val mutableMap = mutableMapOf<String, Any?>()
    for (key in js("Object").keys(json)) {
        mutableMap[key as String] = json[key as String]
    }
    return mutableMap.toMap()
}
