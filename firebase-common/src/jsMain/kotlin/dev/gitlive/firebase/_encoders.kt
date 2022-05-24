/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlin.js.json

actual fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor): FirebaseCompositeEncoder = when(descriptor.kind) {
    StructureKind.LIST -> Array<Any?>(descriptor.elementsCount) { null }
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault) { _, index, value -> it[index] = value } }
    StructureKind.MAP -> {
        val map = json()
        var lastKey: String = ""
        value = map
        FirebaseCompositeEncoder(shouldEncodeElementDefault) { _, index, value -> if(index % 2 == 0) lastKey = value as String else map[lastKey] = value }
    }
    StructureKind.CLASS,  StructureKind.OBJECT, PolymorphicKind.SEALED -> json()
        .also { value = it }
        .let { FirebaseCompositeEncoder(
            shouldEncodeElementDefault,
            setPolymorphicType = { discriminator, type ->
                it[discriminator] = type
            },
            set = { _, index, value -> it[descriptor.getElementName(index)] = value }
        ) }
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

