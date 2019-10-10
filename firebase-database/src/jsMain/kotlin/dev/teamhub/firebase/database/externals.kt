package dev.teamhub.firebase.database

import kotlin.js.Promise

@JsModule("firebase/app")
external object firebase {

    open class App
    fun database(): database.Database
    object database {
        fun enableLogging(logger: Boolean?, persistent: Boolean? = definedExternally)

        open class Database {
            fun ref(path: String? = definedExternally): Reference
        }
        open class ThenableReference

        open class Reference {
            fun remove(): Promise<Unit>
            fun onDisconnect(): OnDisconnect

            fun update(value: Any?): Promise<Unit>
            fun set(value: Any?): Promise<Unit>
            fun on(eventType: String?, callback: (data: DataSnapshot) -> Unit, cancelCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): (DataSnapshot) -> Unit
            fun off(eventType: String?, callback: (data: DataSnapshot) -> Unit, context: Any? = definedExternally)
            fun once(eventType: String, callback: (data: DataSnapshot) -> Unit, failureCallbackOrContext: (error: Error) -> Unit? = definedExternally, context: Any? = definedExternally): (DataSnapshot)->Unit
            fun push(): ThenableReference
        }
        open class DataSnapshot {
            fun `val`(): Any
            fun exists(): Boolean
            fun forEach(action: (a: DataSnapshot)-> Boolean): Boolean
            fun numChildren(): Int
        }

        open class OnDisconnect {
            fun update(value: Any?): Promise<Unit>
            fun remove(): Promise<Unit>
            fun cancel(): Promise<Unit>
            fun set(value: Any?): Promise<Unit>
        }

        object ServerValue {
            val TIMESTAMP: Map<String, String>
        }
    }
}