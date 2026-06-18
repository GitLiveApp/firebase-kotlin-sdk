@file:JsModule("firebase/app")

package dev.gitlive.firebase.externals

import kotlin.js.Promise

public external fun initializeApp(options: JsAny, name: String = definedExternally): FirebaseApp

public external fun getApp(name: String = definedExternally): FirebaseApp

public external fun getApps(): JsArray<FirebaseApp>

public external fun deleteApp(app: FirebaseApp): Promise<JsAny?>

public external interface FirebaseApp : JsAny {
    public val automaticDataCollectionEnabled: Boolean
    public val name: String
    public val options: FirebaseOptions
}

public external interface FirebaseOptions : JsAny {
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
