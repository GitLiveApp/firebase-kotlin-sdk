/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.MobileFirebaseOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual val emulatorHost: String = "localhost"

actual val firebaseOptions: FirebaseOptions
    get() =
        MobileFirebaseOptions(
            applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
            apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
            databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
            storageBucket = "fir-kotlin-sdk.appspot.com",
            projectId = "fir-kotlin-sdk",
            gcmSenderId = "846484016111"
        )

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = GlobalScope
    .promise {
        try {
            test()
        } catch (e: Throwable) {
            e.log()
            throw e
        }
    }.asDynamic()

internal fun Throwable.log() {
    console.error(this)
    cause?.let {
        console.error("Caused by:")
        it.log()
    }
}

