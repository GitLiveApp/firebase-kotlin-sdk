package dev.gitlive.firebase.auth

import dev.gitlive.firebase.firebase
import kotlinx.coroutines.await
import kotlin.js.Date

class MultiFactor(val js: firebase.multifactor.MultiFactorUser) {
    val enrolledFactors: List<MultiFactorInfo>
        get() = rethrow { js.enrolledFactors.map { MultiFactorInfo(it) } }
    suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?) =
        rethrow { js.enroll(multiFactorAssertion.js, displayName).await() }
    suspend fun getSession(): MultiFactorSession =
        rethrow { MultiFactorSession(js.getSession().await()) }
    suspend fun unenroll(multiFactorInfo: MultiFactorInfo) =
        rethrow { js.unenroll(multiFactorInfo.js).await() }
    suspend fun unenroll(factorUid: String) =
        rethrow { js.unenroll(factorUid).await() }
}

class MultiFactorInfo(val js: firebase.multifactor.MultiFactorInfo) {
    val displayName: String?
        get() = rethrow { js.displayName }
    val enrollmentTime: Double
        get() = rethrow { (Date(js.enrollmentTime).getTime() / 1000.0) }
    val factorId: String
        get() = rethrow { js.factorId }
    val uid: String
        get() = rethrow { js.uid }
}

class MultiFactorAssertion(val js: firebase.multifactor.MultiFactorAssertion) {
    val factorId: String
        get() = rethrow { js.factorId }
}

class MultiFactorSession(val js: firebase.multifactor.MultiFactorSession)

class MultiFactorResolver(val js: firebase.multifactor.MultifactorResolver) {
    val auth: FirebaseAuth = rethrow { FirebaseAuth(js.auth) }
    val hints: List<MultiFactorInfo> = rethrow { js.hints.map { MultiFactorInfo(it) } }
    val session: MultiFactorSession = rethrow { MultiFactorSession(js.session) }

    suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = rethrow { AuthResult(js.resolveSignIn(assertion.js).await()) }
}