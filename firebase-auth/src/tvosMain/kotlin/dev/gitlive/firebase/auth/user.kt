package dev.gitlive.firebase.auth

internal actual fun FirebaseUser.getMultiFactor(): MultiFactor = MultiFactor()

internal actual suspend fun FirebaseUser.updatePhoneNumberInternal(credential: PhoneAuthCredential): Unit = Unit
