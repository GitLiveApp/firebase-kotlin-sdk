/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

/** The response to an HTTP request: the status code and the body as text. */
internal class HttpResponse(val status: Int, val body: String)

/**
 * Sends a POST request with a text [body] and returns the response; throws [com.google.firebase.dataconnect.DataConnectException]
 * when no response is received. Each platform uses its own HTTP client (java.net.http, fetch, NSURLSession).
 */
internal expect suspend fun httpPost(url: String, headers: Map<String, String>, body: String): HttpResponse
