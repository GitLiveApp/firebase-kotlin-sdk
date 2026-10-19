/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp

/**
 * The data of a document read from Firestore, mirroring `com.google.firebase.firestore.DocumentSnapshot` from the
 * Firebase Android SDK. Field values are the Firestore types: `Boolean`, `Long`, `Double`, `String`, [Timestamp],
 * [GeoPoint], [Blob], [DocumentReference], `List` and `Map`, as on Android; a document that does not exist has no data.
 *
 * Not mirrored: the reflective mapping to custom classes (`toObject`, `get(field, Class)`) and the `Date` accessors
 * (see `api/android-sdk/exclusions.txt`); use the reified `toObject` and `getField` of the Kotlin extensions and
 * [getTimestamp] instead.
 */
public expect open class DocumentSnapshot {
    /** The document id. */
    public val id: String

    /** The reference of the document. */
    public val reference: DocumentReference

    /** Whether the snapshot is from the cache or has pending writes. */
    public val metadata: SnapshotMetadata

    /** The fields as a map, or null when the document does not exist. */
    public val data: Map<String, Any?>?

    /** Whether the document exists. */
    public fun exists(): Boolean

    /** The fields as a map with server timestamps resolved per [serverTimestampBehavior], or null when the document does not exist. */
    public fun getData(serverTimestampBehavior: ServerTimestampBehavior): Map<String, Any?>?

    /** Whether the document has [field] (a `.`-separated path). */
    public fun contains(field: String): Boolean

    /** Whether the document has [fieldPath]. */
    public fun contains(fieldPath: FieldPath): Boolean

    /** The value of [field] (a `.`-separated path), or null. */
    public fun get(field: String): Any?

    /** The value of [field] with server timestamps resolved per [serverTimestampBehavior]. */
    public fun get(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any?

    /** The value of [fieldPath], or null. */
    public fun get(fieldPath: FieldPath): Any?

    /** The value of [fieldPath] with server timestamps resolved per [serverTimestampBehavior]. */
    public fun get(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any?

    public fun getBoolean(field: String): Boolean?
    public fun getDouble(field: String): Double?
    public fun getLong(field: String): Long?
    public fun getString(field: String): String?
    public fun getTimestamp(field: String): Timestamp?
    public fun getTimestamp(field: String, serverTimestampBehavior: ServerTimestampBehavior): Timestamp?
    public fun getBlob(field: String): Blob?
    public fun getGeoPoint(field: String): GeoPoint?
    public fun getDocumentReference(field: String): DocumentReference?

    /** How a server timestamp that has not been written to the backend yet is read. */
    public enum class ServerTimestampBehavior {
        NONE,
        ESTIMATE,
        PREVIOUS,
    }
}

/** A [DocumentSnapshot] from a query, which is guaranteed to exist. */
public expect class QueryDocumentSnapshot : DocumentSnapshot

/** The results of a [Query], mirroring the Android SDK's `QuerySnapshot`: the documents and the changes since the previous snapshot. */
public expect class QuerySnapshot : Iterable<QueryDocumentSnapshot> {
    /** The query the results belong to. */
    public val query: Query

    /** Whether the snapshot is from the cache or has pending writes. */
    public val metadata: SnapshotMetadata

    /** The documents. */
    public val documents: List<DocumentSnapshot>

    /** The changes since the previous snapshot, or every document as added for the first snapshot. */
    public val documentChanges: List<DocumentChange>

    /** The changes since the previous snapshot, including metadata-only changes when [metadataChanges] is `INCLUDE`. */
    public fun getDocumentChanges(metadataChanges: MetadataChanges): List<DocumentChange>

    /** Whether there are no documents. */
    public val isEmpty: Boolean

    /** The number of documents. */
    public fun size(): Int

    override fun iterator(): Iterator<QueryDocumentSnapshot>
}

/** A change to the results of a query, mirroring the Android SDK's `DocumentChange`. */
public expect class DocumentChange {
    /** The document after the change. */
    public val document: QueryDocumentSnapshot

    /** Whether the document was added, modified or removed. */
    public val type: Type

    /** The index of the document in the previous results, or -1 if it was added. */
    public val oldIndex: Int

    /** The index of the document in the new results, or -1 if it was removed. */
    public val newIndex: Int

    public enum class Type {
        ADDED,
        MODIFIED,
        REMOVED,
    }
}

/** The metadata of a snapshot, mirroring the Android SDK's `SnapshotMetadata`. */
public expect class SnapshotMetadata {
    /** Whether the snapshot contains local writes not yet acknowledged by the backend. */
    public fun hasPendingWrites(): Boolean

    /** Whether the snapshot was served from the cache. */
    public val isFromCache: Boolean
}
