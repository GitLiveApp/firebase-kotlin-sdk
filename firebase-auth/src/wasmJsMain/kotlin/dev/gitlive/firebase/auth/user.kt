package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.externals.*
import dev.gitlive.firebase.externals.jsObject
import dev.gitlive.firebase.externals.jsSet
import dev.gitlive.firebase.externals.parseDateStringToMillis
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.externals.toList
import dev.gitlive.firebase.externals.awaitUnit
import dev.gitlive.firebase.externals.awaitValue
import dev.gitlive.firebase.auth.externals.UserInfo as JsUserInfo

public val FirebaseUser.js: User get() = js

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
        get() = rethrow { js.providerData.toList().map { UserInfo(it) } }
    public actual val providerId: String
        get() = rethrow { js.providerId }
    public actual suspend fun delete(): Unit = rethrow { js.delete().awaitUnit() }
    public actual suspend fun reload(): Unit = rethrow { js.reload().awaitUnit() }
    public actual suspend fun getIdToken(forceRefresh: Boolean): String? = rethrow { js.getIdToken(forceRefresh).awaitValue().toKotlinString() }
    public actual suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult = rethrow { AuthTokenResult(getIdTokenResult(js, forceRefresh).awaitValue()) }
    public actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = rethrow { AuthResult(linkWithCredential(js, credential.js).awaitValue()) }
    public actual suspend fun reauthenticate(credential: AuthCredential): Unit = rethrow {
        reauthenticateWithCredential(js, credential.js).awaitValue()
        Unit
    }
    public actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = rethrow { AuthResult(reauthenticateWithCredential(js, credential.js).awaitValue()) }

    public actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?): Unit = rethrow { sendEmailVerification(js, actionCodeSettings?.toJson()).awaitUnit() }
    public actual suspend fun unlink(provider: String): FirebaseUser? = rethrow { FirebaseUser(unlink(js, provider).awaitValue()) }
    public actual suspend fun updateEmail(email: String): Unit = rethrow { updateEmail(js, email).awaitUnit() }
    public actual suspend fun updatePassword(password: String): Unit = rethrow { updatePassword(js, password).awaitUnit() }
    public actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential): Unit = rethrow { updatePhoneNumber(js, credential.js).awaitUnit() }
    public actual suspend fun updateProfile(displayName: String?, photoUrl: String?): Unit = rethrow {
        val request = jsObject()
        jsSet(request, "displayName", displayName.toJs())
        jsSet(request, "photoURL", photoUrl.toJs())
        updateProfile(js, request).awaitUnit()
    }
    public actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?): Unit = rethrow { verifyBeforeUpdateEmail(js, newEmail, actionCodeSettings?.toJson()).awaitUnit() }
}

public val UserInfo.js: JsUserInfo get() = js

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

public val UserMetaData.js: UserMetadata get() = js

public actual class UserMetaData(internal val js: UserMetadata) {
    public actual val creationTime: Double?
        get() = rethrow { js.creationTime?.let { parseDateStringToMillis(it) / 1000.0 } }
    public actual val lastSignInTime: Double?
        get() = rethrow { js.lastSignInTime?.let { parseDateStringToMillis(it) / 1000.0 } }
}
