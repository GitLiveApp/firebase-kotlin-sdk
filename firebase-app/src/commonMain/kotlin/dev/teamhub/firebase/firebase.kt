@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")
package dev.teamhub.firebase

expect fun initializeFirebaseApp(context: Any, options: FirebaseOptions): FirebaseApp

expect class FirebaseApp

expect fun getFirebaseApps(context: Any): List<FirebaseApp>

expect class FirebaseOptions

expect class FirebaseOptionsBuilder()

expect fun FirebaseOptionsBuilder.setGoogleAppId(googleAppId: String): FirebaseOptionsBuilder
expect fun FirebaseOptionsBuilder.setApiKey(apiKey: String): FirebaseOptionsBuilder
expect fun FirebaseOptionsBuilder.setApplicationId(applicationId: String): FirebaseOptionsBuilder
expect fun FirebaseOptionsBuilder.setDatabaseUrl(databaseUrl: String?): FirebaseOptionsBuilder
expect fun FirebaseOptionsBuilder.setStorageBucket(storageBucket: String?): FirebaseOptionsBuilder
expect fun FirebaseOptionsBuilder.setProjectId(projectId: String?): FirebaseOptionsBuilder
expect fun FirebaseOptionsBuilder.build(): FirebaseOptions

expect open class FirebaseException : Exception

expect class FirebaseNetworkException : FirebaseException

expect open class FirebaseTooManyRequestsException : FirebaseException

expect open class FirebaseApiNotAvailableException : FirebaseException