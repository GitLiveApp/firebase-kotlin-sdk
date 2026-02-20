package dev.gitlive.firebase.auth

public actual class MultiFactor {
    public actual val enrolledFactors: List<MultiFactorInfo> = emptyList()
    public actual suspend fun enroll(
        multiFactorAssertion: MultiFactorAssertion,
        displayName: String?,
    ): Unit = throw TvOsMultifactorNotSupportedException()

    public actual suspend fun getSession(): MultiFactorSession = throw TvOsMultifactorNotSupportedException()
    public actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo): Unit = Unit
    public actual suspend fun unenroll(factorUid: String): Unit = Unit
}

public actual class MultiFactorInfo private constructor(
    public actual val displayName: String?,
    public actual val enrollmentTime: Double,
    public actual val factorId: String,
    public actual val uid: String,
)

public actual class MultiFactorAssertion private constructor(public actual val factorId: String)

public actual class MultiFactorSession private constructor()

public actual class MultiFactorResolver(
    public actual val auth: FirebaseAuth,
    public actual val hints: List<MultiFactorInfo>,
    public actual val session: MultiFactorSession,
) {

    public actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = throw TvOsMultifactorNotSupportedException()
}

public class TvOsMultifactorNotSupportedException : UnsupportedOperationException("Multifactor authentication is not supported on tvOS")
