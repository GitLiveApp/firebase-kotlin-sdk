/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import kotlin.jvm.JvmStatic

/** A path to a field of a document, mirroring `com.google.firebase.firestore.FieldPath` from the Firebase Android SDK. */
public expect class FieldPath {
    public companion object {
        /** The path of the field named by [fieldNames], which may contain any character. */
        public fun of(vararg fieldNames: String): FieldPath

        /** The sentinel path of the document id, for ordering and filtering by id. */
        public fun documentId(): FieldPath

        /** The path of [path], `.`-separated. */
        public fun fromDotSeparatedPath(path: String): FieldPath
    }
}

/**
 * A sentinel written in place of a field value, mirroring `com.google.firebase.firestore.FieldValue` from the Firebase
 * Android SDK: a server timestamp, a deletion or a transform of the current value.
 *
 * Not mirrored: `vector`, `minimum` and `maximum` (see `api/android-sdk/exclusions.txt`).
 */
public expect abstract class FieldValue {
    public companion object {
        /** The time the write reaches the backend. */
        public fun serverTimestamp(): FieldValue

        /** Deletes the field (in an update or merge). */
        public fun delete(): FieldValue

        /** Adds [elements] to the array field, without duplicates. */
        public fun arrayUnion(vararg elements: Any): FieldValue

        /** Removes [elements] from the array field. */
        public fun arrayRemove(vararg elements: Any): FieldValue

        /** Adds [l] to the numeric field. */
        public fun increment(l: Long): FieldValue

        /** Adds [d] to the numeric field. */
        public fun increment(d: Double): FieldValue
    }
}

/** A geographical point, mirroring `com.google.firebase.firestore.GeoPoint` from the Firebase Android SDK. */
public class GeoPoint(public val latitude: Double, public val longitude: Double) : Comparable<GeoPoint> {
    init {
        require(!latitude.isNaN() && latitude in -90.0..90.0) { "Latitude must be in the range of [-90, 90]" }
        require(!longitude.isNaN() && longitude in -180.0..180.0) { "Longitude must be in the range of [-180, 180]" }
    }

    override fun compareTo(other: GeoPoint): Int = compareValuesBy(this, other, { it.latitude }, { it.longitude })

    override fun equals(other: Any?): Boolean = other is GeoPoint && other.latitude == latitude && other.longitude == longitude

    override fun hashCode(): Int = 31 * latitude.hashCode() + longitude.hashCode()

    override fun toString(): String = "GeoPoint { latitude=$latitude, longitude=$longitude }"
}

/** Binary data stored in a document, mirroring `com.google.firebase.firestore.Blob` from the Firebase Android SDK. */
public class Blob private constructor(private val bytes: ByteArray) : Comparable<Blob> {
    /** A copy of the bytes. */
    public fun toBytes(): ByteArray = bytes.copyOf()

    override fun compareTo(other: Blob): Int {
        val size = minOf(bytes.size, other.bytes.size)
        for (i in 0 until size) {
            val comparison = (bytes[i].toInt() and 0xff).compareTo(other.bytes[i].toInt() and 0xff)
            if (comparison != 0) return comparison
        }
        return bytes.size.compareTo(other.bytes.size)
    }

    override fun equals(other: Any?): Boolean = other is Blob && other.bytes.contentEquals(bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = "Blob { bytes=${bytes.joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }} }"

    public companion object {
        /** A [Blob] of a copy of [bytes]. */
        @JvmStatic
        public fun fromBytes(bytes: ByteArray): Blob = Blob(bytes.copyOf())
    }
}

/** How a `set` merges with an existing document, mirroring `com.google.firebase.firestore.SetOptions` from the Firebase Android SDK. */
public expect class SetOptions {
    public companion object {
        /** Merges every field of the data into the document. */
        public fun merge(): SetOptions

        /** Merges only [fields] (`.`-separated paths) into the document. */
        public fun mergeFields(vararg fields: String): SetOptions

        /** Merges only [fields] (`.`-separated paths) into the document. */
        public fun mergeFields(fields: List<String>): SetOptions

        /** Merges only [fields] into the document. */
        public fun mergeFieldPaths(fields: List<FieldPath>): SetOptions
    }
}
