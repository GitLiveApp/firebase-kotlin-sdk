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
                this as Json
            } else {
                null
            }
        }
        is Collection<*> -> null
        is Array<*> -> null
        else -> {
            this as Json
        }
    } ?: return null
    val mutableMap = mutableMapOf<String, Any?>()
    for (key in js("Object").keys(json)) {
        mutableMap[key] = json[key]
    }
    return mutableMap.toMap()
}
