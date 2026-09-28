/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.database.stub

/*
 * Header stubs for com.google.firebase:firebase-database (see buildSrc utils/HeaderStubs.kt): compiled against,
 * verified to match the real classes, and deleted from the output so the real SDK binds at runtime. Query's constructor
 * only exists for the DatabaseReference stub to extend it (the SDK's constructors take internal types); it is deprecated
 * with an error so that it need not match the real class and cannot be called.
 */

private const val STUB_CONSTRUCTOR = "Header stub constructor; the SDK's Query is not instantiable"

public actual class FirebaseDatabase private constructor() {
    public actual val app: FirebaseApp get() = stub()
    public actual val reference: DatabaseReference get() = stub()
    public actual fun getReference(path: String): DatabaseReference = stub()
    public actual fun getReferenceFromUrl(url: String): DatabaseReference = stub()
    public actual fun goOffline(): Unit = stub()
    public actual fun goOnline(): Unit = stub()
    public actual fun purgeOutstandingWrites(): Unit = stub()
    public actual fun setLogLevel(logLevel: Logger.Level): Unit = stub()
    public actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long): Unit = stub()
    public actual fun setPersistenceEnabled(isEnabled: Boolean): Unit = stub()
    public actual fun useEmulator(host: String, port: Int): Unit = stub()

    public actual companion object {
        @JvmStatic
        public actual fun getInstance(): FirebaseDatabase = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp): FirebaseDatabase = stub()

        @JvmStatic
        public actual fun getInstance(app: FirebaseApp, url: String): FirebaseDatabase = stub()

        @JvmStatic
        public actual fun getInstance(url: String): FirebaseDatabase = stub()

        @JvmStatic
        public actual fun getSdkVersion(): String = stub()
    }
}

@Suppress("DEPRECATION_ERROR")
public actual open class Query {
    @Deprecated(STUB_CONSTRUCTOR, level = DeprecationLevel.ERROR)
    protected constructor()

    public actual val ref: DatabaseReference get() = stub()
    public actual fun addValueEventListener(listener: ValueEventListener): ValueEventListener = stub()
    public actual fun addChildEventListener(listener: ChildEventListener): ChildEventListener = stub()
    public actual fun addListenerForSingleValueEvent(listener: ValueEventListener): Unit = stub()
    public actual fun removeEventListener(listener: ValueEventListener): Unit = stub()
    public actual fun removeEventListener(listener: ChildEventListener): Unit = stub()
    public actual fun get(): Task<DataSnapshot> = stub()

    /** Not in the common API (JS has no offline cache); the shipped nonJsMain extension binds to this member. */
    public fun keepSynced(keepSynced: Boolean): Unit = stub()
    public actual fun orderByChild(path: String): Query = stub()
    public actual fun orderByKey(): Query = stub()
    public actual fun orderByPriority(): Query = stub()
    public actual fun orderByValue(): Query = stub()
    public actual fun limitToFirst(limit: Int): Query = stub()
    public actual fun limitToLast(limit: Int): Query = stub()
    public actual fun startAt(value: String?): Query = stub()
    public actual fun startAt(value: String?, key: String?): Query = stub()
    public actual fun startAt(value: Double): Query = stub()
    public actual fun startAt(value: Double, key: String?): Query = stub()
    public actual fun startAt(value: Boolean): Query = stub()
    public actual fun startAt(value: Boolean, key: String?): Query = stub()
    public actual fun startAfter(value: String?): Query = stub()
    public actual fun startAfter(value: String?, key: String?): Query = stub()
    public actual fun startAfter(value: Double): Query = stub()
    public actual fun startAfter(value: Double, key: String?): Query = stub()
    public actual fun startAfter(value: Boolean): Query = stub()
    public actual fun startAfter(value: Boolean, key: String?): Query = stub()
    public actual fun endAt(value: String?): Query = stub()
    public actual fun endAt(value: String?, key: String?): Query = stub()
    public actual fun endAt(value: Double): Query = stub()
    public actual fun endAt(value: Double, key: String?): Query = stub()
    public actual fun endAt(value: Boolean): Query = stub()
    public actual fun endAt(value: Boolean, key: String?): Query = stub()
    public actual fun endBefore(value: String?): Query = stub()
    public actual fun endBefore(value: String?, key: String?): Query = stub()
    public actual fun endBefore(value: Double): Query = stub()
    public actual fun endBefore(value: Double, key: String?): Query = stub()
    public actual fun endBefore(value: Boolean): Query = stub()
    public actual fun endBefore(value: Boolean, key: String?): Query = stub()
    public actual fun equalTo(value: String?): Query = stub()
    public actual fun equalTo(value: String?, key: String?): Query = stub()
    public actual fun equalTo(value: Double): Query = stub()
    public actual fun equalTo(value: Double, key: String?): Query = stub()
    public actual fun equalTo(value: Boolean): Query = stub()
    public actual fun equalTo(value: Boolean, key: String?): Query = stub()
}

@Suppress("DEPRECATION_ERROR")
public actual open class DatabaseReference private constructor() : Query() {
    public actual val database: FirebaseDatabase get() = stub()
    public actual val key: String? get() = stub()
    public actual val parent: DatabaseReference? get() = stub()
    public actual val root: DatabaseReference get() = stub()
    public actual fun child(pathString: String): DatabaseReference = stub()
    public actual fun push(): DatabaseReference = stub()
    public actual fun onDisconnect(): OnDisconnect = stub()
    public actual fun setValue(value: Any?): Task<Nothing?> = stub()
    public actual fun setValue(value: Any?, listener: CompletionListener?): Unit = stub()
    public actual fun setValue(value: Any?, priority: Any?): Task<Nothing?> = stub()
    public actual fun setValue(value: Any?, priority: Any?, listener: CompletionListener?): Unit = stub()
    public actual fun setPriority(priority: Any?): Task<Nothing?> = stub()
    public actual fun setPriority(priority: Any?, listener: CompletionListener?): Unit = stub()
    public actual fun updateChildren(update: Map<String, Any?>): Task<Nothing?> = stub()
    public actual fun updateChildren(update: Map<String, Any?>, listener: CompletionListener?): Unit = stub()
    public actual fun removeValue(): Task<Nothing?> = stub()
    public actual fun removeValue(listener: CompletionListener?): Unit = stub()
    public actual fun runTransaction(handler: Transaction.Handler): Unit = stub()
    public actual fun runTransaction(handler: Transaction.Handler, fireLocalEvents: Boolean): Unit = stub()

    public actual fun interface CompletionListener {
        public actual fun onComplete(error: DatabaseError?, ref: DatabaseReference)
    }

    public actual companion object {
        @JvmStatic
        public actual fun goOffline(): Unit = stub()

        @JvmStatic
        public actual fun goOnline(): Unit = stub()
    }
}

public actual class DataSnapshot private constructor() {
    public actual fun exists(): Boolean = stub()
    public actual val key: String? get() = stub()
    public actual val ref: DatabaseReference get() = stub()
    public actual val value: Any? get() = stub()
    public actual fun getValue(useExportFormat: Boolean): Any? = stub()
    public actual fun <T> getValue(t: GenericTypeIndicator<T>): T? = stub()
    public actual val priority: Any? get() = stub()
    public actual fun child(path: String): DataSnapshot = stub()
    public actual fun hasChild(path: String): Boolean = stub()
    public actual fun hasChildren(): Boolean = stub()
    public actual val childrenCount: Long get() = stub()
    public actual val children: Iterable<DataSnapshot> get() = stub()
}

public actual class MutableData private constructor() {
    public actual val key: String? get() = stub()
    public actual var value: Any?
        get() = stub()
        set(_) = stub()
    public actual fun <T> getValue(t: GenericTypeIndicator<T>): T? = stub()
    public actual var priority: Any?
        get() = stub()
        set(_) = stub()
    public actual fun child(path: String): MutableData = stub()
    public actual fun hasChild(path: String): Boolean = stub()
    public actual fun hasChildren(): Boolean = stub()
    public actual val childrenCount: Long get() = stub()
    public actual val children: Iterable<MutableData> get() = stub()
}

public actual class Transaction actual constructor() {
    public actual interface Handler {
        public actual fun doTransaction(currentData: MutableData): Result
        public actual fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?)
    }

    public actual class Result private constructor() {
        public actual val isSuccess: Boolean get() = stub()
    }

    public actual companion object {
        @JvmStatic
        public actual fun abort(): Result = stub()

        @JvmStatic
        public actual fun success(resultData: MutableData): Result = stub()
    }
}

public actual class OnDisconnect private constructor() {
    public actual fun setValue(value: Any?): Task<Nothing?> = stub()
    public actual fun setValue(value: Any?, listener: DatabaseReference.CompletionListener?): Unit = stub()
    public actual fun setValue(value: Any?, priority: String?): Task<Nothing?> = stub()
    public actual fun setValue(value: Any?, priority: String?, listener: DatabaseReference.CompletionListener?): Unit = stub()
    public actual fun setValue(value: Any?, priority: Double): Task<Nothing?> = stub()
    public actual fun setValue(value: Any?, priority: Double, listener: DatabaseReference.CompletionListener?): Unit = stub()
    public actual fun setValue(value: Any?, priority: Map<*, *>?, listener: DatabaseReference.CompletionListener?): Unit = stub()
    public actual fun updateChildren(update: Map<String, Any?>): Task<Nothing?> = stub()
    public actual fun updateChildren(update: Map<String, Any?>, listener: DatabaseReference.CompletionListener?): Unit = stub()
    public actual fun removeValue(): Task<Nothing?> = stub()
    public actual fun removeValue(listener: DatabaseReference.CompletionListener?): Unit = stub()
    public actual fun cancel(): Task<Nothing?> = stub()
    public actual fun cancel(listener: DatabaseReference.CompletionListener): Unit = stub()
}

public actual class DatabaseError private constructor() {
    public actual val code: Int get() = stub()
    public actual val message: String get() = stub()
    public actual val details: String get() = stub()
    public actual fun toException(): DatabaseException = stub()

    public actual companion object {
        @JvmField
        public actual val DATA_STALE: Int = -1

        @JvmField
        public actual val OPERATION_FAILED: Int = -2

        @JvmField
        public actual val PERMISSION_DENIED: Int = -3

        @JvmField
        public actual val DISCONNECTED: Int = -4

        @JvmField
        public actual val EXPIRED_TOKEN: Int = -6

        @JvmField
        public actual val INVALID_TOKEN: Int = -7

        @JvmField
        public actual val MAX_RETRIES: Int = -8

        @JvmField
        public actual val OVERRIDDEN_BY_SET: Int = -9

        @JvmField
        public actual val UNAVAILABLE: Int = -10

        @JvmField
        public actual val USER_CODE_EXCEPTION: Int = -11

        @JvmField
        public actual val NETWORK_ERROR: Int = -24

        @JvmField
        public actual val WRITE_CANCELED: Int = -25

        @JvmField
        public actual val UNKNOWN_ERROR: Int = -999

        @JvmStatic
        public actual fun fromException(e: Throwable): DatabaseError = stub()
    }
}

public actual open class DatabaseException : RuntimeException {
    public actual constructor(message: String) : super(message)
    public actual constructor(message: String, cause: Throwable) : super(message, cause)
}

public actual class ServerValue actual constructor() {
    public actual companion object {
        @JvmField
        public actual val TIMESTAMP: Map<String, String> = mapOf(".sv" to "timestamp")

        @JvmStatic
        public actual fun increment(delta: Long): Any = stub()

        @JvmStatic
        public actual fun increment(delta: Double): Any = stub()
    }
}
