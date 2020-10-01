/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlin.collections.set

actual fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder = when(descriptor.kind as StructureKind) {
    StructureKind.LIST -> mutableListOf<Any?>()
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> it.add(index, value) } }
    StructureKind.MAP -> mutableListOf<Any?>()
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity, { value = it.chunked(2).associate { (k, v) -> k to v } }) { _, _, value -> it.add(value) } }
    StructureKind.CLASS -> mutableMapOf<Any?, Any?>()
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> it[descriptor.getElementName(index)] = value } }
    StructureKind.OBJECT -> FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, _, obj -> value = obj }
}
