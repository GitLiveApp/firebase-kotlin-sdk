/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.firestore.internal.DEFAULT_MAX_ATTEMPTS

/**
 * A transaction, mirroring `com.google.firebase.firestore.Transaction` from the Firebase Android SDK: the writes are
 * applied atomically when the [Function] returns. Reads inside a transaction (`get`) are synchronous on Android, the JVM
 * and Apple platforms and are declared in the `nonJs` source set, as the JS SDK only reads asynchronously.
 */
public expect class Transaction {
    /** Writes [data] (a map of field values) to [documentRef], replacing the document. */
    public fun set(documentRef: DocumentReference, data: Any): Transaction

    /** Writes [data] (a map of field values) to [documentRef] according to [options]. */
    public fun set(documentRef: DocumentReference, data: Any, options: SetOptions): Transaction

    /** Updates the fields in [data] of [documentRef]; fails if the document does not exist. */
    public fun update(documentRef: DocumentReference, data: Map<String, Any?>): Transaction

    /** Updates [field] of [documentRef] to [value] and the further field/value pairs in [moreFieldsAndValues]. */
    public fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): Transaction

    /** Updates [fieldPath] of [documentRef] to [value] and the further field path/value pairs in [moreFieldsAndValues]. */
    public fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): Transaction

    /** Deletes [documentRef]. */
    public fun delete(documentRef: DocumentReference): Transaction

    /** The body of a transaction; may throw [FirebaseFirestoreException] to fail it. */
    public fun interface Function<TResult> {
        public fun apply(transaction: Transaction): TResult
    }
}

/** The options of a transaction, mirroring the Android SDK's `TransactionOptions`. */
public class TransactionOptions private constructor(
    /** The number of times the transaction is attempted before failing. */
    public val maxAttempts: Int,
) {
    public class Builder {
        private var maxAttempts: Int

        public constructor() {
            maxAttempts = DEFAULT_MAX_ATTEMPTS
        }

        public constructor(options: TransactionOptions) {
            maxAttempts = options.maxAttempts
        }

        /** The number of times the transaction is attempted; must be positive. */
        public fun setMaxAttempts(maxAttempts: Int): Builder {
            require(maxAttempts > 0) { "Max attempts must be at least 1" }
            this.maxAttempts = maxAttempts
            return this
        }

        public fun build(): TransactionOptions = TransactionOptions(maxAttempts)
    }
}

/**
 * A batch of writes, mirroring `com.google.firebase.firestore.WriteBatch` from the Firebase Android SDK: applied
 * atomically by [commit].
 */
public expect class WriteBatch {
    /** Writes [data] (a map of field values) to [documentRef], replacing the document. */
    public fun set(documentRef: DocumentReference, data: Any): WriteBatch

    /** Writes [data] (a map of field values) to [documentRef] according to [options]. */
    public fun set(documentRef: DocumentReference, data: Any, options: SetOptions): WriteBatch

    /** Updates the fields in [data] of [documentRef]; fails if the document does not exist. */
    public fun update(documentRef: DocumentReference, data: Map<String, Any?>): WriteBatch

    /** Updates [field] of [documentRef] to [value] and the further field/value pairs in [moreFieldsAndValues]. */
    public fun update(documentRef: DocumentReference, field: String, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch

    /** Updates [fieldPath] of [documentRef] to [value] and the further field path/value pairs in [moreFieldsAndValues]. */
    public fun update(documentRef: DocumentReference, fieldPath: FieldPath, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch

    /** Deletes [documentRef]. */
    public fun delete(documentRef: DocumentReference): WriteBatch

    /** Applies the writes atomically. */
    public fun commit(): Task<Nothing?>

    /** A function that fills a [WriteBatch], for `FirebaseFirestore.runBatch`. */
    public fun interface Function {
        public fun apply(batch: WriteBatch)
    }
}
