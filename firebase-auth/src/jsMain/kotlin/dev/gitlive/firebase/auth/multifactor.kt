package dev.gitlive.firebase.auth

import dev.gitlive.firebase.firebase
import kotlinx.coroutines.await
import kotlin.js.Date

actual class MultiFactor(val js: firebase.multifactor.MultiFactorUser) {
    actual val enrolledFactors: List<MultiFactorInfo>
        get() = rethrow { js.enrolledFactors.map { MultiFactorInfo(it) } }
    actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) =
        rethrow { js.enroll(multiFactorAssertion.js, displayName).await() }
    actual suspend fun getSession(): MultiFactorSession =
        rethrow { MultiFactorSession(js.getSession().await()) }
    actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo) =
        rethrow { js.unenroll(multiFactorInfo.js).await() }
    actual suspend fun unenroll(factorUid: String) =
        rethrow { js.unenroll(factorUid).await() }
}

actual class MultiFactorInfo(val js: firebase.multifactor.MultiFactorInfo) {
    actual val displayName: String?
        get() = rethrow { js.displayName }
    actual val enrollmentTime: Double
        get() = rethrow { (Date(js.enrollmentTime).getTime() / 1000.0) }
    actual val factorId: String
        get() = rethrow { js.factorId }
    actual val uid: String
        get() = rethrow { js.uid }
}

actual class MultiFactorAssertion(val js: firebase.multifactor.MultiFactorAssertion) {
    actual val factorId: String
        get() = rethrow { js.factorId }
}

actual class MultiFactorSession(val js: firebase.multifactor.MultiFactorSession)

actual class MultiFactorResolver(val js: firebase.multifactor.MultifactorResolver) {
    actual val auth: FirebaseAuth = rethrow { FirebaseAuth(js.auth) }
    actual val hints: List<MultiFactorInfo> = rethrow { js.hints.map { MultiFactorInfo(it) } }
    actual val session: MultiFactorSession = rethrow { MultiFactorSession(js.session) }

    actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = rethrow { AuthResult(js.resolveSignIn(assertion.js).await()) }
}