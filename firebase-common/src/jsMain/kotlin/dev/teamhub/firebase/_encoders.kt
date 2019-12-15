package dev.teamhub.firebase

import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlin.js.json

actual fun FirebaseEncoder.structureEncoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder = when(desc.kind as StructureKind) {
    StructureKind.LIST -> mutableListOf<Any?>()
        .let { FirebaseCompositeEncoder({ value = it.toTypedArray() }) { _, index, value -> it.add(index, value) } }
    StructureKind.MAP, StructureKind.CLASS -> json()
        .also { value = it }
        .let { FirebaseCompositeEncoder { _, index, value -> it[desc.getElementName(index)] = value } }
}

