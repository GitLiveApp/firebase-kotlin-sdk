package dev.gitlive.firebase

import kotlinx.serialization.SerializationStrategy

/**
 * An extension which which serializer to use for value. Handy in updating fields by name or path
 * where using annotation is not possible
 * @return a value with a custom serializer.
 */
public fun <T> T.withSerializer(serializer: SerializationStrategy<T>): Any = ValueWithSerializer(this, serializer)
public data class ValueWithSerializer<T>(val value: T, val serializer: SerializationStrategy<T>)
