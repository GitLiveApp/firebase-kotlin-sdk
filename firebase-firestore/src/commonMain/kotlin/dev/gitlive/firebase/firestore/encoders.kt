package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.Blob
import dev.gitlive.firebase.EncodeSettings

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
