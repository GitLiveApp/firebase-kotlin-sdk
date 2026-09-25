/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.toJs
import dev.gitlive.firebase.firestore.internal.toCompat
import com.google.firebase.firestore.FirebaseFirestore as CompatFirebaseFirestore
import com.google.firebase.firestore.firestoreSettings as compatFirestoreSettings
import dev.gitlive.firebase.firestore.externals.CollectionReference as JsCollectionReference
import dev.gitlive.firebase.firestore.externals.DocumentChange as JsDocumentChange
import dev.gitlive.firebase.firestore.externals.DocumentReference as JsDocumentReference
import dev.gitlive.firebase.firestore.externals.DocumentSnapshot as JsDocumentSnapshot
import dev.gitlive.firebase.firestore.externals.FieldPath as JsFieldPath
import dev.gitlive.firebase.firestore.externals.Firestore as JsFirestore
import dev.gitlive.firebase.firestore.externals.Query as JsQuery
import dev.gitlive.firebase.firestore.externals.QuerySnapshot as JsQuerySnapshot
import dev.gitlive.firebase.firestore.externals.SnapshotMetadata as JsSnapshotMetadata
import dev.gitlive.firebase.firestore.externals.Transaction as JsTransaction
import dev.gitlive.firebase.firestore.externals.WriteBatch as JsWriteBatch

/** The underlying Firebase JS SDK object. */
public val FirebaseFirestore.js: JsFirestore get() = compat.js

public operator fun FirebaseFirestore.Companion.invoke(js: JsFirestore): FirebaseFirestore = FirebaseFirestore(CompatFirebaseFirestore.wrap(js))

public actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
) {

    public actual companion object {
        public actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024

        // According to documentation, default JS Firestore cache size is 40MB, not 100MB
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 40 * 1024 * 1024
    }

    public actual class Builder internal constructor(
        public actual var sslEnabled: Boolean,
        public actual var host: String,
        public actual var cacheSettings: LocalCacheSettings,
    ) {

        public actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
        )
        public actual constructor(settings: FirebaseFirestoreSettings) : this(settings.sslEnabled, settings.host, settings.cacheSettings)

        public actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings)
    }
}

public actual fun firestoreSettings(
    settings: FirebaseFirestoreSettings?,
    builder: FirebaseFirestoreSettings.Builder.() -> Unit,
): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply {
    settings?.let {
        sslEnabled = it.sslEnabled
        host = it.host
        cacheSettings = it.cacheSettings
    }
}.apply(builder).build()

internal actual fun FirebaseFirestoreSettings.applyTo(firestore: CompatFirebaseFirestore) {
    firestore.firestoreSettings = compatFirestoreSettings {
        setSslEnabled(sslEnabled)
        setHost(host)
        setLocalCacheSettings(cacheSettings.toCompat())
    }
}

/** The underlying Firebase JS SDK object. */
public val WriteBatch.js: JsWriteBatch get() = compat.js

public operator fun WriteBatch.Companion.invoke(js: JsWriteBatch): WriteBatch = WriteBatch(com.google.firebase.firestore.WriteBatch(js))

/** The underlying Firebase JS SDK object. */
public val Transaction.js: JsTransaction get() = compat.js

public operator fun Transaction.Companion.invoke(js: JsTransaction): Transaction = Transaction(com.google.firebase.firestore.Transaction(js))

/** The underlying Firebase JS SDK object. */
public val DocumentReference.js: JsDocumentReference get() = compat.js

public operator fun DocumentReference.Companion.invoke(js: JsDocumentReference): DocumentReference = DocumentReference(com.google.firebase.firestore.DocumentReference(js))

/** The underlying Firebase JS SDK object. */
public val Query.js: JsQuery get() = compat.js

public operator fun Query.Companion.invoke(js: JsQuery): Query = Query(com.google.firebase.firestore.Query(js))

/** The underlying Firebase JS SDK object. */
public val CollectionReference.js: JsCollectionReference get() = compat.js

public operator fun CollectionReference.Companion.invoke(js: JsCollectionReference): CollectionReference = CollectionReference(com.google.firebase.firestore.CollectionReference(js))

/** The underlying Firebase JS SDK object. */
public val QuerySnapshot.js: JsQuerySnapshot get() = compat.js

/** The underlying Firebase JS SDK object. */
public val DocumentChange.js: JsDocumentChange get() = compat.js

/** The underlying Firebase JS SDK object. */
public val DocumentSnapshot.js: JsDocumentSnapshot get() = compat.js

public operator fun DocumentSnapshot.Companion.invoke(js: JsDocumentSnapshot): DocumentSnapshot = DocumentSnapshot(com.google.firebase.firestore.DocumentSnapshot(js))

/** The underlying Firebase JS SDK object. */
public val SnapshotMetadata.js: JsSnapshotMetadata get() = compat.js

/** The field path as the Firebase JS SDK's. */
public val FieldPath.js: JsFieldPath get() = compat.toJs()
