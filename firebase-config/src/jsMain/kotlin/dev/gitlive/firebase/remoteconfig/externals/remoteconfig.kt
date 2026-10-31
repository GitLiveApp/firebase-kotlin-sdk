@file:JsModule("firebase/remote-config")
@file:JsNonModule

package dev.gitlive.firebase.remoteconfig.externals

import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Json
import kotlin.js.Promise

public external fun activate(remoteConfig: RemoteConfig): Promise<Boolean>

public external fun ensureInitialized(remoteConfig: RemoteConfig): Promise<Unit>

public external fun fetchAndActivate(remoteConfig: RemoteConfig): Promise<Boolean>

public external fun fetchConfig(remoteConfig: RemoteConfig): Promise<Unit>

public external fun getAll(remoteConfig: RemoteConfig): Json

public external fun getBoolean(remoteConfig: RemoteConfig, key: String): Boolean

public external fun getNumber(remoteConfig: RemoteConfig, key: String): Number

public external fun getRemoteConfig(app: FirebaseApp? = definedExternally): RemoteConfig

public external fun getString(remoteConfig: RemoteConfig, key: String): String?

public external fun getValue(remoteConfig: RemoteConfig, key: String): Value

public external interface RemoteConfig {
    public var defaultConfig: Any
    public var fetchTimeMillis: Double
    public var lastFetchStatus: String
    public val settings: Settings
}

public external interface Settings {
    public var fetchTimeoutMillis: Number
    public var minimumFetchIntervalMillis: Number
}

public external interface Value {
    public fun asBoolean(): Boolean
    public fun asNumber(): Number
    public fun asString(): String?
    public fun getSource(): String
}

public external fun onConfigUpdate(remoteConfig: RemoteConfig, observer: ConfigUpdateObserver): () -> Unit

public external fun setCustomSignals(remoteConfig: RemoteConfig, customSignals: Json): Promise<Unit>

public external interface ConfigUpdateObserver {
    public var next: (ConfigUpdate) -> Unit
    public var error: (Throwable) -> Unit
    public var complete: () -> Unit
}

public external interface ConfigUpdate {
    /** A JS `Set` of the updated keys. */
    public fun getUpdatedKeys(): dynamic
}
