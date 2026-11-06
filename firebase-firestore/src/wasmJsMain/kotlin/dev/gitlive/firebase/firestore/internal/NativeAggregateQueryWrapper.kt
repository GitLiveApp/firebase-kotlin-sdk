package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.externals.awaitValue
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.jsonStringify
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.firestore.AggregateField
import dev.gitlive.firebase.firestore.AggregateSource
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.NativeAggregateQuery
import dev.gitlive.firebase.firestore.NativeAggregateQuerySnapshot
import dev.gitlive.firebase.firestore.NativeQuery
import dev.gitlive.firebase.firestore.externals.getAggregateFromServer
import dev.gitlive.firebase.firestore.wrapped

internal actual class NativeAggregateQueryWrapper actual constructor(actual val native: NativeAggregateQuery) {

    actual val query: NativeQuery get() = native.query.wrapped

    actual suspend fun get(source: AggregateSource): NativeAggregateQuerySnapshot = getAggregateFromServer(
        query.js,
        json(
            *native.aggregateFields.map { field ->
                field.alias to field.js
            }.toTypedArray(),
        ),
    ).awaitValue().let(::NativeAggregateQuerySnapshot)
}

internal actual class NativeAggregateQuerySnapshotWrapper actual constructor(actual val native: NativeAggregateQuerySnapshot) {

    actual val query: NativeAggregateQuery get() = NativeAggregateQuery(native.js.query, aggregateFields())
    actual val count: Long get() = (jsGet(native.js.data(), AggregateField.Count.alias)!!.toKotlin() as Number).toLong()

    actual operator fun get(aggregateField: AggregateField): Number? = jsGet(native.js.data(), aggregateField.alias)?.toKotlin() as? Number
    actual operator fun get(averageAggregateField: AggregateField.Average): Double? = getDouble(averageAggregateField)
    actual operator fun get(countAggregateField: AggregateField.Count): Long = getLong(countAggregateField)!!
    actual fun getDouble(aggregateField: AggregateField): Double? = get(aggregateField)?.toDouble()
    actual fun getLong(aggregateField: AggregateField): Long? = get(aggregateField)?.toLong()

    private fun aggregateFields(): List<AggregateField> {
        // JS Does not have the AggregateQuery as a separate object, but we can extract it from data
        val dataString = jsonStringify(native.js.data())
        val elements = dataString.substring(1, dataString.length - 1).split(",")
        return elements.mapNotNull { element ->
            val alias = element.drop(1).takeWhile { it != '"' }
            when {
                alias == AggregateField.Count.alias -> AggregateField.Count

                alias.startsWith("sumOf") -> AggregateField.sum(
                    FieldPath(
                        *alias.removePrefix("sumOf").split(".").toTypedArray(),
                    ),
                )

                alias.startsWith("averageOf") -> AggregateField.average(
                    FieldPath(
                        *alias.removePrefix("averageOf").split(".").toTypedArray(),
                    ),
                )

                else -> null
            }
        }
    }
}
