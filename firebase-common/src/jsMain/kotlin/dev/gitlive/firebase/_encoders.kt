/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlin.js.Json
import kotlin.js.json

actual data class EncodedObject(private val keyValues: List<Pair<String, Any?>>) : Json by json(*keyValues.toTypedArray()) {
    actual companion object {
        actual val emptyEncodedObject: EncodedObject = EncodedObject(emptyList())
    }

    actual val raw get() = keyValues.toMap()
}


@PublishedApi
internal actual fun List<Pair<String, Any?>>.asEncodedObject() = EncodedObject(this)

@PublishedApi
internal actual fun Any.asNativeMap(): Map<*, *>? = (this as? Json)?.let { json ->
    val mutableMap = mutableMapOf<String, Any?>()
    for (key in js("Object").keys(json)) {
        mutableMap[key] = json[key]
    }
    mutableMap.toMap()
}

actual fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor): FirebaseCompositeEncoder = when(descriptor.kind) {
    StructureKind.LIST -> encodeAsList(descriptor)
    StructureKind.MAP -> {
        val map = json()
        var lastKey = ""
        value = map
        FirebaseCompositeEncoder(settings) { _, index, value ->
            if (index % 2 == 0) {
                lastKey = (value as? String) ?: JSON.stringify(value)
            } else {
                map[lastKey] = value
            }
        }
    }
    StructureKind.CLASS,  StructureKind.OBJECT -> encodeAsMap(descriptor)
    is PolymorphicKind -> encodeAsMap(descriptor)
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

private fun FirebaseEncoder.encodeAsList(descriptor: SerialDescriptor): FirebaseCompositeEncoder = Array<Any?>(descriptor.elementsCount) { null }
    .also { value = it }
    .let { FirebaseCompositeEncoder(settings) { _, index, value -> it[index] = value } }
private fun FirebaseEncoder.encodeAsMap(descriptor: SerialDescriptor): FirebaseCompositeEncoder = json()
    .also { value = it }
    .let {
        FirebaseCompositeEncoder(
            settings,
            setPolymorphicType = { discriminator, type ->
                it[discriminator] = type
            },
            set = { _, index, value -> it[descriptor.getElementName(index)] = value }
        )
    }
