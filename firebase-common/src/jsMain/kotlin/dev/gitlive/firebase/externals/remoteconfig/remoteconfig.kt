@file:JsModule("firebase/remote-config")
@file:JsNonModule

package dev.gitlive.firebase.externals.remoteconfig

import dev.gitlive.firebase.externals.app.FirebaseApp
import kotlin.js.Json
import kotlin.js.Promise

external fun activate(remoteConfig: RemoteConfig): Promise<Boolean>

external fun ensureInitialized(remoteConfig: RemoteConfig): Promise<Unit>

external fun fetchAndActivate(remoteConfig: RemoteConfig): Promise<Boolean>

external fun fetchConfig(remoteConfig: RemoteConfig): Promise<Unit>

external fun getAll(remoteConfig: RemoteConfig): Json

external fun getBoolean(remoteConfig: RemoteConfig, key: String): Boolean

external fun getNumber(remoteConfig: RemoteConfig, key: String): Number

external fun getRemoteConfig(app: FirebaseApp? = definedExternally): RemoteConfig

external fun getString(remoteConfig: RemoteConfig, key: String): String?

external fun getValue(remoteConfig: RemoteConfig, key: String): Value

external interface RemoteConfig {
    var defaultConfig: Any
    var fetchTimeMillis: Long
    var lastFetchStatus: String
    val settings: Settings
}

external interface Settings {
    var fetchTimeoutMillis: Number
    var minimumFetchIntervalMillis: Number
}

external interface Value {
    fun asBoolean(): Boolean
    fun asNumber(): Number
    fun asString(): String?
    fun getSource(): String
}
