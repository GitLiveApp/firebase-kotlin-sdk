package dev.gitlive.firebase.auth

public actual class PhoneAuthProvider() {

    public actual constructor(auth: FirebaseAuth) : this()

    public actual fun credential(
        verificationId: String,
        smsCode: String,
    ): PhoneAuthCredential = throw UnsupportedOperationException("Phone authentication is not supported on tvOS")

    public actual suspend fun verifyPhoneNumber(
        phoneNumber: String,
        verificationProvider: PhoneVerificationProvider,
    ): AuthCredential = throw UnsupportedOperationException("Phone authentication is not supported on tvOS")
}
