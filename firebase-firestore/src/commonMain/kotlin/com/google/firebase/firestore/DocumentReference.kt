/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import dev.gitlive.firebase.firestore.LISTENER_ACTIVITY_ANDROID_ONLY
import dev.gitlive.firebase.firestore.LISTENER_EXECUTOR_JVM_ONLY
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.firestore.REFLECTIVE_UPDATE
import dev.gitlive.firebase.firestore.REFLECTIVE_SET

/**
 * A document location, mirroring `com.google.firebase.firestore.DocumentReference` from the Firebase Android SDK: the
 * document may or may not exist; reads, writes and listeners are performed through it. A `@Serializable` class is
 * written with the [setData] and [updateData] extensions, which map it through `FirebaseFormat.Firestore` on every
 * platform; the SDK's `set` and `update(Map)`, which map a class by reflection on Android, are deprecated with an error
 * towards them.
 *
 * The `Activity` and `Executor` overloads of `addSnapshotListener` are Android/JVM only, declared through the error-deprecated
 * stand-ins (see `api/android-sdk/exclusions.txt`).
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
    @Deprecated(REFLECTIVE_SET, ReplaceWith("setData(data)", "com.google.firebase.serialization.setData"), DeprecationLevel.ERROR)
    public fun set(data: Any): Task<Nothing?>

    /** Writes [data] (a map of field values) according to [options]. */
    @Deprecated(REFLECTIVE_SET, ReplaceWith("setData(data, options)", "com.google.firebase.serialization.setData"), DeprecationLevel.ERROR)
    public fun set(data: Any, options: SetOptions): Task<Nothing?>

    /** Updates the fields in [data]; fails if the document does not exist. */
    @Deprecated(REFLECTIVE_UPDATE, ReplaceWith("updateData(data)", "com.google.firebase.serialization.updateData"), DeprecationLevel.ERROR)
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

    /** JVM only: [addSnapshotListener] called on [executor]; the multiplatform listener is called on the delivering thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addSnapshotListener(listener)"), DeprecationLevel.ERROR)
    public fun addSnapshotListener(executor: java.util.concurrent.Executor, listener: EventListener<DocumentSnapshot>): ListenerRegistration

    /** JVM only: [addSnapshotListener] called on [executor]; the multiplatform listener is called on the delivering thread. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(LISTENER_EXECUTOR_JVM_ONLY, ReplaceWith("addSnapshotListener(metadataChanges, listener)"), DeprecationLevel.ERROR)
    public fun addSnapshotListener(executor: java.util.concurrent.Executor, metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration

    /** Android only: [addSnapshotListener] scoped to [activity]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addSnapshotListener(listener)"), DeprecationLevel.ERROR)
    public fun addSnapshotListener(activity: android.app.Activity, listener: EventListener<DocumentSnapshot>): ListenerRegistration

    /** Android only: [addSnapshotListener] scoped to [activity]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(LISTENER_ACTIVITY_ANDROID_ONLY, ReplaceWith("addSnapshotListener(metadataChanges, listener)"), DeprecationLevel.ERROR)
    public fun addSnapshotListener(activity: android.app.Activity, metadataChanges: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration
}
