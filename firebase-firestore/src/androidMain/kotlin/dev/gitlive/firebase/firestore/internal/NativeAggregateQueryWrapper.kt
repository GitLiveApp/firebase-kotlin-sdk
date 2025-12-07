package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.AggregateField
import dev.gitlive.firebase.firestore.AggregateSource
import dev.gitlive.firebase.firestore.NativeAggregateQuery
import dev.gitlive.firebase.firestore.NativeAggregateQuerySnapshot
import dev.gitlive.firebase.firestore.NativeQuery
import kotlinx.coroutines.tasks.await

internal actual class NativeAggregateQueryWrapper actual constructor(actual val native: NativeAggregateQuery) {

    actual val query: NativeQuery get() = native.query

    actual suspend fun get(source: AggregateSource): NativeAggregateQuerySnapshot = native.get(when (source) {
        AggregateSource.SERVER -> com.google.firebase.firestore.AggregateSource.SERVER
    }).await()
}

internal actual class NativeAggregateQuerySnapshotWrapper actual constructor(actual val native: NativeAggregateQuerySnapshot) {
    actual val query: NativeAggregateQuery get() = native.query
    actual val count: Long get() = native.count

    actual operator fun get(aggregateField: AggregateField): Number? = native.get(aggregateField.android) as? Number
    actual operator fun get(averageAggregateField: AggregateField.Average): Double? = native.get(averageAggregateField.android)
    actual operator fun get(countAggregateField: AggregateField.Count): Long = native.get(countAggregateField.android)
    actual fun getDouble(aggregateField: AggregateField): Double? = native.getDouble(aggregateField.android)
    actual fun getLong(aggregateField: AggregateField): Long? = native.getLong(aggregateField.android)
}