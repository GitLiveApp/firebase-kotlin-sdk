package dev.gitlive.firebase.firestore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy

expect suspend fun <T> FirebaseFirestore.runTransactionFrozen(func: suspend Transaction.() -> T) : T

expect suspend fun CollectionReference.addFrozen(data: Any, encodeDefaults: Boolean): DocumentReference
expect suspend fun <T> CollectionReference.addFrozen(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean = true): DocumentReference

expect suspend fun DocumentReference.getFrozen(): DocumentSnapshot
expect suspend fun DocumentReference.snapshotsFrozen(scope: CoroutineScope): Flow<DocumentSnapshot?>

@ImplicitReflectionSerializer
expect suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean = true, merge: Boolean = false)
@ImplicitReflectionSerializer
expect suspend fun  DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean = true, vararg mergeFields: String): Unit
@ImplicitReflectionSerializer
expect suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath) : Unit

expect suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, merge: Boolean = false)
expect suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFields: String)
expect suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true, vararg mergeFieldPaths: FieldPath) : Unit

@ImplicitReflectionSerializer
expect suspend fun DocumentReference.updateFrozen(data: Any, encodeDefaults: Boolean = true)
expect suspend fun <T> DocumentReference.updateFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean = true)

expect suspend fun DocumentReference.updateFrozen(vararg fieldsAndValues: Pair<String, Any?>)
expect suspend fun DocumentReference.updateFrozen(vararg fieldsAndValues: Pair<FieldPath, Any?>)

expect suspend fun DocumentReference.deleteFrozen()




