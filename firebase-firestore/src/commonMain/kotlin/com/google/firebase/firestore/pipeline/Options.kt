/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore.pipeline

import dev.gitlive.firebase.firestore.PROTO_VALUE_ANDROID_ONLY
import dev.gitlive.firebase.firestore.INTERNAL_CONSTRUCTOR
import dev.gitlive.firebase.firestore.internal.pipeline.OptionValues
import kotlin.jvm.JvmField

/** A set of options of the pipeline API, built by the `with` functions; [T] is the concrete options class, which they return. */
public abstract class AbstractOptions<T : AbstractOptions<T>>
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val options: OptionValues,
) {
    /** A copy of this options class holding [options]. */
    internal abstract fun self(options: OptionValues): T

    public fun with(key: String, value: String): T = self(options + (key to value))

    public fun with(key: String, value: Boolean): T = self(options + (key to value))

    public fun with(key: String, value: Long): T = self(options + (key to value))

    public fun with(key: String, value: Double): T = self(options + (key to value))

    public fun with(key: String, value: Field): T = self(options + (key to value))

    public fun with(key: String, value: RawOptions): T = self(options + (key to value.options))

    /** Android/JVM only: an option given as the SDK's protobuf value; common code uses a typed overload. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(PROTO_VALUE_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public fun with(key: String, value: com.google.firestore.v1.Value): T = throw UnsupportedOperationException(PROTO_VALUE_ANDROID_ONLY)

    protected fun with(key: String, subSection: AbstractOptions<*>): T = self(options + (key to subSection.options))

    protected fun with(key: String, vararg values: String): T = self(options + (key to values.toList()))

    protected fun adding(newOptions: AbstractOptions<*>): T = self(options + newOptions.options)
}

/** Options with no predefined keys, for `with` on any options class. */
@Suppress("DEPRECATION_ERROR")
public class RawOptions private constructor(options: OptionValues) : AbstractOptions<RawOptions>(options) {
    override fun self(options: OptionValues): RawOptions = RawOptions(options)

    public companion object {
        /** No options. */
        @JvmField
        public val DEFAULT: RawOptions = RawOptions(emptyMap())
    }
}

/** Options of an `aggregate` stage. */
@Suppress("DEPRECATION_ERROR")
public class AggregateOptions private constructor(options: OptionValues) : AbstractOptions<AggregateOptions>(options) {
    public constructor() : this(emptyMap())

    override fun self(options: OptionValues): AggregateOptions = AggregateOptions(options)

    public fun withHints(hints: AggregateHints): AggregateOptions = with("hints", hints)
}

/** Hints for an `aggregate` stage. */
@Suppress("DEPRECATION_ERROR")
public class AggregateHints private constructor(options: OptionValues) : AbstractOptions<AggregateHints>(options) {
    public constructor() : this(emptyMap())

    override fun self(options: OptionValues): AggregateHints = AggregateHints(options)

    public fun withForceStreamableEnabled(): AggregateHints = with("force_streamable", true)
}

/** Options of a collection source stage. */
@Suppress("DEPRECATION_ERROR")
public class CollectionSourceOptions private constructor(options: OptionValues) : AbstractOptions<CollectionSourceOptions>(options) {
    public constructor() : this(emptyMap())

    override fun self(options: OptionValues): CollectionSourceOptions = CollectionSourceOptions(options)

    public fun withHints(hints: CollectionHints): CollectionSourceOptions = with("hints", hints)
}

/** Hints for a collection or collection group source stage. */
@Suppress("DEPRECATION_ERROR")
public class CollectionHints private constructor(options: OptionValues) : AbstractOptions<CollectionHints>(options) {
    public constructor() : this(emptyMap())

    override fun self(options: OptionValues): CollectionHints = CollectionHints(options)

    public fun withForceIndex(value: String): CollectionHints = with("force_index", value)

    public fun withIgnoreIndexFields(vararg values: String): CollectionHints = with("ignore_index_fields", *values)
}

/** Options of a collection group source stage. */
@Suppress("DEPRECATION_ERROR")
public class CollectionGroupOptions private constructor(options: OptionValues) : AbstractOptions<CollectionGroupOptions>(options) {
    public constructor() : this(emptyMap())

    override fun self(options: OptionValues): CollectionGroupOptions = CollectionGroupOptions(options)

    public fun withHints(hints: CollectionHints): CollectionGroupOptions = with("hints", hints)
}

/** Options of a `findNearest` stage. */
@Suppress("DEPRECATION_ERROR")
public class FindNearestOptions private constructor(options: OptionValues) : AbstractOptions<FindNearestOptions>(options) {
    public constructor() : this(emptyMap())

    override fun self(options: OptionValues): FindNearestOptions = FindNearestOptions(options)

    public fun withLimit(limit: Long): FindNearestOptions = with("limit", limit)

    public fun withDistanceField(distanceField: Field): FindNearestOptions = with("distance_field", distanceField)

    public fun withDistanceField(distanceField: String?): FindNearestOptions? = distanceField?.let { with("distance_field", Field.of(it)) }
}

/** Options of an `unnest` stage. */
@Suppress("DEPRECATION_ERROR")
public class UnnestOptions private constructor(options: OptionValues) : AbstractOptions<UnnestOptions>(options) {
    public constructor() : this(emptyMap())

    override fun self(options: OptionValues): UnnestOptions = UnnestOptions(options)

    public fun withIndexField(indexField: String): UnnestOptions = with("index_field", Field.of(indexField))
}
