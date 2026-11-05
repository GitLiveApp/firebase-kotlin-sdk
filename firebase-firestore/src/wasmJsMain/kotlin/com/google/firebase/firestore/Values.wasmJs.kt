/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Timestamp
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsIsArray
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.jsTypeOf
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.externals.toList
import dev.gitlive.firebase.firestore.externals.Bytes
import dev.gitlive.firebase.firestore.externals.QueryConstraint
import dev.gitlive.firebase.firestore.externals.deleteField
import dev.gitlive.firebase.firestore.externals.documentId
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array
import kotlin.js.JsException
import kotlin.js.Promise
import kotlin.math.abs
import kotlin.math.floor
import dev.gitlive.firebase.firestore.externals.DocumentReference as JsDocumentReference
import dev.gitlive.firebase.firestore.externals.FieldPath as JsFieldPath
import dev.gitlive.firebase.firestore.externals.GeoPoint as JsGeoPoint
import dev.gitlive.firebase.firestore.externals.Timestamp as JsTimestamp
import dev.gitlive.firebase.firestore.externals.and as jsAnd
import dev.gitlive.firebase.firestore.externals.arrayRemove as jsArrayRemove
import dev.gitlive.firebase.firestore.externals.arrayUnion as jsArrayUnion
import dev.gitlive.firebase.firestore.externals.increment as jsIncrement
import dev.gitlive.firebase.firestore.externals.or as jsOr
import dev.gitlive.firebase.firestore.externals.serverTimestamp as jsServerTimestamp
import dev.gitlive.firebase.firestore.externals.where as jsWhere
import dev.gitlive.firebase.firestore.externals.VectorValue as JsVectorValue
import dev.gitlive.firebase.firestore.externals.maximum as jsMaximum
import dev.gitlive.firebase.firestore.externals.minimum as jsMinimum

/*
 * The JS SDK reads and writes plain JS values with its own Timestamp, GeoPoint, DocumentReference and Bytes classes;
 * the layer presents them as the Android SDK's types: Map, List, String, Boolean, Long (integral numbers), Double,
 * com.google.firebase.Timestamp, GeoPoint, DocumentReference and Blob.
 */

private const val MAX_EXACT_INTEGER = 9007199254740992.0

/**
 * [this] as the JS SDK writes it, through the format: Kotlin maps become objects, collections arrays and longs numbers,
 * the layer's value types become the SDK's (see [toSdkValue]) and a `@Serializable` class is mapped with the Android
 * SDK's rules (see [firestoreFormat]).
 */
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun Any?.toJs(): JsAny? = firestoreFormat.convertToPlainValue(this) as JsAny?

/** A value read by the JS SDK as the Android SDK's types (see [toJs]). */
@Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun JsAny?.toCompat(): Any? {
    if (this == null) return null
    return when (jsTypeOf(this)) {
        "undefined" -> null
        "string", "boolean" -> toKotlin()
        "number" -> toCompatNumber()
        "object" -> when {
            this is JsTimestamp -> Timestamp(seconds.toLong(), nanoseconds.toInt())
            this is JsGeoPoint -> GeoPoint(latitude, longitude)
            this is JsVectorValue -> VectorValue.of(toArray().toDoubleArray())
            this is JsDocumentReference -> DocumentReference(this)
            this is Bytes -> Blob.fromBytes(toUint8Array().toByteArray())
            jsIsArray(this) -> (this as JsArray<JsAny?>).toList().map { it.toCompat() }
            else -> objectKeys(this).associateWith { key -> jsGet(this, key).toCompat() }
        }
        else -> this
    }
}

/** A JS number as the Android SDK reads it: a [Long] when integral, a [Double] otherwise. */
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun JsAny.toCompatNumber(): Number {
    val number = (this as JsNumber).toDouble()
    return if (number == floor(number) && abs(number) < MAX_EXACT_INTEGER) number.toLong() else number
}

internal fun FieldValue.toJsFieldValue(): JsAny = when (operation) {
    FieldValue.Operation.SERVER_TIMESTAMP -> jsServerTimestamp()
    FieldValue.Operation.DELETE -> deleteField()
    FieldValue.Operation.ARRAY_UNION -> jsArrayUnion(*arguments.map { it.toJs() }.toTypedArray())
    FieldValue.Operation.ARRAY_REMOVE -> jsArrayRemove(*arguments.map { it.toJs() }.toTypedArray())
    FieldValue.Operation.INCREMENT -> jsIncrement((arguments.single() as Number).toDouble())
    FieldValue.Operation.MINIMUM -> jsMinimum((arguments.single() as Number).toDouble())
    FieldValue.Operation.MAXIMUM -> jsMaximum((arguments.single() as Number).toDouble())
}

/** The numbers of a JS SDK vector. */
internal fun JsArray<JsNumber>.toDoubleArray(): DoubleArray = DoubleArray(length) { this[it]!!.toDouble() }

/** A JS array of numbers, as the JS SDK's `vector` takes. */
internal fun DoubleArray.toJsNumberArray(): JsArray<JsNumber> = JsArray<JsNumber>().also { array -> forEachIndexed { index, value -> array[index] = value.toJsNumber() } }

internal fun FieldPath.toJs(): JsFieldPath = if (isDocumentId) documentId() else JsFieldPath(*segments.toTypedArray())

internal fun SetOptions.toJs(): JsAny = if (merge) json("merge" to true) else json("mergeFields" to mergeFields.orEmpty().map { it.toJs() })

internal fun Filter.Node.toJs(): QueryConstraint = when (this) {
    is Filter.Node.Field -> jsWhere(fieldPath.toJs(), operator.jsOperator, value.toJs())
    is Filter.Node.Composite -> when (operator) {
        Filter.Operator.AND -> jsAnd(*filters.map { it.toJs() }.toTypedArray())
        else -> jsOr(*filters.map { it.toJs() }.toTypedArray())
    }
}

internal fun ByteArray.toUint8Array(): Uint8Array {
    val int8 = toInt8Array()
    return Uint8Array(int8.buffer, int8.byteOffset, int8.length)
}

internal fun Uint8Array.toByteArray(): ByteArray = Int8Array(buffer, byteOffset, length).toByteArray()

/** `Object.keys` of a JS object. */
internal fun objectKeys(value: JsAny): List<String> = jsObjectKeys(value).toList().map { it.toKotlinString() }

/** `String(value)`, the JS SDK's description of a value. */
internal fun jsToString(value: JsAny): String = js("String(value)")

/** The JS SDK's `FirestoreError` (with a `code` such as `permission-denied`) as the Android SDK's exception. */
internal fun Throwable.toFirestoreException(): FirebaseFirestoreException = when {
    this is FirebaseFirestoreException -> this
    this is JsException && thrownValue != null -> thrownValue!!.toFirestoreException(this)
    else -> FirebaseFirestoreException(message ?: toString(), FirebaseFirestoreException.Code.UNKNOWN, this)
}

/** A promise rejection or an error callback carries the JS error itself rather than a [Throwable]. */
internal fun JsAny.toFirestoreException(cause: Throwable? = null): FirebaseFirestoreException {
    val code = (jsGet(this, "code").toKotlin() as? String)?.uppercase()?.replace('-', '_')
    val message = jsGet(this, "message").toKotlin() as? String
    return FirebaseFirestoreException(
        message ?: code ?: jsToString(this),
        FirebaseFirestoreException.Code.entries.firstOrNull { it.name == code } ?: FirebaseFirestoreException.Code.UNKNOWN,
        cause ?: toThrowableOrNull() ?: Exception(message ?: jsToString(this)),
    )
}

internal inline fun <T : JsAny?, R> task(start: () -> Promise<T>, crossinline result: (T) -> R): Task<R> {
    val source = TaskCompletionSource<R>()
    try {
        start().then(
            onFulfilled = {
                source.setResult(result(it))
                null
            },
            onRejected = {
                source.setException(it.toFirestoreException())
                null
            },
        )
    } catch (e: Throwable) {
        source.setException(e.toFirestoreException())
    }
    return source.task
}

internal inline fun write(start: () -> Promise<JsAny?>): Task<Nothing?> = task(start) { null }

internal inline fun <R> rethrow(function: () -> R): R = try {
    function()
} catch (e: Throwable) {
    throw e.toFirestoreException()
}
