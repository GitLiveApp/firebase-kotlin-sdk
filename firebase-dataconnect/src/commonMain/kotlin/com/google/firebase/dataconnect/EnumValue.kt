/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

/**
 * The value of a Data Connect enum field: [Known] when the server's value is one of the [Enum] constants this code was
 * compiled with, [Unknown] (carrying the string) when the enum gained a value since. Serialized by
 * [com.google.firebase.dataconnect.serializers.EnumValueSerializer].
 */
public sealed interface EnumValue<out T : Enum<out T>> {

    /** [Known.value] in the case of [Known], or `null` in the case of [Unknown]. */
    public val value: T?

    /** The [Enum.name] of [Known.value], or the string the server sent in the case of [Unknown]. */
    public val stringValue: String

    /** An enum value that this code does not know. */
    public class Unknown(public override val stringValue: String) : EnumValue<Nothing> {
        /** Always `null`. */
        override val value: Nothing? = null

        override fun equals(other: Any?): Boolean = other is Unknown && stringValue == other.stringValue

        override fun hashCode(): Int = stringValue.hashCode()

        override fun toString(): String = "Unknown($stringValue)"

        /** A copy with the given value replaced. */
        public fun copy(stringValue: String = this.stringValue): Unknown = Unknown(stringValue)
    }

    /** An enum value that this code knows. */
    public class Known<T : Enum<T>>(override val value: T) : EnumValue<T> {
        override val stringValue: String
            get() = value.name

        override fun equals(other: Any?): Boolean = other is Known<*> && value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "Known(${value.name})"

        /** A copy with the given value replaced. */
        public fun copy(value: T = this.value): Known<T> = Known(value)
    }
}
