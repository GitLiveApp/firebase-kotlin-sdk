package dev.gitlive.firebase.installations

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

expect val Firebase.installations: FirebaseInstallations

expect fun Firebase.installations(app: FirebaseApp): FirebaseInstallations

expect class FirebaseInstallations {
    suspend fun delete()
    suspend fun getId(): String
    suspend fun getToken(forceRefresh: Boolean): String
}

expect class FirebaseInstallationsException: FirebaseException

