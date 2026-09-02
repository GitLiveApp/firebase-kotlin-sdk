package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.externals.MultiFactorUser
import dev.gitlive.firebase.externals.parseDateStringToMillis
import dev.gitlive.firebase.externals.toList
import dev.gitlive.firebase.externals.awaitUnit
import dev.gitlive.firebase.externals.awaitValue
import dev.gitlive.firebase.auth.externals.MultiFactorAssertion as JsMultiFactorAssertion
import dev.gitlive.firebase.auth.externals.MultiFactorInfo as JsMultiFactorInfo
import dev.gitlive.firebase.auth.externals.MultiFactorResolver as JsMultiFactorResolver
import dev.gitlive.firebase.auth.externals.MultiFactorSession as JsMultiFactorSession

public val MultiFactor.js: MultiFactorUser get() = js

public actual class MultiFactor(internal val js: MultiFactorUser) {
    public actual val enrolledFactors: List<MultiFactorInfo>
        get() = rethrow { js.enrolledFactors.toList().map { MultiFactorInfo(it) } }
    public actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?): Unit = rethrow { js.enroll(multiFactorAssertion.js, displayName).awaitUnit() }
    public actual suspend fun getSession(): MultiFactorSession = rethrow { MultiFactorSession(js.getSession().awaitValue()) }
    public actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo): Unit = rethrow { js.unenroll(multiFactorInfo.js).awaitUnit() }
    public actual suspend fun unenroll(factorUid: String): Unit = rethrow { js.unenroll(factorUid).awaitUnit() }
}

public val MultiFactorInfo.js: JsMultiFactorInfo get() = js

public actual class MultiFactorInfo(internal val js: JsMultiFactorInfo) {
    public actual val displayName: String?
        get() = rethrow { js.displayName }
    public actual val enrollmentTime: Double
        get() = rethrow { parseDateStringToMillis(js.enrollmentTime) / 1000.0 }
    public actual val factorId: String
        get() = rethrow { js.factorId }
    public actual val uid: String
        get() = rethrow { js.uid }
}

public val MultiFactorAssertion.js: JsMultiFactorAssertion get() = js

public actual class MultiFactorAssertion(internal val js: JsMultiFactorAssertion) {
    public actual val factorId: String
        get() = rethrow { js.factorId }
}

public val MultiFactorSession.js: JsMultiFactorSession get() = js

public actual class MultiFactorSession(internal val js: JsMultiFactorSession)

public val MultiFactorResolver.js: JsMultiFactorResolver get() = js

public actual class MultiFactorResolver(internal val js: JsMultiFactorResolver) {
    public actual val auth: FirebaseAuth = rethrow { FirebaseAuth(js.auth) }
    public actual val hints: List<MultiFactorInfo> = rethrow { js.hints.toList().map { MultiFactorInfo(it) } }
    public actual val session: MultiFactorSession = rethrow { MultiFactorSession(js.session) }

    public actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = rethrow { AuthResult(js.resolveSignIn(assertion.js).awaitValue()) }
}
