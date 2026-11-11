// Android-only: the Firebase iOS and JS SDKs have no delivery-metrics export, so the Firebase Kotlin SDK does not
// declare setDeliveryMetricsExportToBigQuery (it is listed as @unavailable in firebase-messaging's
// api/android-sdk/exclusions.txt).
// KMP: this file moves to shared/src/androidMain/kotlin/dev/gitlive/notes/shared/platform/ and the function becomes
//   actual fun configureMessagingExports(messaging: FirebaseMessaging) { ... }
// with commonMain declaring
//   expect fun configureMessagingExports(messaging: FirebaseMessaging)
// and the iOS, JS and wasmJs actuals left empty.
package dev.gitlive.notes.shared.platform

import com.google.firebase.messaging.FirebaseMessaging

fun configureMessagingExports(messaging: FirebaseMessaging) {
    messaging.setDeliveryMetricsExportToBigQuery(true)
}
