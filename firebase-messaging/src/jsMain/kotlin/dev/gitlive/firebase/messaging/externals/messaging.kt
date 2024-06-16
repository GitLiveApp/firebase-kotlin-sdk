package dev.gitlive.firebase.messaging.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

external fun getMessaging(
    app: FirebaseApp? = definedExternally,
): Messaging

external fun getToken(messaging: Messaging = definedExternally, options: dynamic = definedExternally): Promise<String>

external interface Messaging {
    fun subscribeToTopic(tokens: Array<String>, topic: String): Promise<Unit>
    fun unsubscribeFromTopic(tokens: Array<String>, topic: String): Promise<Unit>
}