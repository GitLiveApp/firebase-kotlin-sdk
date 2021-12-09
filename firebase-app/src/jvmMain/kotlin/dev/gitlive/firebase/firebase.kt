/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseException
import java.io.File
import java.io.FileInputStream

actual typealias FirebaseException = FirebaseException

actual typealias FirebaseNetworkException = NetworkException

actual typealias FirebaseTooManyRequestsException = TooManyRequestsException

actual typealias FirebaseApiNotAvailableException = ApiNotAvailableException

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(com.google.firebase.FirebaseApp.getInstance())

actual fun Firebase.app(name: String): FirebaseApp = FirebaseApp(com.google.firebase.FirebaseApp.getInstance(name))

actual fun Firebase.initialize(context: Any?): FirebaseApp? = com.google.firebase.FirebaseApp.initializeApp()?.let { FirebaseApp(it) }

actual fun Firebase.initialize(context: Any?, options: CommonFirebaseOptions, name: String): FirebaseApp =
    FirebaseApp(com.google.firebase.FirebaseApp.initializeApp((options as? AdminFirebaseOptions)?.toJvm(), name))

actual fun Firebase.initialize(context: Any?, options: CommonFirebaseOptions) =
    FirebaseApp(com.google.firebase.FirebaseApp.initializeApp((options as? AdminFirebaseOptions)?.toJvm()))

actual class FirebaseApp internal constructor(val jvm: com.google.firebase.FirebaseApp) {
    actual val name: String
        get() = jvm.name
    actual val options: CommonFirebaseOptions
        get() = jvm.options.run { options }
}

actual fun Firebase.apps(context: Any?) = com.google.firebase.FirebaseApp.getApps()
    .map { FirebaseApp(it) }

fun AdminFirebaseOptions.toJvm(): com.google.firebase.FirebaseOptions {
    val serviceFile = if(File(credentialFile).exists()) FileInputStream(credentialFile) else javaClass.classLoader!!.getResourceAsStream(credentialFile)

    var builder =  com.google.firebase.FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceFile))
    builder = projectId?.let { builder.setProjectId(it) } ?: builder
    builder = connectTimeout?.let { builder.setConnectTimeout(it) } ?: builder
    builder = readTimeout?.let { builder.setReadTimeout(it) } ?: builder
    builder = databaseUrl?.let { builder.setDatabaseUrl(it) } ?: builder
    return builder.build()
}