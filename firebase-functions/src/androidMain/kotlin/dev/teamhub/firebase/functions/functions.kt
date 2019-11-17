package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.*
import java.util.concurrent.TimeUnit

actual val Firebase.functions
    get() = FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance())

actual fun Firebase.functions(region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(region))

actual fun Firebase.functions(app: FirebaseApp) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android))

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android, region))

actual class FirebaseFunctions internal constructor(val android: com.google.firebase.functions.FirebaseFunctions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(android.getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } })
}

@Serializable
data class Data<T>(val data: T)

actual class HttpsCallableReference internal constructor(val android: com.google.firebase.functions.HttpsCallableReference) {
    actual suspend fun call() = HttpsCallableResult(android.call().await())

    actual suspend inline fun <reified T : Any> call(data: T) =
        HttpsCallableResult(android.call(Mapper.map(Data(data))["data"]).await())

    actual suspend inline fun <reified T> call(data: T, strategy: SerializationStrategy<T>) =
        object : KSerializer<T>, SerializationStrategy<T> by strategy {
            override fun deserialize(decoder: Decoder) = error("not supported")
        }.let {
            HttpsCallableResult(android.call(Mapper.map(Data.serializer(it), Data(data))["data"]).await())
        }
}

actual class HttpsCallableResult constructor(val android: com.google.firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T: Any> data() =
        Mapper.unmapNullable<Data<T>>(mapOf("data" to android.data)).data

    actual inline fun <reified T> data(strategy: DeserializationStrategy<T>) =
        object : KSerializer<T>, DeserializationStrategy<T> by strategy {
            override fun serialize(encoder: Encoder, obj: T) = error("not supported")
        }.let {
            Mapper.unmapNullable(Data.serializer(it), mapOf("data" to android.data)).data
        }
}

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException