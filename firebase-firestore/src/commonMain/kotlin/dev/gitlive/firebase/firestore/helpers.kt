package dev.gitlive.firebase.firestore

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmName

//** Helper method to perform an update operation. */
@JvmName("performUpdateFields")
fun encodeFieldAndValue(
    fieldsAndValues: Array<out Pair<String, Any?>>,
    encodeDefaults: Boolean = true,
    serializersModule: SerializersModule = EmptySerializersModule()
) = encodeFieldAndValue(fieldsAndValues, encodeField = { it }, encodeValue = { encode(it, encodeDefaults, serializersModule) })

/** Helper method to perform an update operation. */
@JvmName("performUpdateFieldPaths")
fun encodeFieldAndValue(
    fieldsAndValues: Array<out Pair<FieldPath, Any?>>,
    encodeDefaults: Boolean = true,
    serializersModule: SerializersModule = EmptySerializersModule()
) = encodeFieldAndValue(fieldsAndValues, { it.encoded }, { encode(it, encodeDefaults, serializersModule) })

/** Helper method to perform an update operation in Android and JS. */
internal fun <T, K> encodeFieldAndValue(
    fieldsAndValues: Array<out Pair<T, Any?>>,
    encodeField: (T) -> K,
    encodeValue: (Any?) -> Any?
) : List<Pair<K, Any?>>? =
    fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
        ?.map { (field, value) -> encodeField(field) to value?.let { encodeValue(it) } }

internal fun <K, R> List<Pair<K, Any?>>.performUpdate(
    update: (K, Any?, Array<Any?>) -> R
) = update(
    this[0].first,
    this[0].second,
    this.drop(1).flatMap { (field, value) -> listOf(field, value) }.toTypedArray()
)
