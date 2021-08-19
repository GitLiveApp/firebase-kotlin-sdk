package dev.gitlive.firebase.emulator

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

suspend fun fetchOobCodes(
    projectId: String,
    emulatorHost: String,
    emulatorPort: Int
): List<OobCode> {
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    return client.get<OobCodesResponse>("http://$emulatorHost:$emulatorPort/emulator/v1/projects/${projectId}/oobCodes").oobCodes
}

@Serializable
data class OobCode(
    val email: String,
    val requestType: String,
    val oobCode: String,
    val oobLink: String,
)

@Serializable
data class OobCodesResponse(val oobCodes: List<OobCode>)
