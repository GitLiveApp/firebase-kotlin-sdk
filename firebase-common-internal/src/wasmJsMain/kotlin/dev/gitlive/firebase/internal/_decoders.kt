/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import dev.gitlive.firebase.externals.asJsAny
import dev.gitlive.firebase.externals.asJsArray
import dev.gitlive.firebase.externals.jsContainsValue
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsObjectEntries
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.jsonParse
import dev.gitlive.firebase.externals.toKotlin
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder

internal actual fun FirebaseDecoderImpl.structureDecoder(descriptor: SerialDescriptor, polymorphicIsNested: Boolean): CompositeDecoder = when (descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> decodeAsMap(false)
    StructureKind.LIST -> decodeAsList()
    StructureKind.MAP -> {
        val entries = jsObjectEntries(value.asJsAny())
        FirebaseCompositeDecoder(
            entries.length,
            settings,
        ) { desc, index ->
            val entry = entries[index / 2]!!
            if (index % 2 == 0) {
                val key = entry[0].toKotlin() as String
                if (desc.getElementDescriptor(index).kind == PrimitiveKind.STRING) {
                    key
                } else {
                    jsonParse(key).toKotlin()
                }
            } else {
                entry[1].toKotlin()
            }
        }
    }

    is PolymorphicKind -> decodeAsMap(polymorphicIsNested)
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

internal actual fun getPolymorphicType(value: Any?, discriminator: String): String = jsGet(value.asJsAny(), discriminator).toKotlin() as String

private fun FirebaseDecoderImpl.decodeAsList(): CompositeDecoder {
    val array = value.asJsArray()
    return FirebaseCompositeDecoder(array.length, settings) { _, index -> array[index].toKotlin() }
}

private fun FirebaseDecoderImpl.decodeAsMap(isNestedPolymorphic: Boolean): CompositeDecoder {
    val obj = value.asJsAny()
    return FirebaseClassDecoder(jsObjectKeys(obj).length, settings, { jsContainsValue(obj, it) }) { desc, index ->
        if (isNestedPolymorphic) {
            if (desc.getElementName(index) == "value") {
                obj
            } else {
                jsGet(obj, desc.getElementName(index)).toKotlin()
            }
        } else {
            jsGet(obj, desc.getElementName(index)).toKotlin()
        }
    }
}
