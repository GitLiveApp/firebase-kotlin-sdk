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

    fun remoteConfig(app: App? = definedExternally): remoteConfig.RemoteConfig

    object remoteConfig {
        interface RemoteConfig {
            var defaultConfig: Any
            var fetchTimeMillis: Long
            var lastFetchStatus: String
            val settings: Settings
            fun activate(): Promise<Boolean>
            fun ensureInitialized(): Promise<Unit>
            fun fetch(): Promise<Unit>
            fun fetchAndActivate(): Promise<Boolean>
            fun getAll(): Json
            fun getBoolean(key: String): Boolean
            fun getNumber(key: String): Number
            fun getString(key: String): String?
            fun getValue(key: String): Value
        }

        interface Settings {
            var fetchTimeoutMillis: Number
            var minimumFetchIntervalMillis: Number
        }

        interface Value {
            fun asBoolean(): Boolean
            fun asNumber(): Number
            fun asString(): String?
            fun getSource(): String
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
