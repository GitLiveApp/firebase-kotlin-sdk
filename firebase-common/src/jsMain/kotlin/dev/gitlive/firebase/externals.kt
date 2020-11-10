/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("firebase/functions")
external object functions

@JsModule("firebase/auth")
external object auth

@JsModule("firebase/database")
external object database

@JsModule("firebase/firestore")
external object firestore

typealias SnapshotCallback = (data: firebase.database.DataSnapshot, b: String?) -> Unit

@JsModule("firebase/app")
external object firebase {

    open class App {
        val name: String
        val options: Options
        fun functions(region: String? = definedExternally): functions.Functions
        fun database(url: String? = definedExternally): database.Database
        fun firestore(): firestore.Firestore
    }

    interface Options {
        val applicationId: String
        val apiKey: String
        val databaseUrl: String?
        val gaTrackingId: String?
        val storageBucket: String?
        val projectId: String?
    }

    val apps : Array<App>
    fun app(name: String? = definedExternally): App
    fun initializeApp(options: Any, name: String? = definedExternally) : App

    interface FirebaseError {
        var code: String
        var message: String
        var name: String
    }

    fun auth(app: App? = definedExternally): auth.Auth

    object auth {
        open class Auth {
            val currentUser: user.User?
            var languageCode: String?

            fun applyActionCode(code: String): Promise<Unit>
            fun checkActionCode(code: String): Promise<ActionCodeInfo>
            fun confirmPasswordReset(code: String, newPassword: String): Promise<Unit>
            fun createUserWithEmailAndPassword(email: String, password: String): Promise<AuthResult>
            fun fetchSignInMethodsForEmail(email: String): Promise<Array<String>>
            fun sendPasswordResetEmail(email: String, actionCodeSettings: Any?): Promise<Unit>
            fun sendSignInLinkToEmail(email: String, actionCodeSettings: Any?): Promise<Unit>
            fun signInWithEmailAndPassword(email: String, password: String): Promise<AuthResult>
            fun signInWithCustomToken(token: String): Promise<AuthResult>
            fun signInAnonymously(): Promise<AuthResult>
            fun signInWithCredential(authCredential: AuthCredential): Promise<AuthResult>
            fun signInWithPopup(provider: AuthProvider): Promise<AuthResult>
            fun signInWithRedirect(provider: AuthProvider): Promise<Unit>
            fun getRedirectResult(): Promise<AuthResult>
            fun signOut(): Promise<Unit>
            fun updateCurrentUser(user: user.User?): Promise<Unit>
            fun verifyPasswordResetCode(code: String): Promise<String>

            fun onAuthStateChanged(nextOrObserver: (user.User?) -> Unit): () -> Unit
            fun onIdTokenChanged(nextOrObserver: (user.User?) -> Unit): () -> Unit
        }
        abstract class AuthResult {
            val credential: AuthCredential?
            val operationType: String?
            val user: user.User?
        }

        abstract class AuthCredential {
            val providerId: String
            val signInMethod: String
        }

        abstract class ActionCodeInfo {
            val operation: String
            val data: ActionCodeData
        }

        abstract class ActionCodeData {
            val email: String?
            val multiFactorInfo: multifactor.MultiFactorInfo?
            val previousEmail: String?
        }

        interface ActionCodeSettings {
            val android: AndroidActionCodeSettings?
            val dynamicLinkDomain: String?
            val handleCodeInApp: Boolean?
            val iOS: iOSActionCodeSettings?
            val url: String
        }

        interface AndroidActionCodeSettings {
            val installApp: Boolean?
            val minimumVersion: String?
            val packageName: String
        }

        interface iOSActionCodeSettings {
            val bundleId: String?
        }

        interface AuthProvider

        class EmailAuthProvider : AuthProvider {
            companion object {
                fun credential(email :  String, password : String): AuthCredential
            }
        }

        class FacebookAuthProvider : AuthProvider {
            companion object {
                fun credential(token: String): AuthCredential
            }
        }

        class GithubAuthProvider : AuthProvider {
            companion object {
                fun credential(token: String): AuthCredential
            }
        }

        class GoogleAuthProvider : AuthProvider{
            companion object {
                fun credential(idToken: String?, accessToken: String?): AuthCredential
            }
        }

        open class OAuthProvider(providerId: String) : AuthProvider {
            val providerId: String
            fun credential(optionsOrIdToken: Any?, accessToken: String?): AuthCredential

            fun addScope(scope: String)
            fun setCustomParameters(customOAuthParameters: Map<String, String>)
        }

        interface OAuthCredentialOptions {
            val accessToken: String?
            val idToken: String?
            val rawNonce: String?
        }

        class PhoneAuthProvider(auth: Auth?) : AuthProvider {
            companion object {
                fun credential(verificationId: String, verificationCode: String): AuthCredential
            }
            fun verifyPhoneNumber(phoneInfoOptions: String, applicationVerifier: ApplicationVerifier): Promise<String>
        }

        abstract class ApplicationVerifier {
            val type: String
            fun verify(): Promise<String>
        }

        class TwitterAuthProvider : AuthProvider {
            companion object {
                fun credential (token: String, secret: String): AuthCredential
            }
        }
    }

    fun User(a: Any,b: Any,c: Any): user.User

    object user {
        abstract class User {
            val uid: String
            val displayName: String?
            val email: String?
            val emailVerified: Boolean
            val metadata: UserMetadata
            val multiFactor: multifactor.MultiFactorUser
            val phoneNumber: String?
            val photoURL: String?
            val providerData: Array<UserInfo>
            val providerId: String
            val refreshToken: String
            val tenantId: String?
            val isAnonymous: Boolean

            fun delete(): Promise<Unit>
            fun getIdToken(forceRefresh: Boolean?): Promise<String>
            fun linkWithCredential(credential: auth.AuthCredential): Promise<auth.AuthResult>
            fun reauthenticateWithCredential(credential: auth.AuthCredential): Promise<auth.AuthResult>
            fun reload(): Promise<Unit>
            fun sendEmailVerification(actionCodeSettings: Any?): Promise<Unit>
            fun unlink(providerId: String): Promise<User>
            fun updateEmail(newEmail: String): Promise<Unit>
            fun updatePassword(newPassword: String): Promise<Unit>
            fun updatePhoneNumber(phoneCredential: auth.AuthCredential): Promise<Unit>
            fun updateProfile(profile: ProfileUpdateRequest): Promise<Unit>
            fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: Any?): Promise<Unit>
        }

        abstract class UserMetadata {
            val creationTime: String?
            val lastSignInTime: String?
        }

        abstract class UserInfo {
            val displayName: String?
            val email: String?
            val phoneNumber: String?
            val photoURL: String?
            val providerId: String
            val uid: String
        }

        interface ProfileUpdateRequest {
            val displayName: String?
            val photoURL: String?
        }

    }

    object multifactor {
        abstract class MultiFactorUser {
            val enrolledFactors: Array<MultiFactorInfo>

            fun enroll(assertion: MultiFactorAssertion, displayName: String?): Promise<Unit>
            fun getSession(): Promise<MultiFactorSession>
            fun unenroll(option: MultiFactorInfo): Promise<Unit>
            fun unenroll(option: String): Promise<Unit>
        }

        abstract class MultiFactorInfo {
            val displayName: String?
            val enrollmentTime: String
            val factorId: String
            val uid: String
        }

        abstract class MultiFactorAssertion {
            val factorId: String
        }

        interface MultiFactorSession

        abstract class MultifactorResolver {

            val auth: auth.Auth
            val hints: Array<MultiFactorInfo>
            val session: MultiFactorSession

            fun resolveSignIn(assertion: MultiFactorAssertion): Promise<auth.AuthResult>
        }
    }

    fun functions(app: App? = definedExternally): functions.Functions

    object functions {
        class Functions {
            fun httpsCallable(name: String, options: Json?): HttpsCallable
            fun useFunctionsEmulator(origin: String)
        }
        interface HttpsCallableResult {
            val data: Any?
        }
        interface HttpsCallable {
        }

    }

    fun database(app: App? = definedExternally): database.Database

    object database {
        fun enableLogging(logger: Boolean?, persistent: Boolean? = definedExternally)

        open class Database {
            fun ref(path: String? = definedExternally): Reference
        }
        open class ThenableReference : Reference


        open class Query {
            fun on(eventType: String?, callback: SnapshotCallback, cancelCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): SnapshotCallback
            fun off(eventType: String?, callback: SnapshotCallback?, context: Any? = definedExternally)
            fun once(eventType: String, callback: SnapshotCallback, failureCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): SnapshotCallback
            fun orderByChild(path: String): Query
            fun orderByKey(): Query
            fun startAt(value: Any, key: String? = definedExternally): Query
        }

        open class Reference: Query {
            val key: String?
            fun child(path: String): Reference
            fun remove(): Promise<Unit>
            fun onDisconnect(): OnDisconnect
            fun update(value: Any?): Promise<Unit>
            fun set(value: Any?): Promise<Unit>
            fun push(): ThenableReference
        }

        open class DataSnapshot {
            val key: String?
            fun `val`(): Any
            fun exists(): Boolean
            fun forEach(action: (a: DataSnapshot) -> Boolean): Boolean
            fun numChildren(): Int
            fun child(path: String): DataSnapshot
        }

        open class OnDisconnect {
            fun update(value: Any?): Promise<Unit>
            fun remove(): Promise<Unit>
            fun cancel(): Promise<Unit>
            fun set(value: Any?): Promise<Unit>
        }

        object ServerValue {
            val TIMESTAMP: Any
        }
    }

    fun firestore(): firestore.Firestore

    object firestore {
        fun setLogLevel(level: String)

        open class PersistenceSettings {
            var experimentalTabSynchronization: Boolean
        }

        open class Firestore {
            fun <T> runTransaction(func: (transaction: Transaction) -> Promise<T>): Promise<T>
            fun batch(): WriteBatch
            fun collection(collectionPath: String): CollectionReference
            fun collectionGroup(collectionId: String): Query
            fun doc(documentPath: String): DocumentReference
            fun settings(settings: Json)
            fun enablePersistence(): Promise<Unit>
            fun clearPersistence(): Promise<Unit>
        }

        open class FieldPath constructor(vararg fieldNames: String)

        open class Query {
            fun get(options: Any? = definedExternally): Promise<QuerySnapshot>
            fun where(field: Any, opStr: String, value: Any?): Query
            fun order(field: Any, descending: Boolean): Query
            fun onSnapshot(next: (snapshot: QuerySnapshot) -> Unit, error: (error: Error) -> Unit): () -> Unit
            fun limit(limit: Double): Query
        }

        open class CollectionReference : Query {
            val path: String
            fun add(data: Any): Promise<DocumentReference>
        }

        open class QuerySnapshot {
            val docs: Array<DocumentSnapshot>
            val empty: Boolean
        }

        open class DocumentSnapshot {
            val id: String
            val ref: DocumentReference
            val exists: Boolean
            fun data(options: Any? = definedExternally): Any?
            fun get(fieldPath: Any, options: Any? = definedExternally): Any?
        }

        open class DocumentReference {
            val id: String
            val path: String

            fun get(options: Any? = definedExternally): Promise<DocumentSnapshot>
            fun set(data: Any, options: Any? = definedExternally): Promise<Unit>
            fun update(data: Any): Promise<Unit>
            fun update(field: Any, value: Any?, vararg moreFieldsAndValues: Any?): Promise<Unit>
            fun delete(): Promise<Unit>
            fun onSnapshot(next: (snapshot: DocumentSnapshot) -> Unit, error: (error: Error) -> Unit): ()->Unit
        }

        open class WriteBatch {
            fun commit(): Promise<Unit>
            fun delete(documentReference: DocumentReference): WriteBatch
            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): WriteBatch
            fun update(documentReference: DocumentReference, data: Any): WriteBatch
            fun update(documentReference: DocumentReference, field: Any, value: Any?, vararg moreFieldsAndValues: Any?): WriteBatch
        }

        open class Transaction {
            fun get(documentReference: DocumentReference): Promise<DocumentSnapshot>
            fun set(documentReference: DocumentReference, data: Any, options: Any? = definedExternally): Transaction
            fun update(documentReference: DocumentReference, data: Any): Transaction
            fun update(documentReference: DocumentReference, field: Any, value: Any?, vararg moreFieldsAndValues: Any?): Transaction
            fun delete(documentReference: DocumentReference): Transaction
        }

        open class Timestamp(seconds: Long, nanoseconds: Int) {
            companion object {
                fun now(): Timestamp
            }
            val seconds: Long
            val nanoseconds: Int
        }

        abstract class FieldValue {
            companion object {
                fun delete(): FieldValue
                fun arrayRemove(vararg elements: Any): FieldValue
                fun arrayUnion(vararg elements: Any): FieldValue
                fun serverTimestamp(): FieldValue
            }
        }
    }
}

operator fun firebase.functions.HttpsCallable.invoke() = asDynamic()() as Promise<firebase.functions.HttpsCallableResult>
operator fun firebase.functions.HttpsCallable.invoke(data: Any?) = asDynamic()(data) as Promise<firebase.functions.HttpsCallableResult>

