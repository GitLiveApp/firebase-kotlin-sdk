<h1 align="left">Firebase Kotlin SDK <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/teamhubapp/firebase-kotlin-sdk?style=flat-square"></h1>
<img align="left" width="75px" src="https://avatars2.githubusercontent.com/u/42865805?s=200&v=4"> 
  <b>Built and maintained with 🧡 by <a href="https://teamhub.dev">TeamHub</a></b><br/>
  <i>Real-time code collaboration inside any IDE</i><br/>
  🔓 <a href="https://teamhub.typeform.com/to/uSS8cv">Request Early Access</a>
<h4></h4>

The Firebase Kotlin SDK is a Kotlin-first SDK for Firebase. It's API is similar to the [Firebase Android SDK Kotlin Extensions](https://firebase.github.io/firebase-android-sdk/reference/kotlin/firebase-ktx/) but it also supports multiplatform projects, enabling you to use Firebase directly from your common source targeting *iOS*, *Android* or *JS*.

## Kotlin-first design

Unlike the Kotlin Extensions for the Firebase Android SDK this project does not extend a Java based SDK so we get to use the full power of Kotlin.

<h4><a href="https://kotlinlang.org/docs/tutorials/coroutines/async-programming.html#coroutines">Suspending functions</a></h4>

Asynchronous operations that return a single or no value are represented by suspending functions in the SDK instead of callbacks, listeners or OS specific types such as [Task](https://developer.android.com/reference/com/google/android/play/core/tasks/Task), for example:

`suspend fun signInWithCustomToken(token: String): AuthResult`

<h4><a href="https://kotlinlang.org/docs/reference/coroutines/flow.html">Flows</a></h4>

Asynchronous streams of values are represented by Flows in the SDK instead of repeatedly Invoked callbacks or listeners, for example:

`val snapshots: Flow<DocumentSnapshot>`

<h4><a href="https://github.com/Kotlin/kotlinx.serialization">Serialization</a></h4>

<h4><a href="https://kotlinlang.org/docs/reference/functions.html#named-arguments">Named arguments</a></h4>

<h3><a href="https://kotlinlang.org/docs/reference/multiplatform.html">Multiplatform</a></h3>

## Available libraries

The following libraries are available for the various Firebase products.

| Service or Product	                                                                 | Gradle Dependency                                                                                                                   | API Coverage                                                                                                                                                                                                               |
| ------------------------------------------------------------------------------------ | :-----------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [Authentication](https://firebase.google.com/docs/auth#kotlin-android)               | [`dev.teamhub.firebase:firebase-auth:0.1.0`](https://mvnrepository.com/artifact/dev.teamhub.firebase/firebase-auth/0.1.0)           | [![40%](https://img.shields.io/badge/-0--50%25-red?style=flat-square)](/firebase-auth/src/commonMain/kotlin/dev/teamhub/firebase/auth/auth.kt) |
| [Realtime Database](https://firebase.google.com/docs/database#kotlin-android)        | [`dev.teamhub.firebase:firebase-database:0.1.0`](https://mvnrepository.com/artifact/dev.teamhub.firebase/firebase-database/0.1.0)   | [![70%](https://img.shields.io/badge/-0--50%25-red?style=flat-square)](/firebase-database/src/commonMain/kotlin/dev/teamhub/firebase/auth/database.kt) |
| [Cloud Firestore](https://firebase.google.com/docs/firestore#kotlin-android)         | [`dev.teamhub.firebase:firebase-firestore:0.1.0`](https://mvnrepository.com/artifact/dev.teamhub.firebase/firebase-firestore/0.1.0) | [![60%](https://img.shields.io/badge/-0--50%25-red?style=flat-square)](/firebase-firestore/src/commonMain/kotlin/dev/teamhub/firebase/firestore/firestore.kt) |
| [Cloud Functions](https://firebase.google.com/docs/functions/callable#kotlin-android)| [`dev.teamhub.firebase:firebase-functions:0.1.0`](https://mvnrepository.com/artifact/dev.teamhub.firebase/firebase-functions/0.1.0) | [![80%](https://img.shields.io/badge/-0--50%25-orange?style=flat-square)](/firebase-functions/src/commonMain/kotlin/dev/teamhub/firebase/functions/functions.kt) |
| [Cloud Messaging](https://firebase.google.com/docs/messaging#kotlin-android)         | [`dev.teamhub.firebase:firebase-messaging:0.1.0`](https://mvnrepository.com/artifact/dev.teamhub.firebase/firebase-messaging/0.1.0) | ![0%](https://img.shields.io/badge/-0%25-lightgrey?style=flat-square) |
| [Cloud Storage](https://firebase.google.com/docs/storage#kotlin-android)             | [`dev.teamhub.firebase:firebase-storage:0.1.0`](https://mvnrepository.com/artifact/dev.teamhub.firebase/firebase-storage/0.1.0)     | ![0%](https://img.shields.io/badge/-0%25-lightgrey?style=flat-square) |

Is the Firebase library or API you need missing? [Create an issue](https://github.com/TeamHubApp/firebase-kotlin-sdk/issues/new?labels=API+coverage&template=increase-api-coverage.md&title=Add+%5Bclass+name%5D.%5Bfunction+name%5D+to+%5Blibrary+name%5D+for+%5Bplatform+names%5D) to request additional API coverage or be awesome and [submit a PR](https://github.com/TeamHubApp/firebase-kotlin-sdk/fork)

## Getting started

### New to Firebase?

Head over to the main Firebase documentation and follow the steps 

### Configure Firebase

Since you most likely want to share your firebase configuration across platforms you should configure Firebase programatically in your common source instead of a platform-specific configuration file.

You can do this as follows:

```kotlin
val options = FirebaseOptions.Builder()
        .setApplicationId("1:27992087142:android:ce3b6448250083d1") // Required for Analytics.
        .setApiKey("AIzaSyADUe90ULnQDuGShD9W23RDP0xmeDc6Mvw") // Required for Auth.
        .setDatabaseUrl("https://myproject.firebaseio.com") // Required for RTDB.
        .build()
```
