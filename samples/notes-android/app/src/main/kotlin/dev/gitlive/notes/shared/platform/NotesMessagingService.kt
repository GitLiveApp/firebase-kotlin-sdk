// Android-only: FirebaseMessagingService is an android.app.Service, which the Firebase Kotlin SDK leaves to platform code
// (received messages reach iOS through the app delegate and JS through a service worker).
// KMP: this file moves to shared/src/androidMain (or stays in the Android app) unchanged, and the <service> stays in the
// Android manifest. RemoteMessage itself is available in common code, so a shared handler could take the message.
package dev.gitlive.notes.shared.platform

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotesMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        FirebaseCrashlytics.getInstance().log("Messaging token refreshed")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        FirebaseCrashlytics.getInstance().log("Message ${message.messageId} from ${message.from}: ${message.notification?.title}")
    }
}
