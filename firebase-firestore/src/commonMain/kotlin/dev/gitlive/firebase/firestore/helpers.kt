package dev.gitlive.firebase.firestore

/** Helper method to perform an update operation in Android and JS. */
internal fun <T, K, R> performUpdate(
    fieldsAndValues: Array<out Pair<T, Any?>>,
    encodeField: (T) -> K,
    encodeValue: (Any?) -> Any?,
    update: (K, Any?, Array<Any?>) -> R
) : R? =
    fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
        ?.map { (field, value) -> encodeField(field) to value?.let { encodeValue(it) } }
        ?.let { encoded ->
            update(
                encoded[0].first,
                encoded[0].second,
                encoded.drop(1).flatMap { (field, value) -> listOf(field, value) }.toTypedArray()
            )
        }
