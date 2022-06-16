/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind

actual fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor, decodeDouble: (value: Any?) -> Double?): CompositeDecoder = when(descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT, PolymorphicKind.SEALED -> (value as Map<*, *>).let { map ->
        FirebaseClassDecoder(decodeDouble, map.size, { map.containsKey(it) }) { desc, index -> map[desc.getElementName(index)] }
    }
    StructureKind.LIST -> (value as List<*>).let {
        FirebaseCompositeDecoder(decodeDouble, it.size) { _, index -> it[index] }
    }
    StructureKind.MAP -> (value as Map<*, *>).entries.toList().let {
        FirebaseCompositeDecoder(decodeDouble, it.size) { _, index -> it[index/2].run { if(index % 2 == 0) key else value }  }
    }
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

actual fun getPolymorphicType(value: Any?, discriminator: String): String =
    (value as Map<*,*>)[discriminator] as String