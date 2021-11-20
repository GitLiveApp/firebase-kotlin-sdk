/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlin.collections.set

actual fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind as StructureKind) {
    StructureKind.LIST -> mutableListOf<Any?>()
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, {getEncoder(shouldEncodeElementDefault)}) { _, index, value -> it.add(index, value) } }
    StructureKind.MAP -> mutableListOf<Any?>()
        .let {
            FirebaseCompositeEncoder(
                shouldEncodeElementDefault,
                {getEncoder(shouldEncodeElementDefault)},
                { value = it.chunked(2).associate { (k, v) -> k to v } }) { _, _, value -> it.add(value) }
        }
    StructureKind.OBJECT, StructureKind.CLASS -> mutableMapOf<Any?, Any?>()
        .also { value = it }
        .let {
            FirebaseCompositeEncoder(shouldEncodeElementDefault, {getEncoder(shouldEncodeElementDefault)}) { _, index, value ->
                it[descriptor.getElementName(
                    index
                )] = value
            }
        }

}