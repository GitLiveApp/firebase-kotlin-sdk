/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth;

/**
 * The static members of the Firebase Android SDK that this module's own code uses. Compiled by javac after the Kotlin
 * header stubs are stripped, so it binds to the real classes (Kotlin code compiled against the stubs would call their
 * Companion objects instead, which the real classes do not have). This variant binds to firebase-java-sdk, which has no
 * phone verification and no {@code ActionCodeSettings.newBuilder()}; the Android variant lives in src/androidMain/java.
 */
final class AuthStatics {
    private AuthStatics() {}

    static AuthCredential emailCredential(String email, String password) { return EmailAuthProvider.getCredential(email, password); }
    static AuthCredential emailLinkCredential(String email, String emailLink) { return EmailAuthProvider.getCredentialWithLink(email, emailLink); }
    static AuthCredential facebookCredential(String accessToken) { return FacebookAuthProvider.getCredential(accessToken); }
    static AuthCredential githubCredential(String token) { return GithubAuthProvider.getCredential(token); }
    static AuthCredential googleCredential(String idToken, String accessToken) { return GoogleAuthProvider.getCredential(idToken, accessToken); }
    static AuthCredential twitterCredential(String token, String secret) { return TwitterAuthProvider.getCredential(token, secret); }
    static OAuthProvider.Builder oAuthProviderBuilder(String providerId, FirebaseAuth firebaseAuth) { return OAuthProvider.newBuilder(providerId, firebaseAuth); }
    static OAuthProvider.CredentialBuilder oAuthCredentialBuilder(String providerId) { return OAuthProvider.newCredentialBuilder(providerId); }
    static PhoneAuthCredential phoneCredential(String verificationId, String smsCode) { return PhoneAuthProvider.getCredential(verificationId, smsCode); }
    static void verifyPhoneNumber(Object options) { throw new UnsupportedOperationException("Phone number verification is not supported by firebase-java-sdk"); }
    static ActionCodeSettings.Builder actionCodeSettingsBuilder() { return new ActionCodeSettings.Builder(); }
}
