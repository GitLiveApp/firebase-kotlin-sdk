/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.DataConnectException
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse as JavaHttpResponse

private val client: HttpClient by lazy { HttpClient.newHttpClient() }

internal actual suspend fun httpPost(url: String, headers: Map<String, String>, body: String): HttpResponse {
    val request = HttpRequest.newBuilder(URI.create(url))
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .apply { headers.forEach { (name, value) -> header(name, value) } }
        .build()
    val response = try {
        client.sendAsync(request, JavaHttpResponse.BodyHandlers.ofString()).await()
    } catch (e: java.io.IOException) {
        throw DataConnectException("request to $url failed: ${e.message}", e)
    }
    return HttpResponse(response.statusCode(), response.body())
}
