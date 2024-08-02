package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.externals.*
import kotlinx.coroutines.await
import kotlin.js.Date
import dev.gitlive.firebase.auth.externals.UserInfo as JsUserInfo
import kotlin.js.json

public val FirebaseUser.js get() = js

public actual class FirebaseUser internal constructor(internal val js: User) {
    public actual val uid: String
        get() = rethrow { js.uid }
    public actual val displayName: String?
        get() = rethrow { js.displayName }
    public actual val email: String?
        get() = rethrow { js.email }
    public actual val phoneNumber: String?
        get() = rethrow { js.phoneNumber }
    public actual val photoURL: String?
        get() = rethrow { js.photoURL }
    public actual val isAnonymous: Boolean
        get() = rethrow { js.isAnonymous }
    public actual val isEmailVerified: Boolean
        get() = rethrow { js.emailVerified }
    public actual val metaData: UserMetaData?
        get() = rethrow { UserMetaData(js.metadata) }
    public actual val multiFactor: MultiFactor
        get() = rethrow { MultiFactor(multiFactor(js)) }
    public actual val providerData: List<UserInfo>
        get() = rethrow { js.providerData.map { UserInfo(it) } }
    public actual val providerId: String
        get() = rethrow { js.providerId }
    public actual suspend fun delete(): Unit = rethrow { js.delete().await() }
    public actual suspend fun reload(): Unit = rethrow { js.reload().await() }
    public actual suspend fun getIdToken(forceRefresh: Boolean): String? = rethrow { js.getIdToken(forceRefresh).await() }
    public actual suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult = rethrow { AuthTokenResult(getIdTokenResult(js, forceRefresh).await()) }
    public actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = rethrow { AuthResult(linkWithCredential(js, credential.js).await()) }
    public actual suspend fun reauthenticate(credential: AuthCredential): Unit = rethrow {
        reauthenticateWithCredential(js, credential.js).await()
        Unit
    }
    public actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = rethrow { AuthResult(reauthenticateWithCredential(js, credential.js).await()) }

    public actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?): Unit = rethrow { sendEmailVerification(js, actionCodeSettings?.toJson()).await() }
    public actual suspend fun unlink(provider: String): FirebaseUser? = rethrow { FirebaseUser(unlink(js, provider).await()) }
    public actual suspend fun updateEmail(email: String): Unit = rethrow { updateEmail(js, email).await() }
    public actual suspend fun updatePassword(password: String): Unit = rethrow { updatePassword(js, password).await() }
    public actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential): Unit = rethrow { updatePhoneNumber(js, credential.js).await() }
    public actual suspend fun updateProfile(displayName: String?, photoUrl: String?): Unit = rethrow {
        val request = listOf(
            "displayName" to displayName,
            "photoURL" to photoUrl,
        )
        updateProfile(js, json(*request.toTypedArray())).await()
    }
    public actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?): Unit = rethrow { verifyBeforeUpdateEmail(js, newEmail, actionCodeSettings?.toJson()).await() }
}

public val UserInfo.js get() = js

public actual class UserInfo(internal val js: JsUserInfo) {
    public actual val displayName: String?
        get() = rethrow { js.displayName }
    public actual val email: String?
        get() = rethrow { js.email }
    public actual val phoneNumber: String?
        get() = rethrow { js.phoneNumber }
    public actual val photoURL: String?
        get() = rethrow { js.photoURL }
    public actual val providerId: String
        get() = rethrow { js.providerId }
    public actual val uid: String
        get() = rethrow { js.uid }
}

public val UserMetaData.js get() = js

public actual class UserMetaData(internal val js: UserMetadata) {
    public actual val creationTime: Double?
        get() = rethrow { js.creationTime?.let { (Date(it).getTime() / 1000.0) } }
    public actual val lastSignInTime: Double?
        get() = rethrow { js.lastSignInTime?.let { (Date(it).getTime() / 1000.0) } }
}
