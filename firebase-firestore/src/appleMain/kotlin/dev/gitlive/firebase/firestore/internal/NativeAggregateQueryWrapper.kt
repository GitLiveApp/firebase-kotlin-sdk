package dev.gitlive.firebase.firestore.internal

import cocoapods.FirebaseFirestoreInternal.FIRAggregateSource
import dev.gitlive.firebase.firestore.AggregateField
import dev.gitlive.firebase.firestore.AggregateSource
import dev.gitlive.firebase.firestore.NativeAggregateQuery
import dev.gitlive.firebase.firestore.NativeAggregateQuerySnapshot
import dev.gitlive.firebase.firestore.NativeQuery
import dev.gitlive.firebase.firestore.awaitResult
import platform.Foundation.NSNumber

internal actual class NativeAggregateQueryWrapper actual constructor(actual val native: NativeAggregateQuery) {

    actual val query: NativeQuery get() = native.query

    actual suspend fun get(source: AggregateSource): NativeAggregateQuerySnapshot = awaitResult { callback ->
        native.aggregationWithSource(
            when (source) {
                AggregateSource.SERVER -> FIRAggregateSource.FIRAggregateSourceServer
            },
            callback,
        )
    }
}

internal actual class NativeAggregateQuerySnapshotWrapper actual constructor(actual val native: NativeAggregateQuerySnapshot) {
    actual val query: NativeAggregateQuery get() = native.query
    actual val count: Long get() = native.count().longValue

    @Suppress("CAST_NEVER_SUCCEEDS") // Should succeed just fine
    actual operator fun get(aggregateField: AggregateField): Number? = native.valueForAggregateField(aggregateField.ios) as? NSNumber as? Number
    actual operator fun get(averageAggregateField: AggregateField.Average): Double? = get(aggregateField = averageAggregateField)?.toDouble()
    actual operator fun get(countAggregateField: AggregateField.Count): Long = get(aggregateField = countAggregateField)!!.toLong()

    actual fun getDouble(aggregateField: AggregateField): Double? = get(aggregateField)?.toDouble()
    actual fun getLong(aggregateField: AggregateField): Long? = get(aggregateField)?.toLong()
}
