/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JsModule("firebase/compat/app")

package dev.gitlive.firebase

import kotlin.js.Json
import kotlin.js.Promise

@JsName("default")
external object firebase {

    open class App {
        fun functions(region: String? = definedExternally): functions.Functions
    }

    fun app(name: String? = definedExternally): App

    interface FirebaseError {
        var code: String
        var message: String
        var name: String
    }

    fun functions(app: App? = definedExternally): functions.Functions

    object functions {
        class Functions {
            fun httpsCallable(name: String, options: Json?): HttpsCallable
            fun useFunctionsEmulator(origin: String)
            fun useEmulator(host: String, port: Int)
        }
        interface HttpsCallableResult {
            val data: Any?
        }
        interface HttpsCallable {
        }

    }

    fun installations(app: App? = definedExternally): installations.Installations

    object installations {
        interface Installations {
            fun delete(): Promise<Unit>
            fun getId(): Promise<String>
            fun getToken(forceRefresh: Boolean): Promise<String>
        }
    }
}
