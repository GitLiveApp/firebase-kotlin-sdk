/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.serialization

import com.google.firebase.Timestamp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.capturedKClass

/** The path to a value, for error messages, as the Android SDK prints it: `a.b.[0].c`. */
internal class ErrorPath private constructor(private val parent: ErrorPath?, private val name: String?, val length: Int) {
    fun child(name: String) = ErrorPath(this, name, length + 1)

    fun suffix() = if (length > 0) " (found in field '$this')" else ""

    override fun toString(): String = when (length) {
        0 -> ""
        1 -> name!!
        else -> "$parent.$name"
    }

    companion object {
        val EMPTY = ErrorPath(null, null, 0)
    }
}

internal enum class UnknownProperties { IGNORE, LOG, THROW }

/** How a class is mapped, the counterpart of the Android SDK's `BeanMapper`. */
internal class ClassMapping(format: FirebaseFormat, val descriptor: SerialDescriptor) {
    private val rules = format.rules

    val className: String get() = descriptor.serialName.removeSuffix("?")

    /** The stored name of each element: its serial name, so the Kotlin property name unless `@SerialName` says otherwise. */
    val names: Array<String> = Array(descriptor.elementsCount, descriptor::getElementName)

    /** Elements written as the server timestamp when `null` (`@ServerTimestamp`). */
    val serverTimestamp: BooleanArray

    /** Elements that receive the document instead of a property (`@DocumentId`). */
    val documentId: BooleanArray

    val unknownProperties: UnknownProperties

    init {
        val count = descriptor.elementsCount
        // @ServerTimestamp and @DocumentId are Firestore's
        val firestore = rules is Rules.Firestore
        serverTimestamp = BooleanArray(count) { index ->
            (firestore && descriptor.getElementAnnotations(index).any { it is ServerTimestamp }).also { annotated ->
                if (annotated && !isTimestampType(descriptor.getElementDescriptor(index))) {
                    throw IllegalArgumentException(
                        "Field ${descriptor.getElementName(index)} is annotated with @ServerTimestamp but is ${typeString(descriptor.getElementDescriptor(index))} instead of Date, Timestamp, or Instant.",
                    )
                }
            }
        }
        documentId = BooleanArray(count) { index ->
            (firestore && descriptor.getElementAnnotations(index).any { it is DocumentId }).also { annotated ->
                if (annotated && !isDocumentIdType(rules, descriptor.getElementDescriptor(index))) {
                    throw IllegalArgumentException(
                        "Field is annotated with @DocumentId but is ${typeString(descriptor.getElementDescriptor(index))} instead of String or DocumentReference.",
                    )
                }
            }
        }
        val classAnnotations = descriptor.annotations
        unknownProperties = when {
            classAnnotations.any { it is ThrowOnExtraProperties } -> UnknownProperties.THROW
            rules.unknownPropertyLog == null || classAnnotations.any { it is IgnoreExtraProperties } -> UnknownProperties.IGNORE
            else -> UnknownProperties.LOG
        }
        // Duplicate serial names are rejected by kotlinx.serialization itself, so only an empty class is checked here.
        if (rules.requiresProperties && count == 0 && descriptor.kind != StructureKind.OBJECT) {
            throw rules.mappingError("No properties to serialize found on class $className")
        }
    }

    /** The message for a property without a class property. */
    fun unknownPropertyMessage(name: String): String {
        val lower = name.lowercase()
        val caseMismatch = names.any { it.lowercase() == lower }
        return "No setter/field for $name found on class $className" + if (caseMismatch) " (fields/setters are case sensitive!)" else ""
    }
}

/** Whether a `@ServerTimestamp` property may have this type: `Timestamp`, `kotlin.time.Instant` or `java.util.Date`. */
private fun isTimestampType(descriptor: SerialDescriptor): Boolean {
    val name = descriptor.serialName.removeSuffix("?")
    if (name == "kotlin.time.Instant" || name == "com.google.firebase.Timestamp" || name == "java.util.Date") return true
    if (descriptor.kind != SerialKind.CONTEXTUAL) return false
    val type = descriptor.capturedKClass ?: return false
    return type == Timestamp::class || javaClassNameOf(type) == "java.util.Date"
}

private fun isDocumentIdType(rules: Rules, descriptor: SerialDescriptor): Boolean = when {
    descriptor.kind == PrimitiveKind.STRING -> true
    descriptor.kind != SerialKind.CONTEXTUAL -> false
    else -> rules is Rules.Firestore && descriptor.capturedKClass == rules.bindings.documentReferenceType
}

/**
 * A type as `java.lang.reflect.Type.toString()` prints it, for messages: a primitive unless [boxed], which a nullable
 * type and a type argument always are.
 */
internal fun typeString(descriptor: SerialDescriptor, boxed: Boolean = descriptor.isNullable): String {
    if (descriptor.kind == SerialKind.CONTEXTUAL) descriptor.capturedKClass?.let { return "class ${javaClassNameOf(it)}" }
    return when (val name = descriptor.serialName.removeSuffix("?")) {
        "kotlin.String" -> "class java.lang.String"
        "kotlin.Int" -> if (boxed) "class java.lang.Integer" else "int"
        "kotlin.Long" -> if (boxed) "class java.lang.Long" else "long"
        "kotlin.Double" -> if (boxed) "class java.lang.Double" else "double"
        "kotlin.Float" -> if (boxed) "class java.lang.Float" else "float"
        "kotlin.Boolean" -> if (boxed) "class java.lang.Boolean" else "boolean"
        "kotlin.Short" -> if (boxed) "class java.lang.Short" else "short"
        "kotlin.Byte" -> if (boxed) "class java.lang.Byte" else "byte"
        "kotlin.Char" -> if (boxed) "class java.lang.Character" else "char"
        else -> when (descriptor.kind) {
            StructureKind.LIST -> "interface java.util.List"
            StructureKind.MAP -> "interface java.util.Map"
            else -> "class $name"
        }
    }
}

/** A value's class as `Class.toString()` prints it, for messages. */
internal fun javaClassString(value: Any) = "class ${javaClassName(value)}"

/** A value's class as `Class.getName()` returns it, for messages. */
internal fun javaClassName(value: Any): String = when (value) {
    is String -> "java.lang.String"
    is Long -> "java.lang.Long"
    is Int -> "java.lang.Integer"
    is Double -> "java.lang.Double"
    is Float -> "java.lang.Float"
    is Boolean -> "java.lang.Boolean"
    is Short -> "java.lang.Short"
    is Byte -> "java.lang.Byte"
    is Char -> "java.lang.Character"
    else -> javaClassNameOf(value::class)
}
