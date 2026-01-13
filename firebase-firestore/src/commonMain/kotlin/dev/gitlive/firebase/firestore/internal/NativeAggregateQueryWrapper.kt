package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.AggregateField
import dev.gitlive.firebase.firestore.AggregateSource
import dev.gitlive.firebase.firestore.NativeAggregateQuery
import dev.gitlive.firebase.firestore.NativeAggregateQuerySnapshot
import dev.gitlive.firebase.firestore.NativeQuery

internal expect class NativeAggregateQueryWrapper(native: NativeAggregateQuery) {

    val native: NativeAggregateQuery
    val query: NativeQuery

    suspend fun get(source: AggregateSource = AggregateSource.SERVER): NativeAggregateQuerySnapshot
}

internal expect class NativeAggregateQuerySnapshotWrapper(native: NativeAggregateQuerySnapshot) {
    val native: NativeAggregateQuerySnapshot
    val query: NativeAggregateQuery
    val count: Long

    operator fun get(aggregateField: AggregateField): Number?
    operator fun get(averageAggregateField: AggregateField.Average): Double?
    operator fun get(countAggregateField: AggregateField.Count): Long
    fun getDouble(aggregateField: AggregateField): Double?
    fun getLong(aggregateField: AggregateField): Long?
}
