package dev.gitlive.firebase.firestore.internal

import com.google.android.gms.tasks.TaskExecutors
import dev.gitlive.firebase.android.firestore.AggregateField
import dev.gitlive.firebase.android.firestore.AggregateSource
import dev.gitlive.firebase.android.firestore.FieldPath
import dev.gitlive.firebase.android.firestore.MetadataChanges
import dev.gitlive.firebase.android.firestore.Query
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.Filter
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.WhereConstraint
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal actual open class NativeQueryWrapper internal actual constructor(actual open val native: Query) {

    actual fun limit(limit: Number) = native.limit(limit.toLong())
    actual fun limitToLast(limit: Number) = native.limitToLast(limit.toLong())

    actual suspend fun count(): Long = native.count().get(AggregateSource.SERVER).await().count

    actual suspend fun sum(field: String): Double = aggregateDouble(AggregateField.sum(field)) ?: 0.0
    actual suspend fun sum(field: EncodedFieldPath): Double = aggregateDouble(AggregateField.sum(field)) ?: 0.0
    actual suspend fun average(field: String): Double? = aggregateDouble(AggregateField.average(field))
    actual suspend fun average(field: EncodedFieldPath): Double? = aggregateDouble(AggregateField.average(field))

    private suspend fun aggregateDouble(aggregateField: AggregateField): Double? = native.aggregate(aggregateField).get(AggregateSource.SERVER).await().getDouble(aggregateField)

    actual val snapshots get() = callbackFlow {
        val listener = native.addSnapshotListener { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val metadataChanges =
            if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = native.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual suspend fun get(source: Source): QuerySnapshot = QuerySnapshot(native.get(source.toAndroidSource()).await())

    actual fun where(filter: Filter) = native.where(filter.toAndroidFilter())

    private fun Filter.toAndroidFilter(): dev.gitlive.firebase.android.firestore.Filter = when (this) {
        is Filter.And -> dev.gitlive.firebase.android.firestore.Filter.and(
            *filters.map { it.toAndroidFilter() }
                .toTypedArray(),
        )
        is Filter.Or -> dev.gitlive.firebase.android.firestore.Filter.or(
            *filters.map { it.toAndroidFilter() }
                .toTypedArray(),
        )
        is Filter.Field -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: (String, Any?) -> dev.gitlive.firebase.android.firestore.Filter = when (constraint) {
                        is WhereConstraint.EqualTo -> dev.gitlive.firebase.android.firestore.Filter::equalTo
                        is WhereConstraint.NotEqualTo -> dev.gitlive.firebase.android.firestore.Filter::notEqualTo
                    }
                    modifier.invoke(field, constraint.value)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: (String, Any) -> dev.gitlive.firebase.android.firestore.Filter = when (constraint) {
                        is WhereConstraint.LessThan -> dev.gitlive.firebase.android.firestore.Filter::lessThan
                        is WhereConstraint.GreaterThan -> dev.gitlive.firebase.android.firestore.Filter::greaterThan
                        is WhereConstraint.LessThanOrEqualTo -> dev.gitlive.firebase.android.firestore.Filter::lessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> dev.gitlive.firebase.android.firestore.Filter::greaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> dev.gitlive.firebase.android.firestore.Filter::arrayContains
                    }
                    modifier.invoke(field, constraint.value)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: (String, List<Any>) -> dev.gitlive.firebase.android.firestore.Filter = when (constraint) {
                        is WhereConstraint.InArray -> dev.gitlive.firebase.android.firestore.Filter::inArray
                        is WhereConstraint.ArrayContainsAny -> dev.gitlive.firebase.android.firestore.Filter::arrayContainsAny
                        is WhereConstraint.NotInArray -> dev.gitlive.firebase.android.firestore.Filter::notInArray
                    }
                    modifier.invoke(field, constraint.values)
                }
            }
        }
        is Filter.Path -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: (FieldPath, Any?) -> dev.gitlive.firebase.android.firestore.Filter = when (constraint) {
                        is WhereConstraint.EqualTo -> dev.gitlive.firebase.android.firestore.Filter::equalTo
                        is WhereConstraint.NotEqualTo -> dev.gitlive.firebase.android.firestore.Filter::notEqualTo
                    }
                    modifier.invoke(path.android, constraint.value)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: (FieldPath, Any) -> dev.gitlive.firebase.android.firestore.Filter = when (constraint) {
                        is WhereConstraint.LessThan -> dev.gitlive.firebase.android.firestore.Filter::lessThan
                        is WhereConstraint.GreaterThan -> dev.gitlive.firebase.android.firestore.Filter::greaterThan
                        is WhereConstraint.LessThanOrEqualTo -> dev.gitlive.firebase.android.firestore.Filter::lessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> dev.gitlive.firebase.android.firestore.Filter::greaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> dev.gitlive.firebase.android.firestore.Filter::arrayContains
                    }
                    modifier.invoke(path.android, constraint.value)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: (FieldPath, List<Any>) -> dev.gitlive.firebase.android.firestore.Filter = when (constraint) {
                        is WhereConstraint.InArray -> dev.gitlive.firebase.android.firestore.Filter::inArray
                        is WhereConstraint.ArrayContainsAny -> dev.gitlive.firebase.android.firestore.Filter::arrayContainsAny
                        is WhereConstraint.NotInArray -> dev.gitlive.firebase.android.firestore.Filter::notInArray
                    }
                    modifier.invoke(path.android, constraint.values)
                }
            }
        }
    }

    actual fun orderBy(field: String, direction: Direction) = native.orderBy(field, direction)
    actual fun orderBy(field: EncodedFieldPath, direction: Direction) = native.orderBy(field, direction)

    actual fun startAfter(document: NativeDocumentSnapshot) = native.startAfter(document)
    actual fun startAfter(vararg fieldValues: Any) = native.startAfter(*fieldValues)
    actual fun startAt(document: NativeDocumentSnapshot) = native.startAt(document)
    actual fun startAt(vararg fieldValues: Any) = native.startAt(*fieldValues)

    actual fun endBefore(document: NativeDocumentSnapshot) = native.endBefore(document)
    actual fun endBefore(vararg fieldValues: Any) = native.endBefore(*fieldValues)
    actual fun endAt(document: NativeDocumentSnapshot) = native.endAt(document)
    actual fun endAt(vararg fieldValues: Any) = native.endAt(*fieldValues)

    private fun addSnapshotListener(
        includeMetadataChanges: Boolean = false,
        listener: ProducerScope<QuerySnapshot>.(dev.gitlive.firebase.android.firestore.QuerySnapshot?, dev.gitlive.firebase.android.firestore.FirebaseFirestoreException?) -> Unit,
    ) = callbackFlow {
        val executor = callbackExecutorMap[native.firestore] ?: TaskExecutors.MAIN_THREAD
        val metadataChanges =
            if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val registration =
            native.addSnapshotListener(executor, metadataChanges) { snapshots, exception ->
                listener(snapshots, exception)
            }
        awaitClose { registration.remove() }
    }
}
