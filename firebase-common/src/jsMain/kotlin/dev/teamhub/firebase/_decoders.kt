package dev.teamhub.firebase

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlin.js.Json

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
actual fun FirebaseDecoder.structureDecoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder = when(desc.kind as StructureKind) {
    StructureKind.CLASS -> (value as Json).let { json ->
        FirebaseClassDecoder(js("Object").keys(value).length as Int, { json[it] != undefined }) {
                desc, index -> json[desc.getElementName(index)]
        }
    }
    StructureKind.LIST -> (value as Array<*>).let {
        FirebaseCompositeDecoder(it.size) { _, index -> it[index] }
    }
    StructureKind.MAP, StructureKind.OBJECT -> (js("Object").entries(value) as Array<Array<Any>>).let {
        FirebaseCompositeDecoder(it.size) { _, index -> it[index/2].run { if(index % 2 == 0) get(0) else get(1) } }
    }
}