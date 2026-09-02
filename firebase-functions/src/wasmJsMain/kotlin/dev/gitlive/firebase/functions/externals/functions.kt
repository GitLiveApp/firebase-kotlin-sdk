@file:JsModule("firebase/functions")

package dev.gitlive.firebase.functions.externals

import dev.gitlive.firebase.externals.FirebaseApp

public external fun connectFunctionsEmulator(functions: Functions, host: String, port: Int)

public external fun getFunctions(
    app: FirebaseApp? = definedExternally,
    regionOrCustomDomain: String? = definedExternally,
): Functions

public external fun httpsCallable(functions: Functions, name: String, options: JsAny?): HttpsCallable

public external interface Functions : JsAny

public external interface HttpsCallableResult : JsAny {
    public val data: JsAny?
}

public external interface HttpsCallable : JsAny
