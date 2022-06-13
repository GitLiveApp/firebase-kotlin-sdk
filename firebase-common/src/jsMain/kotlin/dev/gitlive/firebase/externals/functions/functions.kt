@file:JsModule("firebase/functions")
@file:JsNonModule

package dev.gitlive.firebase.externals.functions

import dev.gitlive.firebase.externals.app.FirebaseApp
import kotlin.js.Json

external fun connectFunctionsEmulator(functions: Functions, host: String, port: Int)

external fun getFunctions(
    app: FirebaseApp? = definedExternally,
    regionOrCustomDomain: String? = definedExternally
): Functions

external fun httpsCallable(functions: Functions, name: String, options: Json?): HttpsCallable

external interface Functions

external interface HttpsCallableResult {
    val data: Any?
}

external interface HttpsCallable
