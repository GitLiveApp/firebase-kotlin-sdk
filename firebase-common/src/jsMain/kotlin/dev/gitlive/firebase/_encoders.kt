/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlin.js.json

actual fun FirebaseEncoder.structureEncoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder = when(desc.kind as StructureKind) {
    StructureKind.LIST -> Array<Any?>(desc.elementsCount) { null }
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> it[index] = value } }
    StructureKind.MAP -> {
        val map = json()
        var lastKey: String = ""
        value = map
        FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> if(index % 2 == 0) lastKey = value as String else map[lastKey] = value }
    }
    StructureKind.CLASS,  StructureKind.OBJECT -> json()
        .also { value = it }
        .let { FirebaseCompositeEncoder(shouldEncodeElementDefault, positiveInfinity) { _, index, value -> it[desc.getElementName(index)] = value } }
}

