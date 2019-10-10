package dev.teamhub.firebase

@JsModule("firebase/app")
external object firebase {

    open class App
    val apps : Array<App>
    fun initializeApp(options: Any, name: String? = definedExternally) : App

    interface FirebaseError {
        var code: String
        var message: String
        var name: String
    }

}