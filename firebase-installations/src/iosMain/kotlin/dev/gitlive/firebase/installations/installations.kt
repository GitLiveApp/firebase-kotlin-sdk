package dev.gitlive.firebase.installations

import cocoapods.FirebaseInstallations.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.*

public actual val Firebase.installations: FirebaseInstallations
    get() = FirebaseInstallations(FIRInstallations.installations())

public actual fun Firebase.installations(app: FirebaseApp): FirebaseInstallations = FirebaseInstallations(
    FIRInstallations.installationsWithApp(app.ios as objcnames.classes.FIRApp),
)

public actual class FirebaseInstallations internal constructor(public val ios: FIRInstallations) {

    public actual suspend fun delete(): Unit = ios.await { deleteWithCompletion(completion = it) }

    public actual suspend fun getId(): String = ios.awaitResult { installationIDWithCompletion(completion = it) }

    public actual suspend fun getToken(forceRefresh: Boolean): String {
        val result: FIRInstallationsAuthTokenResult = ios.awaitResult { authTokenForcingRefresh(forceRefresh = forceRefresh, completion = it) }

        return result.authToken
    }
}

public actual class FirebaseInstallationsException(message: String) : FirebaseException(message)

internal suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(FirebaseInstallationsException(error.toString()))
        }
    }
    job.await()
}

internal suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(FirebaseInstallationsException(error.toString()))
        }
    }
    return job.await() as R
}
