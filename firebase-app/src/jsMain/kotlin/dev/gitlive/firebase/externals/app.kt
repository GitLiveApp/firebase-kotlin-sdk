@file:JsModule("firebase/app")
@file:JsNonModule

package dev.gitlive.firebase.externals

import kotlin.js.Promise

public external fun initializeApp(options: Any, name: String = definedExternally): FirebaseApp

public external fun getApp(name: String = definedExternally): FirebaseApp

public external fun getApps(): Array<FirebaseApp>

public external fun deleteApp(app: FirebaseApp): Promise<Unit>

public external interface FirebaseApp {
    public val automaticDataCollectionEnabled: Boolean
    public val name: String
    public val options: FirebaseOptions
}

public external interface FirebaseOptions {
    public val apiKey: String
    public val appId: String
    public val authDomain: String?
    public val databaseURL: String?
    public val measurementId: String?
    public val messagingSenderId: String?
    public val gaTrackingId: String?
    public val projectId: String?
    public val storageBucket: String?
}
