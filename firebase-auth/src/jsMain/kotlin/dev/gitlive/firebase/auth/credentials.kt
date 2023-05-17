package dev.gitlive.firebase.auth

import dev.gitlive.firebase.firebase
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

actual open class AuthCredential(val js: firebase.auth.AuthCredential) {
    actual val providerId: String
        get() = js.providerId
}

actual class PhoneAuthCredential(js: firebase.auth.AuthCredential) : AuthCredential(js)
actual class OAuthCredential(js: firebase.auth.AuthCredential) : AuthCredential(js)

actual object EmailAuthProvider {
    actual fun credential(email: String, password: String): AuthCredential =
        AuthCredential(firebase.auth.EmailAuthProvider.credential(email, password))

    actual fun credentialWithLink(
        email: String,
        emailLink: String
    ): AuthCredential = AuthCredential(firebase.auth.EmailAuthProvider.credentialWithLink(email, emailLink))
}

actual object FacebookAuthProvider {
    actual fun credential(accessToken: String): AuthCredential =
        AuthCredential(firebase.auth.FacebookAuthProvider.credential(accessToken))
}

actual object GithubAuthProvider {
    actual fun credential(token: String): AuthCredential =
        AuthCredential(firebase.auth.GithubAuthProvider.credential(token))
}

actual object GoogleAuthProvider {
    actual fun credential(idToken: String?, accessToken: String?): AuthCredential {
        require(idToken != null || accessToken != null) {
            "Both parameters are optional but at least one must be present."
        }
        return AuthCredential(firebase.auth.GoogleAuthProvider.credential(idToken, accessToken))
    }
}

actual class OAuthProvider(val js: firebase.auth.OAuthProvider) {

    actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth
    ) : this(firebase.auth.OAuthProvider(provider))  {
        rethrow {
            scopes.forEach { js.addScope(it) }
            js.setCustomParameters(customParameters)
        }
    }
    actual companion object {
        actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential = rethrow {
            firebase.auth.OAuthProvider(providerId)
                .credential(
                    json(
                        "accessToken" to (accessToken ?: undefined),
                        "idToken" to (idToken ?: undefined),
                        "rawNonce" to (rawNonce ?: undefined)
                    ),
                    accessToken ?: undefined
                )
                .let { OAuthCredential(it) }
        }
    }
}

actual class PhoneAuthProvider(val js: firebase.auth.PhoneAuthProvider) {

    actual constructor(auth: FirebaseAuth) : this(firebase.auth.PhoneAuthProvider(auth.js))

    actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(firebase.auth.PhoneAuthProvider.credential(verificationId, smsCode))
    actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential = rethrow {
        val verificationId = js.verifyPhoneNumber(phoneNumber, verificationProvider.verifier).await()
        val verificationCode = verificationProvider.getVerificationCode(verificationId)
        credential(verificationId, verificationCode)
    }
}

actual interface PhoneVerificationProvider {
    val verifier: firebase.auth.ApplicationVerifier
    suspend fun getVerificationCode(verificationId: String): String
}

actual object TwitterAuthProvider {
    actual fun credential(token: String, secret: String): AuthCredential = AuthCredential(firebase.auth.TwitterAuthProvider.credential(token, secret))
}
