/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind

actual fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder = when(descriptor.kind as StructureKind) {
        StructureKind.CLASS, StructureKind.OBJECT -> (value as Map<*, *>).let { map ->
            FirebaseClassDecoder(map.size, { map.containsKey(it) }) { desc, index -> map[desc.getElementName(index)] }
        }
        StructureKind.LIST -> (value as List<*>).let {
            FirebaseCompositeDecoder(it.size) { _, index -> it[index] }
        }
        StructureKind.MAP -> (value as Map<*, *>).entries.toList().let {
            FirebaseCompositeDecoder(it.size) { _, index -> it[index/2].run { if(index % 2 == 0) key else value }  }
        }
    }