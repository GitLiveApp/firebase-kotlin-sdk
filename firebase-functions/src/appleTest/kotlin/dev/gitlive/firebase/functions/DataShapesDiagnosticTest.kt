/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.getHttpsCallable
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import platform.Foundation.NSArray
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.arrayWithObject
import platform.Foundation.create
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/** Temporary: finds which call data shapes the iOS SDK's serializer rejects. */
@OptIn(BetaInteropApi::class)
class DataShapesDiagnosticTest {

    private lateinit var functions: FirebaseFunctions

    @BeforeTest
    fun initializeFirebase() {
        val app = dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
            context,
            dev.gitlive.firebase.FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
        functions = FirebaseFunctions.getInstance(app.compat).apply { useEmulator(emulatorHost, 5001) }
    }

    @Test
    fun dataShapes() = runTest {
        val callable = functions.getHttpsCallable("echo")
        val failures = mutableListOf<String>()
        val results = mutableListOf<String>()
        suspend fun probe(name: String, block: suspend () -> Any?) {
            runCatching { block() }.onFailure { failures += "$name: $it" }.onSuccess { results += "$name -> $it (${it?.let { v -> v::class.simpleName }})" }
        }
        probe("text") { callable.call(mapOf("text" to "a")).await().data }
        probe("number") { callable.call(mapOf("number" to 1.5)).await().data }
        probe("int") { callable.call(mapOf("int" to 1)).await().data }
        probe("long") { callable.call(mapOf("long" to 2L)).await().data }
        probe("flag") { callable.call(mapOf("flag" to true)).await().data }
        probe("none") { callable.call(mapOf("none" to null)).await().data }
        probe("list") { callable.call(mapOf("list" to listOf(1, "b"))).await().data }
        probe("listWithNull") { callable.call(mapOf("list" to listOf(1, null))).await().data }
        probe("nested") { callable.call(mapOf("nested" to mapOf("k" to 1))).await().data }
        probe("rootString") { callable.call("a").await().data }
        probe("rootNull") { callable.call(null).await().data }
        probe("rootList") { callable.call(listOf(1, "b")).await().data }
        probe("rootBoolean") { callable.call(true).await().data }
        probe("iosDictionary") { callIos(callable, NSMutableDictionary().apply { setObject("a", NSString.create(string = "k")) }) }
        probe("iosDictionaryView") { callIos(callable, NSArray.arrayWithObject(NSMutableDictionary().apply { setObject("a", NSString.create(string = "k")) })[0]) }
        probe("iosArray") { callIos(callable, NSMutableArray().apply { addObject("a") }) }
        probe("iosArrayView") { callIos(callable, NSArray.arrayWithObject(NSMutableArray().apply { addObject("a") })[0]) }
        probe("iosKotlinMap") { callIos(callable, mapOf("k" to "a")) }
        probe("iosKotlinString") { callIos(callable, "a") }
        println("DIAGNOSTIC RESULTS\n" + results.joinToString("\n"))
        assertTrue(failures.isEmpty(), "DIAGNOSTIC FAILURES\n" + failures.joinToString("\n") + "\nRESULTS\n" + results.joinToString("\n"))
    }

    private suspend fun callIos(callable: HttpsCallableReference, data: Any?): Any? = suspendCancellableCoroutine { continuation ->
        callable.ios.callWithObject(data) { result, error ->
            if (error != null) continuation.resumeWithException(IllegalStateException(error.localizedDescription)) else continuation.resume(result?.data())
        }
    }
}
