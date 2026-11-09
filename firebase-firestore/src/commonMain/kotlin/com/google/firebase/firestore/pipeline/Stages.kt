/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore.pipeline

import dev.gitlive.firebase.firestore.INTERNAL_CONSTRUCTOR
import dev.gitlive.firebase.firestore.internal.pipeline.OptionValues
import dev.gitlive.firebase.firestore.internal.pipeline.selections
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/*
 * The stages of a pipeline that the Android SDK has classes for; the ones its Pipeline functions add without one are in
 * dev.gitlive.firebase.firestore.internal.pipeline. A stage carries [options], keyed as on the wire, which the platform
 * sends along with it where its SDK takes them.
 */

/** A stage of a [Pipeline]; [T] is the concrete stage, which the `withOption` functions return. */
public abstract class Stage<T : Stage<T>>
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
protected constructor(
    internal val name: String,
    internal val options: OptionValues,
) {
    /** A copy of this stage holding [options]. */
    internal abstract fun self(options: OptionValues): T

    public fun withOption(key: String, value: String): T = self(options + (key to value))

    public fun withOption(key: String, value: Boolean): T = self(options + (key to value))

    public fun withOption(key: String, value: Long): T = self(options + (key to value))

    public fun withOption(key: String, value: Double): T = self(options + (key to value))

    public fun withOption(key: String, value: Field): T = self(options + (key to value))

    public companion object
}

// Source stages

/** All documents of a collection. */
@Suppress("DEPRECATION_ERROR")
public class CollectionSource
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val path: String,
    options: OptionValues,
) : Stage<CollectionSource>("collection", options) {
    override fun self(options: OptionValues): CollectionSource = CollectionSource(path, options)
}

/** All documents of the collections named [collectionId], anywhere in the database. */
@Suppress("DEPRECATION_ERROR")
public class CollectionGroupSource
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    public val collectionId: String,
    options: OptionValues,
) : Stage<CollectionGroupSource>("collection_group", options) {
    override fun self(options: OptionValues): CollectionGroupSource = CollectionGroupSource(collectionId, options)
}

/** The documents of a subcollection of each document of the enclosing pipeline, for `union` and `unnest` sub-pipelines. */
@Suppress("DEPRECATION_ERROR")
public class SubcollectionSource
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val path: String,
    options: OptionValues,
) : Stage<SubcollectionSource>("subcollection", options) {
    override fun self(options: OptionValues): SubcollectionSource = SubcollectionSource(path, options)

    public companion object {
        @JvmStatic
        public fun of(path: String): SubcollectionSource = SubcollectionSource(path, emptyMap())
    }
}

/** Groups the documents by [groups] and computes [accumulators] over each group. */
@Suppress("DEPRECATION_ERROR")
public class AggregateStage private constructor(
    internal val accumulators: Map<String, AggregateFunction>,
    internal val groups: Map<String, Expression>,
    options: OptionValues,
) : Stage<AggregateStage>("aggregate", options) {
    override fun self(options: OptionValues): AggregateStage = AggregateStage(accumulators, groups, options)

    public fun withGroups(group: Selectable, vararg additionalGroups: Any): AggregateStage = AggregateStage(accumulators, selections(group, additionalGroups), options)

    public fun withGroups(groupField: String, vararg additionalGroups: Any): AggregateStage = AggregateStage(accumulators, selections(groupField, additionalGroups), options)

    public companion object {
        @JvmStatic
        public fun withAccumulators(accumulator: AliasedAggregate, vararg additionalAccumulators: AliasedAggregate): AggregateStage {
            val accumulators = LinkedHashMap<String, AggregateFunction>()
            for (aliased in listOf(accumulator) + additionalAccumulators) {
                require(accumulators.put(aliased.alias, aliased.aggregate) == null) { "Duplicate alias: '${aliased.alias}'" }
            }
            return AggregateStage(accumulators, emptyMap(), emptyMap())
        }
    }
}

/** The documents whose [vectorField] is nearest to [vectorValue] by [distanceMeasure]. */
@Suppress("DEPRECATION_ERROR")
public class FindNearestStage
@Deprecated(INTERNAL_CONSTRUCTOR, level = DeprecationLevel.ERROR)
internal constructor(
    internal val vectorField: Expression,
    internal val vectorValue: Expression,
    internal val distanceMeasure: DistanceMeasure,
    options: OptionValues,
) : Stage<FindNearestStage>("find_nearest", options) {
    override fun self(options: OptionValues): FindNearestStage = FindNearestStage(vectorField, vectorValue, distanceMeasure, options)

    /** How the distance between vectors is measured. */
    public class DistanceMeasure private constructor(internal val wire: String) {
        override fun toString(): String = wire

        public companion object {
            @JvmField
            public val EUCLIDEAN: DistanceMeasure = DistanceMeasure("euclidean")

            @JvmField
            public val COSINE: DistanceMeasure = DistanceMeasure("cosine")

            @JvmField
            public val DOT_PRODUCT: DistanceMeasure = DistanceMeasure("dot_product")
        }
    }

    public companion object
}

/** A stage the SDK has no class for, by name and arguments. */
@Suppress("DEPRECATION_ERROR")
public class RawStage private constructor(name: String, internal val arguments: List<Any?>, options: OptionValues) : Stage<RawStage>(name, options) {
    override fun self(options: OptionValues): RawStage = RawStage(name, arguments, options)

    public fun withArguments(vararg arguments: Any?): RawStage = RawStage(name, arguments.toList(), options)

    public companion object {
        @JvmStatic
        public fun ofName(name: String): RawStage = RawStage(name, emptyList(), emptyMap())
    }
}

/** A random sample of the documents: a number of them, or a percentage. */
@Suppress("DEPRECATION_ERROR")
public class SampleStage private constructor(internal val count: Number, internal val mode: Mode, options: OptionValues) : Stage<SampleStage>("sample", options) {
    override fun self(options: OptionValues): SampleStage = SampleStage(count, mode, options)

    /** Whether the sample is a count of documents or a percentage. */
    public class Mode private constructor(internal val wire: String) {
        override fun toString(): String = wire

        public companion object {
            public val DOCUMENTS: Mode = Mode("documents")
            public val PERCENT: Mode = Mode("percent")
        }
    }

    public companion object {
        @JvmStatic
        public fun withDocLimit(results: Int): SampleStage = SampleStage(results, Mode.DOCUMENTS, emptyMap())

        @JvmStatic
        public fun withPercentage(percentage: Double): SampleStage = SampleStage(percentage, Mode.PERCENT, emptyMap())
    }
}

/** A search over the documents, in preview. */
@Suppress("DEPRECATION_ERROR")
public class SearchStage private constructor(
    internal val query: BooleanExpression?,
    internal val rquery: String?,
    internal val limit: Long?,
    internal val sort: List<Ordering>,
    internal val offset: Long?,
    internal val retrievalDepth: Long?,
    internal val addFields: Map<String, Expression>,
    internal val languageCode: String?,
    options: OptionValues,
) : Stage<SearchStage>("search", options) {
    override fun self(options: OptionValues): SearchStage = copy(options = options)

    private fun copy(
        limit: Long? = this.limit,
        sort: List<Ordering> = this.sort,
        offset: Long? = this.offset,
        retrievalDepth: Long? = this.retrievalDepth,
        addFields: Map<String, Expression> = this.addFields,
        languageCode: String? = this.languageCode,
        options: OptionValues = this.options,
    ): SearchStage = SearchStage(query, rquery, limit, sort, offset, retrievalDepth, addFields, languageCode, options)

    public fun withAddFields(field: Selectable, vararg additionalFields: Selectable): SearchStage = copy(addFields = selections(field, additionalFields))

    public fun withSort(order: Ordering, vararg additionalOrderings: Ordering): SearchStage = copy(sort = listOf(order) + additionalOrderings)

    public fun withLimit(limit: Long): SearchStage = copy(limit = limit)

    public fun withRetrievalDepth(retrievalDepth: Long): SearchStage = copy(retrievalDepth = retrievalDepth)

    public fun withOffset(offset: Long): SearchStage = copy(offset = offset)

    public fun withLanguageCode(languageCode: String): SearchStage = copy(languageCode = languageCode)

    public companion object {
        @JvmStatic
        public fun withQuery(query: BooleanExpression): SearchStage = SearchStage(query, null, null, emptyList(), null, null, emptyMap(), null, emptyMap())

        @JvmStatic
        public fun withQuery(rquery: String): SearchStage = SearchStage(null, rquery, null, emptyList(), null, null, emptyMap(), null, emptyMap())
    }
}

/** Replaces each document by one per element of an array field, the element under [alias]. */
@Suppress("DEPRECATION_ERROR")
public class UnnestStage private constructor(internal val arrayWithAlias: Selectable, options: OptionValues) : Stage<UnnestStage>("unnest", options) {
    override fun self(options: OptionValues): UnnestStage = UnnestStage(arrayWithAlias, options)

    public fun withIndexField(indexField: String): UnnestStage = withOption("index_field", Field.of(indexField))

    public companion object {
        @JvmStatic
        public fun withField(arrayWithAlias: Selectable): UnnestStage = UnnestStage(arrayWithAlias, emptyMap())

        @JvmStatic
        public fun withField(arrayField: String, alias: String): UnnestStage = UnnestStage(AliasedExpression(alias, Field.of(arrayField)), emptyMap())
    }
}
