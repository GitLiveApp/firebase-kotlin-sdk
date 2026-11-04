/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.firebase.Timestamp
import dev.gitlive.firebase.firestore.externals.snapshotEqual
import kotlin.js.Json
import kotlin.js.json
import dev.gitlive.firebase.firestore.externals.DocumentChange as JsDocumentChange
import dev.gitlive.firebase.firestore.externals.DocumentSnapshot as JsDocumentSnapshot
import dev.gitlive.firebase.firestore.externals.QuerySnapshot as JsQuerySnapshot
import dev.gitlive.firebase.firestore.externals.SnapshotMetadata as JsSnapshotMetadata

private fun DocumentSnapshot.ServerTimestampBehavior.toJs(): Json = json("serverTimestamps" to name.lowercase())

/** @property js The underlying Firebase JS SDK object. */
public actual class SnapshotMetadata internal constructor(public val js: JsSnapshotMetadata) {
    public actual fun hasPendingWrites(): Boolean = js.hasPendingWrites
    public actual val isFromCache: Boolean get() = js.fromCache

    override fun equals(other: Any?): Boolean = other is SnapshotMetadata && other.hasPendingWrites() == hasPendingWrites() && other.isFromCache == isFromCache

    override fun hashCode(): Int = 31 * hasPendingWrites().hashCode() + isFromCache.hashCode()

    override fun toString(): String = "SnapshotMetadata{hasPendingWrites=${hasPendingWrites()}, isFromCache=$isFromCache}"
}

/** @property js The underlying Firebase JS SDK object. */
public actual class DocumentChange internal constructor(public val js: JsDocumentChange) {
    public actual val document: QueryDocumentSnapshot get() = QueryDocumentSnapshot(js.doc)
    public actual val type: Type get() = Type.valueOf(js.type.uppercase())
    public actual val oldIndex: Int get() = js.oldIndex
    public actual val newIndex: Int get() = js.newIndex

    public actual enum class Type {
        ADDED,
        MODIFIED,
        REMOVED,
    }

    override fun equals(other: Any?): Boolean = other is DocumentChange && (other.js === js || other.type == type && other.oldIndex == oldIndex && other.newIndex == newIndex && other.document == document)

    override fun hashCode(): Int = listOf(type, oldIndex, newIndex, document).hashCode()

    override fun toString(): String = "DocumentChange{type=$type, document=${js.doc.id}, oldIndex=$oldIndex, newIndex=$newIndex}"
}

/** @property js The underlying Firebase JS SDK object. */
public actual open class DocumentSnapshot internal constructor(public open val js: JsDocumentSnapshot) {
    public actual val id: String get() = js.id
    public actual val reference: DocumentReference get() = DocumentReference(js.ref)
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)

    @Suppress("UNCHECKED_CAST")
    public actual val data: Map<String, Any?>? get() = rethrow { js.data() }.toCompat() as Map<String, Any?>?

    public actual fun exists(): Boolean = rethrow { js.exists() }

    @Suppress("UNCHECKED_CAST")
    public actual fun getData(serverTimestampBehavior: ServerTimestampBehavior): Map<String, Any?>? = rethrow { js.data(serverTimestampBehavior.toJs()) }.toCompat() as Map<String, Any?>?

    public actual fun contains(field: String): Boolean = rethrow { jsTypeOf(js.get(field)) != "undefined" }

    public actual fun contains(fieldPath: FieldPath): Boolean = rethrow { jsTypeOf(js.get(fieldPath.toJs())) != "undefined" }

    public actual fun get(field: String): Any? = rethrow { js.get(field) }.toCompat()

    public actual fun get(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow { js.get(field, serverTimestampBehavior.toJs()) }.toCompat()

    public actual fun get(fieldPath: FieldPath): Any? = rethrow { js.get(fieldPath.toJs()) }.toCompat()

    public actual fun get(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow { js.get(fieldPath.toJs(), serverTimestampBehavior.toJs()) }.toCompat()

    public actual fun getBoolean(field: String): Boolean? = typed(field, get(field))
    public actual fun getDouble(field: String): Double? = typed<Number>(field, get(field))?.toDouble()
    public actual fun getLong(field: String): Long? = typed<Number>(field, get(field))?.toLong()
    public actual fun getString(field: String): String? = typed(field, get(field))
    public actual fun getTimestamp(field: String): Timestamp? = typed(field, get(field))
    public actual fun getTimestamp(field: String, serverTimestampBehavior: ServerTimestampBehavior): Timestamp? = typed(field, get(field, serverTimestampBehavior))
    public actual fun getBlob(field: String): Blob? = typed(field, get(field))
    public actual fun getGeoPoint(field: String): GeoPoint? = typed(field, get(field))
    public actual fun getDocumentReference(field: String): DocumentReference? = typed(field, get(field))

    /** The data as the JS SDK reads it with the leaves as the layer's value types, which the dev.gitlive layer's decoders take. */
    internal fun nativeData(serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow { js.data(serverTimestampBehavior.toJs()) }.toCompatLeaves()

    internal fun nativeGet(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow { js.get(field, serverTimestampBehavior.toJs()) }.toCompatLeaves()

    internal fun nativeGet(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow { js.get(fieldPath.toJs(), serverTimestampBehavior.toJs()) }.toCompatLeaves()

    /** The JS SDK creates a new object per read, so snapshots are equal when they are of the same document and data. */
    override fun equals(other: Any?): Boolean = other is DocumentSnapshot && (other.js === js || snapshotEqual(js, other.js))

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "DocumentSnapshot(${js.ref.path})"

    public actual enum class ServerTimestampBehavior {
        NONE,
        ESTIMATE,
        PREVIOUS,
    }
}

private inline fun <reified T> typed(field: String, value: Any?): T? = when (value) {
    null -> null
    is T -> value
    else -> throw RuntimeException("Field '$field' is not a ${T::class.simpleName}, but is ${value::class.simpleName}")
}

/** @property js The underlying Firebase JS SDK object. */
public actual class QueryDocumentSnapshot internal constructor(js: JsDocumentSnapshot) : DocumentSnapshot(js)

/** @property js The underlying Firebase JS SDK object. */
public actual class QuerySnapshot internal constructor(public val js: JsQuerySnapshot) : Iterable<QueryDocumentSnapshot> {
    public actual val query: Query get() = Query(js.query)
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)
    public actual val documents: List<DocumentSnapshot> get() = js.docs.map { DocumentSnapshot(it) }
    public actual val documentChanges: List<DocumentChange> get() = rethrow { js.docChanges() }.map { DocumentChange(it) }

    public actual fun getDocumentChanges(metadataChanges: MetadataChanges): List<DocumentChange> = rethrow { js.docChanges(json("includeMetadataChanges" to (metadataChanges == MetadataChanges.INCLUDE))) }.map { DocumentChange(it) }

    public actual val isEmpty: Boolean get() = js.empty

    public actual fun size(): Int = js.size

    actual override fun iterator(): Iterator<QueryDocumentSnapshot> = js.docs.map { QueryDocumentSnapshot(it) }.iterator()

    override fun equals(other: Any?): Boolean = other is QuerySnapshot && (other.js === js || snapshotEqual(js, other.js))

    override fun hashCode(): Int = js.size

    override fun toString(): String = "QuerySnapshot(${js.size} documents)"
}
