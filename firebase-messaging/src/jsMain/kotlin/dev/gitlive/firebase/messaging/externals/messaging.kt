package dev.gitlive.firebase.messaging.externals

import dev.gitlive.firebase.externals.FirebaseApp

external fun getMessaging(
    app: FirebaseApp? = definedExternally,
): Messaging

external interface Messaging