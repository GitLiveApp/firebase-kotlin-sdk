package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.*
import dev.gitlive.firebase.encode
import dev.gitlive.firebase.firestore.*
import kotlinx.cinterop.StableRef
import kotlinx.coroutines.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext
import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.NSError
import platform.posix.free

actual suspend fun <T> FirebaseFirestore.runTransactionFrozen(func: suspend Transaction.() -> T) : T  =
    awaitResultFrozen<Any?> { ios.runTransactionWithBlock(freezeObject { transaction, error ->
            runBlocking { Transaction(transaction!!).func().freeze() } }, it.freeze()).freeze() } as T

actual suspend fun CollectionReference.addFrozen(data: Any, encodeDefaults: Boolean): DocumentReference =
    DocumentReference(awaitFrozen { ios.addDocumentWithData(encode(data, encodeDefaults) as Map<Any?, *>, it) })

actual suspend fun <T> CollectionReference.addFrozen(data: T, strategy: SerializationStrategy<T>, encodeDefaults: Boolean): DocumentReference =
    DocumentReference(awaitFrozen { ios.addDocumentWithData(encode(strategy, data, encodeDefaults) as Map<Any?, *>) })

actual suspend fun DocumentReference.getFrozen(): DocumentSnapshot =
    DocumentSnapshot(awaitResultFrozen { ios.getDocumentWithCompletion(it.freeze()) })

actual suspend fun DocumentReference.snapshotsFrozen(scope: CoroutineScope): Flow<DocumentSnapshot?>  {
    val flow = StableRef.create(MutableStateFlow<DocumentSnapshot?>(null)).freeze()
    var listener: FIRListenerRegistrationProtocol? = null
    threadSafeSuspendCallback<Unit> { complete ->
        listener = ios.addSnapshotListener(freezeObject { snap, error ->
            snap?.let { flow.get().value = DocumentSnapshot(snap).freeze() }
            error?.let { throw error.toException() }
            // TODO improve this
            complete(Result.success(Unit))
        })
        ({})
    }
    return flow.get().onCompletion { listener?.remove(); flow.dispose() }
}

actual suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean, merge: Boolean) =
    awaitFrozen { ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, merge, it) }

actual suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean, vararg mergeFields: String) =
    awaitFrozen { ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, mergeFields.asList(), it) }

actual suspend fun DocumentReference.setFrozen(data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
    awaitFrozen { ios.setData(encode(data, encodeDefaults)!! as Map<Any?, *>, mergeFieldPaths.asList(), it) }

actual suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean) =
    awaitFrozen { ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, merge, it) }

actual suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) =
    awaitFrozen { ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, mergeFields.asList(), it) }

actual suspend fun <T> DocumentReference.setFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) =
    awaitFrozen { ios.setData(encode(strategy, data, encodeDefaults)!! as Map<Any?, *>, mergeFieldPaths.asList(), it) }

actual suspend fun DocumentReference.updateFrozen(data: Any, encodeDefaults: Boolean) =
    awaitFrozen { ios.updateData(encode(data, encodeDefaults) as Map<Any?, *>, it) }

actual suspend fun <T> DocumentReference.updateFrozen(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
    awaitFrozen { ios.updateData(encode(strategy, data, encodeDefaults) as Map<Any?, *>, it) }

actual suspend fun DocumentReference.updateFrozen(vararg fieldsAndValues: Pair<String, Any?>) =
    awaitFrozen { block -> ios.updateData(fieldsAndValues.associate { it }, block) }

actual suspend fun DocumentReference.updateFrozen(vararg fieldsAndValues: Pair<FieldPath, Any?>) =
    awaitFrozen { block -> ios.updateData(fieldsAndValues.associate { it }, block) }

actual suspend fun DocumentReference.deleteFrozen() =
    awaitFrozen { ios.deleteDocumentWithCompletion(it) }


private fun <T> freezeObject(obj: T) : T = obj.freeze()

suspend fun <T> awaitResultFrozen(function: (callback: (T?, NSError?) -> Unit) -> Unit): T =
    threadSafeSuspendCallback { complete ->
        function(freezeObject { result, error ->
            if (result != null) {
                complete(Result.success(result))
            } else if (error != null) {
                complete(Result.failure(error.toException()))
            }
        })
        ({})
    }

suspend fun <T> awaitFrozen(function: (callback: (NSError?) -> Unit) -> T): T {
    var result: T? = null
    threadSafeSuspendCallback<Unit> { complete ->
        result = function(freezeObject { error ->
            if (error == null)
                complete(Result.success(Unit))
            else
                complete(Result.failure(error.toException()))
        })
        ({})
    }
    return result!!
}

private fun NSError.toException() = when(domain) {
    FIRFirestoreErrorDomain -> when(code) {
        FIRFirestoreErrorCodeOK -> FirestoreExceptionCode.OK
        FIRFirestoreErrorCodeCancelled -> FirestoreExceptionCode.CANCELLED
        FIRFirestoreErrorCodeUnknown -> FirestoreExceptionCode.UNKNOWN
        FIRFirestoreErrorCodeInvalidArgument -> FirestoreExceptionCode.INVALID_ARGUMENT
        FIRFirestoreErrorCodeDeadlineExceeded -> FirestoreExceptionCode.DEADLINE_EXCEEDED
        FIRFirestoreErrorCodeNotFound -> FirestoreExceptionCode.NOT_FOUND
        FIRFirestoreErrorCodeAlreadyExists -> FirestoreExceptionCode.ALREADY_EXISTS
        FIRFirestoreErrorCodePermissionDenied -> FirestoreExceptionCode.PERMISSION_DENIED
        FIRFirestoreErrorCodeResourceExhausted -> FirestoreExceptionCode.RESOURCE_EXHAUSTED
        FIRFirestoreErrorCodeFailedPrecondition -> FirestoreExceptionCode.FAILED_PRECONDITION
        FIRFirestoreErrorCodeAborted -> FirestoreExceptionCode.ABORTED
        FIRFirestoreErrorCodeOutOfRange -> FirestoreExceptionCode.OUT_OF_RANGE
        FIRFirestoreErrorCodeUnimplemented -> FirestoreExceptionCode.UNIMPLEMENTED
        FIRFirestoreErrorCodeInternal -> FirestoreExceptionCode.INTERNAL
        FIRFirestoreErrorCodeUnavailable -> FirestoreExceptionCode.UNAVAILABLE
        FIRFirestoreErrorCodeDataLoss -> FirestoreExceptionCode.DATA_LOSS
        FIRFirestoreErrorCodeUnauthenticated -> FirestoreExceptionCode.UNAUTHENTICATED
        else -> FirestoreExceptionCode.UNKNOWN
    }
    else -> FirestoreExceptionCode.UNKNOWN
}.let { FirebaseFirestoreException(description!!, it) }

/** A lambda that is called when some work is complete, with the result */
private typealias CompletionLambda<T> = (result: Result<T>) -> Unit

/** An empty lambda that is called to cancel an ongoing async work */
private typealias CancellationLambda = () -> Unit

private suspend fun waitAndDelayForCondition(condition: () -> Boolean) {
    do {
        delay(50)
    } while (!condition())
}

@OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
private suspend fun <T> threadSafeSuspendCallback(startAsync: (CompletionLambda<T>) -> CancellationLambda): T {
    check(coroutineContext[ContinuationInterceptor] is Delay) {
        """Frozen Functions Works on CoroutineDispatchers that implement Delay.
            |Implement Delay for your dispatcher or use runBlocking.
        """.trimMargin()
    }

    val futureResult = AtomicReference<Result<T>?>(null).freeze()
    val isCancelled = AtomicInt(0)

    val completion = { result: Result<T> ->
        initRuntimeIfNeeded()
        if (isCancelled.value == 0) {
            futureResult.value = result.freeze()
        }
    }.freeze()

    val cancellable = startAsync(completion)
    try {
        waitAndDelayForCondition { futureResult.value != null }

        val result = futureResult.value
        futureResult.value = null
        if (result == null) throw IllegalStateException("Future should have a result; found null")

        return result.getOrThrow()
    } catch (e: CancellationException) {
        isCancelled.value = 1
        cancellable()
        throw e
    }
}
