/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder

actual fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor): CompositeDecoder = when(descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> decodeAsMap()
    StructureKind.LIST -> decodeAsList()
    StructureKind.MAP -> (value as Map<*, *>).entries.toList().let {
        FirebaseCompositeDecoder(it.size, settings) { _, index -> it[index/2].run { if(index % 2 == 0) key else value }  }
    }
    is PolymorphicKind -> when (settings.polymorphicStructure) {
        EncodeDecodeSettings.PolymorphicStructure.MAP -> decodeAsMap()
        EncodeDecodeSettings.PolymorphicStructure.LIST -> decodeAsList()
    }
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

actual fun getPolymorphicType(value: Any?, discriminator: String): String =
    (value as Map<*,*>)[discriminator] as String

private fun FirebaseDecoder.decodeAsList(): CompositeDecoder = (value as List<*>).let {
    FirebaseCompositeDecoder(it.size, settings) { _, index -> it[index] }
}
private fun FirebaseDecoder.decodeAsMap(): CompositeDecoder = (value as Map<*, *>).let { map ->
    FirebaseClassDecoder(map.size, settings, { map.containsKey(it) }) { desc, index -> map[desc.getElementName(index)] }
}
