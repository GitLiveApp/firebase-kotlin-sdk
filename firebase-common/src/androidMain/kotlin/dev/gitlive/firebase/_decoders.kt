/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind

actual fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor): CompositeDecoder = when(descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> when {
        value is Map<*, *> ->
            FirebaseClassDecoder(value.size, { value.containsKey(it) }) { desc, index ->
                value[desc.getElementName(index)]
            }
        value != null && value::class.qualifiedName == "com.google.firebase.Timestamp" -> {
            makeTimestampJavaReflectionDecoder(value)
        }
        value != null && value::class.qualifiedName == "com.google.firebase.firestore.DocumentReference" -> {
            makeDocumentReferenceJavaReflectionDecoder(value)
        }
        else -> FirebaseEmptyCompositeDecoder()
    }
    StructureKind.LIST, is PolymorphicKind -> (value as List<*>).let {
        FirebaseCompositeDecoder(it.size) { _, index -> it[index] }
    }
    StructureKind.MAP -> (value as Map<*, *>).entries.toList().let {
        FirebaseCompositeDecoder(it.size) { _, index -> it[index / 2].run { if (index % 2 == 0) key else value } }
    }
    else -> TODO("Not implemented ${descriptor.kind}")
}

private val timestampKeys = setOf("seconds", "nanoseconds")

private fun makeTimestampJavaReflectionDecoder(jvmObj: Any): CompositeDecoder {
    val timestampClass = Class.forName("com.google.firebase.Timestamp")
    val getSeconds = timestampClass.getMethod("getSeconds")
    val getNanoseconds = timestampClass.getMethod("getNanoseconds")

    return FirebaseClassDecoder(
        size = 2,
        containsKey = { timestampKeys.contains(it) }
    ) { descriptor, index ->
        when (descriptor.getElementName(index)) {
            "seconds" -> getSeconds.invoke(jvmObj) as Long
            "nanoseconds" -> getNanoseconds.invoke(jvmObj) as Int
            else -> null
        }
    }
}

private val geoPointKeys = setOf("latitude", "longitude")

private fun makeGeoPointJavaReflectionDecoder(jvmObj: Any): CompositeDecoder {
    val timestampClass = Class.forName("com.google.firebase.firestore.GeoPoint")
    val getLatitude = timestampClass.getMethod("getLatitude")
    val getLongitude = timestampClass.getMethod("getLongitude")

    return FirebaseClassDecoder(
        size = 2,
        containsKey = { geoPointKeys.contains(it) }
    ) { descriptor, index ->
        when (descriptor.getElementName(index)) {
            "latitude" -> getLatitude.invoke(jvmObj) as Double
            "longitude" -> getLongitude.invoke(jvmObj) as Double
            else -> null
        }
    }
}

private val documentKeys = setOf("path")

private fun makeDocumentReferenceJavaReflectionDecoder(jvmObj: Any): CompositeDecoder {
    val timestampClass = Class.forName("com.google.firebase.firestore.DocumentReference")
    val getPath = timestampClass.getMethod("getPath")

    return FirebaseClassDecoder(
        size = 1,
        containsKey = { documentKeys.contains(it) }
    ) { descriptor, index ->
        when (descriptor.getElementName(index)) {
            "path" -> getPath.invoke(jvmObj) as String
            else -> null
        }
    }
}
