/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.teamhub.firebase

import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlin.collections.set

actual fun FirebaseEncoder.structureEncoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder =
    when(desc.kind as StructureKind) {
        StructureKind.LIST -> mutableListOf<Any?>()
            .also { value = it }
            .let { FirebaseCompositeEncoder(positiveInfinity) { _, index, value -> it.add(index, value) } }
        StructureKind.MAP -> mutableListOf<Any?>()
            .let { FirebaseCompositeEncoder(positiveInfinity, { value = it.chunked(2).associate { (k, v) -> k to v } }) { _, _, value -> it.add(value) } }
        StructureKind.CLASS,  StructureKind.OBJECT -> mutableMapOf<Any?, Any?>()
            .also { value = it }
            .let { FirebaseCompositeEncoder(positiveInfinity) { _, index, value -> it[desc.getElementName(index)] = value } }
    }