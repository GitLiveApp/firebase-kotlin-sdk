package dev.gitlive.firebase

import kotlinx.serialization.KSerializer

/** A serializer of a special Firebase value that shall not be additionally encoded. */
interface FirebaseSpecialValueSerializer<T> : KSerializer<T>
