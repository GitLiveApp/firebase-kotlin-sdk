/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRDocumentChange
import cocoapods.FirebaseFirestoreInternal.FIRDocumentChangeType
import cocoapods.FirebaseFirestoreInternal.FIRDocumentSnapshot
import cocoapods.FirebaseFirestoreInternal.FIRQueryDocumentSnapshot
import cocoapods.FirebaseFirestoreInternal.FIRQuerySnapshot
import cocoapods.FirebaseFirestoreInternal.FIRServerTimestampBehavior
import cocoapods.FirebaseFirestoreInternal.FIRSnapshotMetadata
import com.google.firebase.Timestamp

private fun DocumentSnapshot.ServerTimestampBehavior.toIos(): FIRServerTimestampBehavior = when (this) {
    DocumentSnapshot.ServerTimestampBehavior.NONE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorNone
    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorEstimate
    DocumentSnapshot.ServerTimestampBehavior.PREVIOUS -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorPrevious
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class SnapshotMetadata internal constructor(public val ios: FIRSnapshotMetadata) {
    public actual fun hasPendingWrites(): Boolean = ios.pendingWrites
    public actual val isFromCache: Boolean get() = ios.fromCache

    override fun equals(other: Any?): Boolean = other is SnapshotMetadata && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.description ?: "SnapshotMetadata"
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual class DocumentChange internal constructor(public val ios: FIRDocumentChange) {
    public actual val document: QueryDocumentSnapshot get() = QueryDocumentSnapshot(ios.document)
    public actual val type: Type
        get() = when (ios.type) {
            FIRDocumentChangeType.FIRDocumentChangeTypeAdded -> Type.ADDED
            FIRDocumentChangeType.FIRDocumentChangeTypeModified -> Type.MODIFIED
            else -> Type.REMOVED
        }
    public actual val oldIndex: Int get() = ios.oldIndex.toInt()
    public actual val newIndex: Int get() = ios.newIndex.toInt()

    public actual enum class Type {
        ADDED,
        MODIFIED,
        REMOVED,
    }

    override fun equals(other: Any?): Boolean = other is DocumentChange && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.description ?: "DocumentChange"
}

/** @property ios The underlying Firebase iOS SDK object. */
public actual open class DocumentSnapshot internal constructor(public open val ios: FIRDocumentSnapshot) {
    public actual val id: String get() = ios.documentID
    public actual val reference: DocumentReference get() = DocumentReference(ios.reference)
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)
    public actual val data: Map<String, Any?>? get() = ios.data().toCompatData()

    public actual fun exists(): Boolean = ios.exists

    public actual fun getData(serverTimestampBehavior: ServerTimestampBehavior): Map<String, Any?>? = ios.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos()).toCompatData()

    public actual fun contains(field: String): Boolean = ios.valueForField(field) != null

    public actual fun contains(fieldPath: FieldPath): Boolean = ios.valueForField(fieldPath.toIos()) != null

    public actual fun get(field: String): Any? = ios.valueForField(field).toCompat()

    public actual fun get(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = ios.valueForField(field, serverTimestampBehavior.toIos()).toCompat()

    // Despite its name, valueForField accepts both a field name and a FIRFieldPath.
    public actual fun get(fieldPath: FieldPath): Any? = ios.valueForField(fieldPath.toIos()).toCompat()

    public actual fun get(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? = ios.valueForField(fieldPath.toIos(), serverTimestampBehavior.toIos()).toCompat()

    public actual fun getBoolean(field: String): Boolean? = typed(field, get(field))
    public actual fun getDouble(field: String): Double? = typed<Number>(field, get(field))?.toDouble()
    public actual fun getLong(field: String): Long? = typed<Number>(field, get(field))?.toLong()
    public actual fun getString(field: String): String? = typed(field, get(field))
    public actual fun getTimestamp(field: String): Timestamp? = typed(field, get(field))
    public actual fun getTimestamp(field: String, serverTimestampBehavior: ServerTimestampBehavior): Timestamp? = typed(field, get(field, serverTimestampBehavior))
    public actual fun getBlob(field: String): Blob? = typed(field, get(field))
    public actual fun getGeoPoint(field: String): GeoPoint? = typed(field, get(field))
    public actual fun getDocumentReference(field: String): DocumentReference? = typed(field, get(field))

    override fun equals(other: Any?): Boolean = other is DocumentSnapshot && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.description ?: "DocumentSnapshot($id)"

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

/** @property ios The underlying Firebase iOS SDK object. */
public actual class QueryDocumentSnapshot internal constructor(override val ios: FIRQueryDocumentSnapshot) : DocumentSnapshot(ios)

/** @property ios The underlying Firebase iOS SDK object. */
public actual class QuerySnapshot internal constructor(public val ios: FIRQuerySnapshot) : Iterable<QueryDocumentSnapshot> {
    public actual val query: Query get() = Query(ios.query)
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)
    public actual val documents: List<DocumentSnapshot> get() = ios.documents.map { QueryDocumentSnapshot(it as FIRQueryDocumentSnapshot) }
    public actual val documentChanges: List<DocumentChange> get() = ios.documentChanges.map { DocumentChange(it as FIRDocumentChange) }

    public actual fun getDocumentChanges(metadataChanges: MetadataChanges): List<DocumentChange> = ios.documentChangesWithIncludeMetadataChanges(metadataChanges == MetadataChanges.INCLUDE).map { DocumentChange(it as FIRDocumentChange) }

    public actual val isEmpty: Boolean get() = ios.isEmpty()

    public actual fun size(): Int = ios.count.toInt()

    actual override fun iterator(): Iterator<QueryDocumentSnapshot> = ios.documents.map { QueryDocumentSnapshot(it as FIRQueryDocumentSnapshot) }.iterator()

    override fun equals(other: Any?): Boolean = other is QuerySnapshot && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = ios.description ?: "QuerySnapshot"
}
