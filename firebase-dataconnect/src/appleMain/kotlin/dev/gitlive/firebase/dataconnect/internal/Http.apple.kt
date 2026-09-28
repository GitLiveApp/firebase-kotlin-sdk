/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.DataConnectException
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(BetaInteropApi::class)
internal actual suspend fun httpPost(url: String, headers: Map<String, String>, body: String): HttpResponse = suspendCancellableCoroutine { continuation ->
    val request = NSMutableURLRequest(uRL = NSURL(string = url))
    request.setHTTPMethod("POST")
    headers.forEach { (name, value) -> request.setValue(value, forHTTPHeaderField = name) }
    @Suppress("CAST_NEVER_SUCCEEDS")
    request.setHTTPBody((body as NSString).dataUsingEncoding(NSUTF8StringEncoding))
    val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
        if (error != null) {
            continuation.resumeWithException(DataConnectException("request to $url failed: ${error.localizedDescription}"))
        } else {
            val status = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0

            @Suppress("CAST_NEVER_SUCCEEDS")
            val text = data?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding) as String? } ?: ""
            continuation.resume(HttpResponse(status, text))
        }
    }
    task.resume()
    continuation.invokeOnCancellation { task.cancel() }
}
