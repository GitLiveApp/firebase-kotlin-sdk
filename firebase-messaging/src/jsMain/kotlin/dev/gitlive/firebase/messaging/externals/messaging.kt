package dev.gitlive.firebase.messaging.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

public external fun getMessaging(
    app: FirebaseApp? = definedExternally,
): Messaging

public external fun getToken(messaging: Messaging = definedExternally, options: dynamic = definedExternally): Promise<String>

public external fun deleteToken(messaging: Messaging = definedExternally): Promise<Boolean>

public external interface Messaging
