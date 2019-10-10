package dev.teamhub.firebase.common

import kotlin.js.Json
import kotlin.js.json
import kotlin.reflect.KClass

fun toJson(data: Any?): Any? = when(data) {
    null -> null
    is firebase.firestore.FieldValue -> data
    is Boolean -> data
    is Double -> data
    is String -> data
    is List<*> -> data.map { toJson(it) }.toTypedArray()
    is Map<*, *> -> json(*data.entries.map { (k, v) -> k as String to toJson(v) }.toTypedArray())
    else -> (js("Object").entries(data) as Array<Array<Any>>)
            .filter { (key) -> !key.toString().startsWith("__exclude__") }
            .map { (key, value) ->
                key as String
                val unmangled = key.substringBefore("_")
                if(data.asDynamic().__proto__.hasOwnProperty(unmangled).unsafeCast<Boolean>()) {
                    unmangled to toJson(value)
                } else {
                    key to toJson(value)
                }
            }
            .let { json(*it.toTypedArray()) }
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun fromJson(data: Any?, valueType: KClass<*>? = null): Any? = when(data) {
    undefined -> undefined
    is Boolean -> data
    is Double -> data
    is String -> data
    is Array<*> -> data.map { fromJson(it) }
    else -> {
        if(valueType == null || valueType == Map::class) (js("Object").entries(data) as Array<Array<Any>>)
                .associate { (key, value) -> key to fromJson(value) }
                .let { return@fromJson it }

        val instance = js("Reflect").construct(valueType.js, emptyArray<Any>())

        val mangled = js("Object")
                .keys(instance)
                .unsafeCast<Array<String>>()
                .associate { it.substringBefore("_") to it }

        (js("Object").entries(data) as Array<Array<Any>>)
                .forEach { (key, value) ->
                    val descriptor = js("Object").getOwnPropertyDescriptor(instance.__proto__, key)
                    if(descriptor == null) {
                        instance[key as String] = fromJson(value)
                    } else {
                        instance[mangled[key as String]] = fromJson(value)
                    }
                }

        instance.unsafeCast<Json>()
   }
}


