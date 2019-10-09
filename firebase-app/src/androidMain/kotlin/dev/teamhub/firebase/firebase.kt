package dev.teamhub.firebase

import android.content.Context

actual typealias FirebaseException = com.google.firebase.FirebaseException

actual typealias FirebaseNetworkException = com.google.firebase.FirebaseNetworkException

actual typealias FirebaseTooManyRequestsException = com.google.firebase.FirebaseTooManyRequestsException

actual typealias FirebaseApiNotAvailableException = com.google.firebase.FirebaseApiNotAvailableException

actual fun initializeFirebaseApp(context: Any, options: FirebaseOptions) =
    FirebaseApp.initializeApp(context as Context, options)

actual typealias FirebaseApp = com.google.firebase.FirebaseApp

actual typealias FirebaseOptions = com.google.firebase.FirebaseOptions

actual typealias FirebaseOptionsBuilder = com.google.firebase.FirebaseOptions.Builder

actual fun getFirebaseApps(context: Any) = FirebaseApp.getApps(context as Context)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseOptionsBuilder.setApiKey(apiKey: String): FirebaseOptionsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseOptionsBuilder.setApplicationId(applicationId: String): FirebaseOptionsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseOptionsBuilder.setDatabaseUrl(databaseUrl: String?): FirebaseOptionsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseOptionsBuilder.setStorageBucket(storageBucket: String?): FirebaseOptionsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseOptionsBuilder.setProjectId(projectId: String?): FirebaseOptionsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseOptionsBuilder.build(): FirebaseOptions {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun FirebaseOptionsBuilder.setGoogleAppId(googleAppId: String): FirebaseOptionsBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
