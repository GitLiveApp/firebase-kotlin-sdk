package dev.gitlive.firebase.installations

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await

actual val Firebase.installations
    get() = FirebaseInstallations(com.google.firebase.installations.FirebaseInstallations.getInstance())

actual fun Firebase.installations(app: FirebaseApp)
        = FirebaseInstallations(com.google.firebase.installations.FirebaseInstallations.getInstance(app.android))

actual class FirebaseInstallations internal constructor(val android: com.google.firebase.installations.FirebaseInstallations) {

    actual suspend fun delete() = android.delete().await().let {  }

    actual suspend fun getId(): String = android.id.await()

    actual suspend fun getToken(forceRefresh: Boolean): String =
        android.getToken(forceRefresh).await().token
}

actual typealias FirebaseInstallationsException = com.google.firebase.installations.FirebaseInstallationsException
