package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.SetOptions
import dev.gitlive.firebase.encode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.SerializationStrategy

actual suspend fun <T> FirebaseFirestore.runTransactionFrozen(func: suspend Transaction.() -> T) : T  =
    this.runTransaction(func)

actual suspend fun CollectionReference.addFrozen(data: Any, encodeDefaults: Boolean): DocumentReference =
    this.add(data, encodeDefaults)

actual suspend fun <T> CollectionReference.addFrozen(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean): DocumentReference =
    this.add(data, strategy, encodeDefaults)

actual suspend fun DocumentReference.getFrozen(): DocumentSnapshot =
    this.get()

actual suspend fun DocumentReference.snapshotsFrozen(scope: CoroutineScope): Flow<DocumentSnapshot?> = this.snapshots

actual suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean, merge: Boolean) =
    this.set(data, encodeDefaults, merge)

// TODO improve this
actual suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean, vararg mergeFields: String): Unit =
    android.set(encode(data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
        .await().run { Unit }

actual suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): Unit =
    android.set(encode(data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.toList()))
        .await().run { Unit }

actual suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
    this.set(strategy, data, encodeDefaults, merge)

actual suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String): Unit =
    android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFields(*mergeFields))
        .await().run { Unit }

actual suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): Unit =
    android.set(encode(strategy, data, encodeDefaults)!!, SetOptions.mergeFieldPaths(mergeFieldPaths.toList()))
        .await().run { Unit }

actual suspend fun DocumentReference.updateFrozen(data: Any, encodeDefaults: Boolean) =
    this.update(data, encodeDefaults)

actual suspend fun <T> DocumentReference.updateFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
    this.update(strategy, data, encodeDefaults)

@JvmName("updateFieldsFrozen")
actual suspend fun DocumentReference.updateFrozen(vararg fieldsAndValues: Pair<String, Any?>) =
    this.update(fieldsAndValues)

@JvmName("updateFieldPathsFrozen")
actual suspend fun DocumentReference.updateFrozen(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
    this.update(fieldsAndValues)

actual suspend fun DocumentReference.deleteFrozen() =
    this.delete()

