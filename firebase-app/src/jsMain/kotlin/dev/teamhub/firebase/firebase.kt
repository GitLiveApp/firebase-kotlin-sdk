package dev.teamhub.firebase

import kotlin.js.Json
import kotlin.js.json
import kotlin.reflect.KClass

actual typealias FirebaseApp = firebase.App

actual fun initializeFirebaseApp(context: Any, options: FirebaseOptions) =  firebase.initializeApp(
        json(
            "apiKey" to options.apiKey,
            "applicationId" to options.applicationId,
            "databaseURL" to options.databaseUrl,
            "storageBucket" to options.storageBucket,
            "projectId" to options.projectId,
            "googleAppId" to options.googleAppId
        )
)

actual data class FirebaseOptions constructor(
         val apiKey: String? = undefined,
         val applicationId: String? = undefined,
         val databaseUrl: String? = undefined,
         val storageBucket: String? = undefined,
         val projectId: String? = undefined,
         val googleAppId: String? = undefined
)

actual class FirebaseOptionsBuilder actual constructor() {
    var options = FirebaseOptions()
}

actual open class FirebaseException(code: String?, message: String?) : Exception("$code: $message")

actual fun getFirebaseApps(context: Any) = firebase.apps.toList()

actual open class FirebaseNetworkException(code: String?, message: String?) : FirebaseException(code, message)
actual open class FirebaseTooManyRequestsException(code: String?, message: String?) : FirebaseException(code, message)
actual open class FirebaseApiNotAvailableException(code: String?, message: String?) : FirebaseException(code, message)

actual fun FirebaseOptionsBuilder.setGoogleAppId(googleAppId: String) = options.copy(googleAppId = googleAppId).let { options = it }.let { this }

actual fun FirebaseOptionsBuilder.setApiKey(apiKey: String) = options.copy(apiKey = apiKey).let { options = it }.let { this }

actual fun FirebaseOptionsBuilder.setApplicationId(applicationId: String) = options.copy(applicationId = applicationId).let { options = it }.let { this }

actual fun FirebaseOptionsBuilder.setDatabaseUrl(databaseUrl: String?) = options.copy(databaseUrl = databaseUrl).let { options = it }.let { this }

actual fun FirebaseOptionsBuilder.setStorageBucket(storageBucket: String?) = options.copy(storageBucket = storageBucket).let { options = it }.let { this }

actual fun FirebaseOptionsBuilder.setProjectId(projectId: String?) = options.copy(projectId = projectId).let { options = it }.let { this }

actual fun FirebaseOptionsBuilder.build() = options

