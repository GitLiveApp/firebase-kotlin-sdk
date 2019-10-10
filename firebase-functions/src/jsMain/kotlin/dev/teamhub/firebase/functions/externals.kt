package dev.teamhub.firebase.functions

import kotlin.js.Json

@JsModule("firebase/app")
external object firebase {

    open class App

    fun functions(app: App? = definedExternally): functions.Functions

    object functions {
        class Functions {
            fun httpsCallable(name: String, options: Json?): HttpsCallable
        }
        class HttpsCallableResult {
//            val data: Any
        }
        class HttpsCallable

    }

}