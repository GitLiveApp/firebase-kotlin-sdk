// The JVM facade keeps the name of the Android source set file this moved from: inline callers compiled against 2.x reach
// isSpecialValue through it.
@file:JvmName("_encodersKt")

package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.Blob
import dev.gitlive.firebase.EncodeSettings
import kotlin.jvm.JvmName

/** @return whether value is special and shouldn't be encoded/decoded: one of the Android-SDK-shaped Firestore value types. */
@PublishedApi
internal fun isSpecialValue(value: Any): Boolean = when (value) {
    is NativeFieldValue,
    is NativeGeoPoint,
    is NativeTimestamp,
    is NativeDocumentReferenceType,
    is Blob,
    -> true
    else -> false
}

@PublishedApi
internal inline fun <reified T> encode(value: T, buildSettings: EncodeSettings.Builder.() -> Unit): Any? = if (value?.let(::isSpecialValue) == true) {
    value
} else {
    dev.gitlive.firebase.internal.encode(value, buildSettings)
}

/**
 * A special value as the decoders read it from the platform SDK, as its Android-SDK-shaped type: the value itself on
 * most platforms, where the `com.google.firebase.firestore` layer has converted the leaves; on wasmJs the JS SDK's
 * object, which a JS tree holds (a Kotlin value cannot be stored in one), converted here.
 */
internal expect fun Any.toCompatSpecialValue(): Any
