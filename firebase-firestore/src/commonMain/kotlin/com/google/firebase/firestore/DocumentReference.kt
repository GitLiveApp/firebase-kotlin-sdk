/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task

/**
 * A document location, mirroring `com.google.firebase.firestore.DocumentReference` from the Firebase Android SDK: the
 * document may or may not exist; reads, writes and listeners are performed through it. Data is written as maps of
 * field values (a custom class is only mapped by reflection on Android).
 *
 * Not mirrored: the `Activity` and `Executor` overloads of `addSnapshotListener` (see `api/android-sdk/exclusions.txt`).
 */
public expect class DocumentReference {
    /** The [FirebaseFirestore] this reference belongs to. */
    public val firestore: FirebaseFirestore

    /** The last segment of the path: the document id. */
    public val id: String

    /** The path from the root, `/`-separated. */
    public val path: String

    /** The collection containing this document. */
    public val parent: CollectionReference

    /** The subcollection at [collectionPath], relative to this document. */
    public fun collection(collectionPath: String): CollectionReference

    /** Reads the document, from the cache and the backend as available. */
    public fun get(): Task<DocumentSnapshot>

    /** Reads the document from [source]. */
    public fun get(source: Source): Task<DocumentSnapshot>

    /** Writes [data] (a map of field values), replacing the document. */
    public fun set(data: Any): Task<Nothing?>

    /** Writes [data] (a map of field values) according to [options]. */
    public fun set(data: Any, options: SetOptions): Task<Nothing?>

    /** Updates the fields in [data]; fails if the document does not exist. */
    public fun update(data: Map<String, Any?>): Task<Nothing?>

    /** Updates [field] to [value] and the further field/value pairs in [moreFieldsAndValues]. */
    public fun update(field: String, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?>

    /** Updates [fieldPath] to [value] and the further field path/value pairs in [moreFieldsAndValues]. */
    public fun update(fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Task<Nothing?>

    /** Deletes the document. */
    public fun delete(): Task<Nothing?>

    /** Listens to the document and every change to it. */
    public fun addSnapshotListener(listener: EventListener<DocumentSnapshot>): ListenerRegistration

    /** Listens to the document, also notifying [listener] of metadata changes when [metadataChanges] is `INCLUDE`. */
    public fun addSnapshotListener(metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration

    /** Listens to the document with [options]. */
    public fun addSnapshotListener(options: SnapshotListenOptions, listener: EventListener<DocumentSnapshot>): ListenerRegistration
}
