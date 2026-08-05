# Module firebase-auth
This module is a direct forward of the Firebase Authentication library. It provides the main functionality, like authenticating with Google or Apple.

## Phone authentication

`PhoneAuthProvider.verifyPhoneNumber` sends an SMS to the given number and suspends until it can return an `AuthCredential`, which you then pass to `signInWithCredential`:

```kotlin
val credential = PhoneAuthProvider().verifyPhoneNumber(phoneNumber, verificationProvider)
Firebase.auth.signInWithCredential(credential)
```

The `PhoneVerificationProvider` you hand it is how the SDK asks your UI for the code the user received. `getVerificationCode` is called once the SMS has been sent, and should suspend until the user has entered it — typically by awaiting a `CompletableDeferred` that your code entry screen completes.

### `PhoneVerificationProvider` is implemented per platform

`PhoneVerificationProvider` is an `expect interface` with no common members, because each official Firebase SDK needs something different to start the flow. It therefore cannot be implemented in common code — declare your own `expect` function that returns one and write an `actual` for each platform you support.

#### Android

```kotlin
class AndroidPhoneVerificationProvider(
    override val activity: Activity,
    private val code: CompletableDeferred<String>,
) : PhoneVerificationProvider {
    override val timeout: Long = 60
    override val unit: TimeUnit = TimeUnit.SECONDS

    override fun codeSent(triggerResend: (Unit) -> Unit) {
        // Show your code entry UI here. Call triggerResend(Unit) to send a new SMS.
    }

    override suspend fun getVerificationCode(): String = code.await()
}
```

Android is the only platform that can also complete the verification with no user input at all, via SMS auto retrieval. The SDK races auto retrieval against `getVerificationCode` and returns whichever arrives first, cancelling the other, so your code entry screen must tolerate being cancelled while it is still waiting on the user.

`timeout` and `unit` bound that auto retrieval window; they do not bound how long the user has to type. Resending replaces the verification id and invalidates the previous one, which the SDK accounts for — the code you return is always submitted against the most recent id.

#### Apple

```kotlin
class ApplePhoneVerificationProvider(
    private val code: CompletableDeferred<String>,
) : PhoneVerificationProvider {
    override val delegate: FIRAuthUIDelegateProtocol? = null
    override suspend fun getVerificationCode(): String = code.await()
}
```

`delegate` is nullable and `null` is the normal choice. It is only used to present the `SFSafariViewController` that Firebase falls back to for reCAPTCHA verification when silent APNs verification is unavailable; passing `null` lets Firebase present it from your app's key window itself. Supply one only if you need to control which view controller presents it.

There is no `timeout` here because there is nothing to time out — SMS auto retrieval is Android only, and the underlying `verifyPhoneNumber(_:uiDelegate:multiFactorSession:)` has no timeout parameter.

#### JS

```kotlin
class JsPhoneVerificationProvider(
    override val verifier: ApplicationVerifier,
    private val code: CompletableDeferred<String>,
) : PhoneVerificationProvider {
    override suspend fun getVerificationCode(verificationId: String): String = code.await()
}
```

`verifier` is an `ApplicationVerifier`, declared here as an external interface of `type` and `verify()`. This SDK does not supply an implementation, so pass the Firebase JS SDK's `RecaptchaVerifier`, which implements it. Note that JS is the one platform whose `getVerificationCode` receives the `verificationId`.

#### JVM

Phone authentication is not available on the JVM target. The [Firebase Java SDK](https://github.com/GitLiveApp/firebase-java-sdk) that backs it does not implement `PhoneAuthProvider`, so calling `verifyPhoneNumber` throws `NotImplementedError`.

### Driving the flow yourself

If you would rather not implement `PhoneVerificationProvider` at all, `PhoneAuthProvider.credential(verificationId, smsCode)` builds the credential directly from a verification id you obtained through the underlying official SDK.
