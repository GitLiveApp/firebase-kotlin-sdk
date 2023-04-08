/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlin.collections.set

actual fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor): FirebaseCompositeEncoder = when(descriptor.kind) {
    StructureKind.LIST -> mutableListOf<Any?>()
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault) { _, index, value -> it.add(index, value) } }
    StructureKind.MAP -> mutableListOf<Any?>()
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, { value = it.chunked(2).associate { (k, v) -> k to v } }) { _, _, value -> it.add(value) } }
    StructureKind.CLASS,  StructureKind.OBJECT -> mutableMapOf<Any?, Any?>()
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault,
            setPolymorphicType = { discriminator, type ->
                it[discriminator] = type
            },
            set = { _, index, value -> it[descriptor.getElementName(index)] = value }
        ) }
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}