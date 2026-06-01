package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.AggregateField
import dev.gitlive.firebase.firestore.AggregateSource
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.NativeAggregateQuery
import dev.gitlive.firebase.firestore.NativeAggregateQuerySnapshot
import dev.gitlive.firebase.firestore.NativeQuery
import dev.gitlive.firebase.firestore.externals.getAggregateFromServer
import dev.gitlive.firebase.firestore.externals.getCount
import dev.gitlive.firebase.firestore.wrapped
import kotlinx.coroutines.await
import kotlin.js.json

internal actual class NativeAggregateQueryWrapper actual constructor(actual val native: NativeAggregateQuery) {

    actual val query: NativeQuery get() = native.query.wrapped

    actual suspend fun get(source: AggregateSource): NativeAggregateQuerySnapshot = if (native.aggregateFields.size == 1 && native.aggregateFields.first() == AggregateField.Count.js) {
        getCount(query.js)
    } else {
        getAggregateFromServer(
            query.js,
            json(
                *native.aggregateFields.map { field ->
                    field.alias to field.js
                }.toTypedArray(),
            ),
        )
    }.await().let(::NativeAggregateQuerySnapshot)
}

internal actual class NativeAggregateQuerySnapshotWrapper actual constructor(actual val native: NativeAggregateQuerySnapshot) {

    actual val query: NativeAggregateQuery get() = NativeAggregateQuery(native.js.query, aggregateFields())
    actual val count: Long get() = (native.js.data()[AggregateField.Count.alias] as Int).toLong()

    actual operator fun get(aggregateField: AggregateField): Number? = native.js.data()[aggregateField.alias] as? Number
    actual operator fun get(averageAggregateField: AggregateField.Average): Double? = getDouble(averageAggregateField)
    actual operator fun get(countAggregateField: AggregateField.Count): Long = getLong(countAggregateField)!!
    actual fun getDouble(aggregateField: AggregateField): Double? = get(aggregateField)?.toDouble()
    actual fun getLong(aggregateField: AggregateField): Long? = get(aggregateField)?.toLong()

    private fun aggregateFields(): List<AggregateField> {
        // JS Does not have the AggregateQuery as a separate object, but we can extract it from data
        val dataString = JSON.stringify(native.js.data())
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
