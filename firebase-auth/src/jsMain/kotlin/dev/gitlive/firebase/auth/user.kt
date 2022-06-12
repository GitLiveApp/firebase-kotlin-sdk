package dev.gitlive.firebase.auth

import dev.gitlive.firebase.externals.auth.*
import kotlinx.coroutines.await
import kotlin.js.Date
import dev.gitlive.firebase.externals.auth.UserInfo as JsUserInfo

actual class FirebaseUser internal constructor(val js: User) {
    actual val uid: String
        get() = rethrow { js.uid }
    actual val displayName: String?
        get() = rethrow { js.displayName }
    actual val email: String?
        get() = rethrow { js.email }
    actual val phoneNumber: String?
        get() = rethrow { js.phoneNumber }
    actual val photoURL: String?
        get() = rethrow { js.photoURL }
    actual val isAnonymous: Boolean
        get() = rethrow { js.isAnonymous }
    actual val isEmailVerified: Boolean
        get() = rethrow { js.emailVerified }
    actual val metaData: UserMetaData?
        get() = rethrow { UserMetaData(js.metadata) }
    actual val multiFactor: MultiFactor
        get() = rethrow { MultiFactor(multiFactor(js)) }
    actual val providerData: List<UserInfo>
        get() = rethrow { js.providerData.map { UserInfo(it) } }
    actual val providerId: String
        get() = rethrow { js.providerId }
    actual suspend fun delete() = rethrow { js.delete().await() }
    actual suspend fun reload() = rethrow { js.reload().await() }
    actual suspend fun getIdToken(forceRefresh: Boolean): String? = rethrow { js.getIdToken(forceRefresh).await() }
    actual suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult = rethrow { AuthTokenResult(getIdTokenResult(js, forceRefresh).await()) }
    actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = rethrow { AuthResult( linkWithCredential(js, credential.js).await()) }
    actual suspend fun reauthenticate(credential: AuthCredential) = rethrow {
        reauthenticateWithCredential(js, credential.js).await()
        Unit
    }
    actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = rethrow { AuthResult(reauthenticateWithCredential(js, credential.js).await()) }

    actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?) = rethrow { sendEmailVerification(js, actionCodeSettings?.toJson()).await() }
    actual suspend fun unlink(provider: String): FirebaseUser? = rethrow { FirebaseUser(unlink(js, provider).await()) }
    actual suspend fun updateEmail(email: String) = rethrow { updateEmail(js, email).await() }
    actual suspend fun updatePassword(password: String) = rethrow { updatePassword(js, password).await() }
    actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential) = rethrow { updatePhoneNumber(js, credential.js).await() }
    actual suspend fun updateProfile(displayName: String?, photoUrl: String?) = rethrow {
        val request = object : ProfileUpdateRequest {
            override val displayName: String? = displayName
            override val photoURL: String? = photoUrl
        }
        updateProfile(js, request).await()
    }
    actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?) = rethrow { verifyBeforeUpdateEmail(js, newEmail, actionCodeSettings?.toJson()).await() }
}

actual class UserInfo(val js: JsUserInfo) {
    actual val displayName: String?
        get() = rethrow { js.displayName }
        actual val email: String?
        get() = rethrow { js.email }
        actual val phoneNumber: String?
        get() = rethrow { js.phoneNumber }
    actual val photoURL: String?
        get() = rethrow { js.photoURL }
    actual val providerId: String
        get() = rethrow { js.providerId }
    actual val uid: String
        get() = rethrow { js.uid }
}

actual class UserMetaData(val js: UserMetadata) {
    actual val creationTime: Double?
        get() = rethrow {js.creationTime?.let { (Date(it).getTime() / 1000.0) } }
    actual val lastSignInTime: Double?
        get() = rethrow {js.lastSignInTime?.let { (Date(it).getTime() / 1000.0) } }
}
