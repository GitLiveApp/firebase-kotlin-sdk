/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.DataConnectException
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

internal actual suspend fun httpPost(url: String, headers: Map<String, String>, body: String): HttpResponse {
    val init = json("method" to "POST", "headers" to json(*headers.map { it.key to it.value }.toTypedArray()), "body" to body)
    val response = try {
        fetch(url, init).await()
    } catch (e: Throwable) {
        throw DataConnectException("request to $url failed: ${e.message}", e)
    }
    val text = (response.text() as Promise<String>).await()
    return HttpResponse((response.status as Number).toInt(), text)
}

/** The global fetch of the browser or of Node 18+. */
@Suppress("UNUSED_PARAMETER")
private fun fetch(url: String, init: dynamic): Promise<dynamic> = js("fetch(url, init)")
